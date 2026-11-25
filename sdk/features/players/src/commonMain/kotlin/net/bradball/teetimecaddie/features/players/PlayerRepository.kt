package net.bradball.teetimecaddie.features.players

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.bradbal.teetimecaddie.core.storage.PlayerPhotoStorage
import net.bradbal.teetimecaddie.core.storage.PlayerStorage
import net.bradbal.teetimecaddie.core.storage.documents.PlayerDocument
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.analytics.LoggableExceptionTypes
import net.bradball.teetimecaddie.core.extensions.isValidPhoneNumber
import net.bradball.teetimecaddie.core.extensions.toPhoneDigits
import net.bradball.teetimecaddie.core.models.Player
import net.bradball.teetimecaddie.core.models.TtcLookup
import net.bradball.teetimecaddie.core.models.TtcResult

/**
 * Player profiles: the name, phone number and avatar behind an account.
 *
 * Takes player ids as parameters and knows nothing about who is signed in — that is
 * `SessionManager`'s concern.
 *
 * **Nothing here throws.** Failures come back as [TtcResult.Failure] carrying a [PlayerException].
 */
interface PlayerRepository {

    /**
     * Observes a player's profile, emitting it again whenever it changes.
     *
     * Emits null only when the server confirms no profile exists. While the profile can't be read
     * — offline, with nothing cached — it emits nothing rather than null, so a null is always safe
     * to act on as "this person has no profile yet".
     *
     * @param playerId The id of the player to observe.
     */
    fun playerFlow(playerId: String): Flow<Player?>

    /**
     * Reads a player's profile once.
     *
     * @param playerId The id of the player to read.
     * @return The player, or success with null when they have no profile yet.
     *   [TtcLookup.Failure] when the read fails, kept distinct from finding no profile.
     */
    suspend fun getPlayer(playerId: String): TtcLookup<Player>

    /**
     * Finds the player who owns a phone number.
     *
     * @param phone The number to look up, in any format; it is normalized first.
     * @return The player, or success with null when no player has the number — it belongs to
     *   someone without the app. [TtcLookup.Failure] when the lookup fails.
     */
    suspend fun findPlayerByPhone(phone: String): TtcLookup<Player>

    /**
     * Checks whether a phone number already belongs to a player.
     *
     * @param phone The number to check, in any format; it is normalized first.
     * @param excludingPlayerId A player whose own number doesn't count, so re-saving a profile
     *   doesn't collide with itself. Null to count every player.
     * @return True when a player other than [excludingPlayerId] has the number, or a failure when
     *   the lookup fails.
     */
    suspend fun isPhoneInUse(phone: String, excludingPlayerId: String? = null): TtcResult<Boolean>

    /**
     * Creates a player's profile, uploading their avatar first when there is one.
     *
     * The name is trimmed and the number stored as its digits. Nothing is written when the name,
     * the number or the avatar upload fails.
     *
     * @param playerId The id of the account the profile belongs to.
     * @param name The player's name. Must not be blank.
     * @param email The account's email address.
     * @param phone The player's phone number, in any format. Must be a complete 10-digit number no
     *   other player has.
     * @param photo The avatar as JPEG bytes, already downscaled, or null for none.
     * @return The new player. [PlayerErrors.NO_NAME] for a blank name, [PlayerErrors.INVALID_PHONE]
     *   for an incomplete number, [PlayerErrors.PHONE_IN_USE] when another player has the number,
     *   [PlayerErrors.PHOTO_UPLOAD_FAILED] when the avatar can't be uploaded, and
     *   [PlayerErrors.SAVE_FAILED] when the profile can't be written.
     */
    suspend fun createPlayer(
        playerId: String,
        name: String,
        email: String,
        phone: String,
        photo: ByteArray? = null
    ): TtcResult<Player>

    /**
     * Saves changes to an existing player's profile.
     *
     * The name is trimmed and the number stored as its digits. Nothing is written when the name or
     * number is rejected.
     *
     * @param player The profile as it should be saved. Its name must not be blank, and its phone
     *   number must be a complete 10-digit number no other player has.
     * @return The player as saved. [PlayerErrors.NO_NAME] for a blank name,
     *   [PlayerErrors.INVALID_PHONE] for an incomplete number, [PlayerErrors.PHONE_IN_USE] when
     *   another player has the number, and [PlayerErrors.SAVE_FAILED] when the write fails.
     */
    suspend fun updatePlayer(player: Player): TtcResult<Player>
}

class PlayerRepositoryImpl(
    private val eventManager: EventManager,
    private val playerStorage: PlayerStorage,
    private val playerPhotoStorage: PlayerPhotoStorage
) : PlayerRepository {

    override fun playerFlow(playerId: String): Flow<Player?> =
        playerStorage.playerFlow(playerId).map { it?.toModel() }

    override suspend fun getPlayer(playerId: String): TtcLookup<Player> =
        runLookup("get_player") { playerStorage.getPlayer(playerId)?.toModel() }

    override suspend fun findPlayerByPhone(phone: String): TtcLookup<Player> =
        runLookup("find_player_by_phone") {
            playerStorage.findPlayerByPhone(phone.toPhoneDigits())?.toModel()
        }

    // A computed answer rather than a read, so this is a TtcResult: it is never "absent".
    override suspend fun isPhoneInUse(phone: String, excludingPlayerId: String?): TtcResult<Boolean> =
        when (val owner = findPlayerByPhone(phone)) {
            is TtcLookup.Failure -> TtcResult.Failure(owner.error)
            is TtcLookup.Success -> {
                val existing = owner.data
                TtcResult.Success(existing != null && existing.id != excludingPlayerId)
            }
        }

    override suspend fun createPlayer(
        playerId: String,
        name: String,
        email: String,
        phone: String,
        photo: ByteArray?
    ): TtcResult<Player> {
        if (name.isBlank()) return TtcResult.Failure(PlayerException(PlayerErrors.NO_NAME))
        if (!phone.isValidPhoneNumber) return TtcResult.Failure(PlayerException(PlayerErrors.INVALID_PHONE))

        val digits = phone.toPhoneDigits()

        // Check the number before anything is written. On a hit the auth account is left alone:
        // the person may simply have mistyped, and the SDK no longer holds their password, so
        // deleting the account here would strand them.
        when (val inUse = isPhoneInUse(digits, excludingPlayerId = playerId)) {
            is TtcResult.Failure -> return inUse
            is TtcResult.Success -> if (inUse.data) {
                return TtcResult.Failure(PlayerException(PlayerErrors.PHONE_IN_USE))
            }
        }

        // Upload before the document write, so a failure here leaves nothing behind to unwind.
        val photoUrl = if (photo != null) {
            try {
                playerPhotoStorage.uploadAvatar(playerId, photo)
            } catch (ex: Exception) {
                currentCoroutineContext().ensureActive()
                eventManager.logException(
                    ex, LoggableExceptionTypes.PLAYERS, hashMapOf("action" to "upload_avatar")
                )
                return TtcResult.Failure(PlayerException(PlayerErrors.PHOTO_UPLOAD_FAILED, cause = ex))
            }
        } else {
            null
        }

        val document = PlayerDocument(name = name.trim(), email = email, phone = digits, photoUrl = photoUrl)

        return runStorage("create_player") {
            playerStorage.addPlayer(playerId, document)
            document.also { it.id = playerId }.toModel()
        }
    }

    override suspend fun updatePlayer(player: Player): TtcResult<Player> {
        if (player.name.isBlank()) return TtcResult.Failure(PlayerException(PlayerErrors.NO_NAME))
        if (!player.phone.isValidPhoneNumber) {
            return TtcResult.Failure(PlayerException(PlayerErrors.INVALID_PHONE))
        }

        when (val inUse = isPhoneInUse(player.phone, excludingPlayerId = player.id)) {
            is TtcResult.Failure -> return inUse
            is TtcResult.Success -> if (inUse.data) {
                return TtcResult.Failure(PlayerException(PlayerErrors.PHONE_IN_USE))
            }
        }

        val updated = player.copy(name = player.name.trim(), phone = player.phone.toPhoneDigits())

        return runStorage("update_player") {
            playerStorage.updatePlayer(updated.id, updated.toDocument())
            updated
        }
    }

    /**
     * Run a storage call, turning anything it throws into a [PlayerErrors.SAVE_FAILED] result.
     *
     * The storage layer still throws — it wraps Firestore — so this is the boundary where those
     * exceptions stop and become values.
     */
    private suspend inline fun <T : Any> runStorage(action: String, block: () -> T): TtcResult<T> = try {
        TtcResult.Success(block())
    } catch (ex: Exception) {
        // Re-throws if this coroutine was cancelled, so cancellation is not swallowed into a
        // Failure. See the note in AuthRepository for why this beats catching the type.
        currentCoroutineContext().ensureActive()
        eventManager.logException(ex, LoggableExceptionTypes.PLAYERS, hashMapOf("action" to action))
        TtcResult.Failure(PlayerException(PlayerErrors.SAVE_FAILED, cause = ex))
    }

    /**
     * Same, for reads that can legitimately come back empty. A null payload means "found nothing";
     * a thrown storage error still becomes a [Failure]. Keeping those apart is the whole point.
     */
    private suspend inline fun <T : Any> runLookup(action: String, block: () -> T?): TtcLookup<T> = try {
        TtcLookup.Success(block())
    } catch (ex: Exception) {
        currentCoroutineContext().ensureActive()
        eventManager.logException(ex, LoggableExceptionTypes.PLAYERS, hashMapOf("action" to action))
        TtcLookup.Failure(PlayerException(PlayerErrors.SAVE_FAILED, cause = ex))
    }
}

