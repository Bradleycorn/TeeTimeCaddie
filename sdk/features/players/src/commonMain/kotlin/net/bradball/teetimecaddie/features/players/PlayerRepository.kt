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
 * `SessionManager`'s concern. Keeping it session-agnostic is what lets the invitation features
 * ([findPlayerByPhone]) use it without dragging authentication along.
 *
 * **Nothing here throws.** Failures come back as [TtcResult.Failure] carrying a [PlayerException].
 */
interface PlayerRepository {

    /**
     * Observe a player, re-emitting whenever their profile changes. Emits null if none exists.
     *
     * A flow, so it reports values rather than results; a read failure simply means no emission.
     */
    fun playerFlow(playerId: String): Flow<Player?>

    /**
     * Read a player once.
     *
     * A [TtcLookup], because having no profile yet is a normal answer — and it stays distinct from
     * a read that failed.
     */
    suspend fun getPlayer(playerId: String): TtcLookup<Player>

    /**
     * Find the player who owns a phone number.
     *
     * Accepts any way of writing the number; it is normalized before the lookup. Finding nobody
     * is a normal answer — TTC-69 and TTC-73 branch on it to tell app users from invitees the
     * organizer manages by hand.
     */
    suspend fun findPlayerByPhone(phone: String): TtcLookup<Player>

    /**
     * Whether a phone number already belongs to someone.
     *
     * @param excludingPlayerId A player to ignore, so re-saving your own profile does not collide
     *   with your own number.
     */
    suspend fun isPhoneInUse(phone: String, excludingPlayerId: String? = null): TtcResult<Boolean>

    /**
     * Create a player's profile, uploading their avatar first if they chose one.
     *
     * @param photo JPEG bytes, already downscaled by the caller, or null.
     * @return [PlayerErrors.PHONE_IN_USE] if the number belongs to someone else — reported
     *   *without* touching the auth account, so a mistyped digit stays recoverable.
     */
    suspend fun createPlayer(
        playerId: String,
        name: String,
        email: String,
        phone: String,
        photo: ByteArray? = null
    ): TtcResult<Player>

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

