package net.bradball.teetimecaddie.features.players

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
import kotlin.coroutines.cancellation.CancellationException

/**
 * Player profiles: the name, phone number and avatar behind an account.
 *
 * Takes player ids as parameters and knows nothing about who is signed in — that is
 * `SessionManager`'s concern. Keeping it session-agnostic is what lets the invitation features
 * ([findPlayerByPhone]) use it without dragging authentication along.
 */
interface PlayerRepository {

    /** Observe a player, re-emitting whenever their profile changes. Emits null if none exists. */
    fun playerFlow(playerId: String): Flow<Player?>

    /** Read a player once, or null if they have no profile yet. */
    suspend fun getPlayer(playerId: String): Player?

    /**
     * Find the player who owns a phone number.
     *
     * Accepts any way of writing the number; it is normalized before the lookup.
     */
    suspend fun findPlayerByPhone(phone: String): Player?

    /**
     * Whether a phone number already belongs to someone.
     *
     * @param excludingPlayerId A player to ignore, so re-saving your own profile does not collide
     *   with your own number.
     */
    suspend fun isPhoneInUse(phone: String, excludingPlayerId: String? = null): Boolean

    /**
     * Create a player's profile, uploading their avatar first if they chose one.
     *
     * @param photo JPEG bytes, already downscaled by the caller, or null.
     * @throws PlayerException [PlayerErrors.PHONE_IN_USE] if the number belongs to someone else —
     *   thrown *without* touching the auth account, so a mistyped digit stays recoverable.
     */
    @Throws(PlayerException::class, CancellationException::class)
    suspend fun createPlayer(
        playerId: String,
        name: String,
        email: String,
        phone: String,
        photo: ByteArray? = null
    ): Player

    @Throws(PlayerException::class, CancellationException::class)
    suspend fun updatePlayer(player: Player): Player
}

class PlayerRepositoryImpl(
    private val eventManager: EventManager,
    private val playerStorage: PlayerStorage,
    private val playerPhotoStorage: PlayerPhotoStorage
) : PlayerRepository {

    override fun playerFlow(playerId: String): Flow<Player?> =
        playerStorage.playerFlow(playerId).map { it?.toModel() }

    override suspend fun getPlayer(playerId: String): Player? =
        playerStorage.getPlayer(playerId)?.toModel()

    override suspend fun findPlayerByPhone(phone: String): Player? =
        playerStorage.findPlayerByPhone(phone.toPhoneDigits())?.toModel()

    override suspend fun isPhoneInUse(phone: String, excludingPlayerId: String?): Boolean {
        val owner = findPlayerByPhone(phone) ?: return false
        return owner.id != excludingPlayerId
    }

    override suspend fun createPlayer(
        playerId: String,
        name: String,
        email: String,
        phone: String,
        photo: ByteArray?
    ): Player {
        if (name.isBlank()) throw PlayerException(PlayerErrors.NO_NAME)
        if (!phone.isValidPhoneNumber) throw PlayerException(PlayerErrors.INVALID_PHONE)

        val digits = phone.toPhoneDigits()

        // Check the number before anything is written. On a hit the auth account is left alone:
        // the person may simply have mistyped, and the SDK no longer holds their password, so
        // deleting the account here would strand them.
        if (isPhoneInUse(digits, excludingPlayerId = playerId)) {
            throw PlayerException(PlayerErrors.PHONE_IN_USE)
        }

        // Upload before the document write, so a failure here leaves nothing behind to unwind.
        val photoUrl = photo?.let { bytes ->
            try {
                playerPhotoStorage.uploadAvatar(playerId, bytes)
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                eventManager.logException(
                    ex, LoggableExceptionTypes.PLAYERS, hashMapOf("action" to "upload_avatar")
                )
                throw PlayerException(PlayerErrors.PHOTO_UPLOAD_FAILED, cause = ex)
            }
        }

        val document = PlayerDocument(name = name.trim(), email = email, phone = digits, photoUrl = photoUrl)

        try {
            playerStorage.addPlayer(playerId, document)
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            eventManager.logException(
                ex, LoggableExceptionTypes.PLAYERS, hashMapOf("action" to "create_player")
            )
            throw PlayerException(PlayerErrors.SAVE_FAILED, cause = ex)
        }

        return document.also { it.id = playerId }.toModel()
    }

    override suspend fun updatePlayer(player: Player): Player {
        if (player.name.isBlank()) throw PlayerException(PlayerErrors.NO_NAME)
        if (!player.phone.isValidPhoneNumber) throw PlayerException(PlayerErrors.INVALID_PHONE)

        if (isPhoneInUse(player.phone, excludingPlayerId = player.id)) {
            throw PlayerException(PlayerErrors.PHONE_IN_USE)
        }

        val updated = player.copy(name = player.name.trim(), phone = player.phone.toPhoneDigits())

        try {
            playerStorage.updatePlayer(updated.id, updated.toDocument())
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            eventManager.logException(
                ex, LoggableExceptionTypes.PLAYERS, hashMapOf("action" to "update_player")
            )
            throw PlayerException(PlayerErrors.SAVE_FAILED, cause = ex)
        }

        return updated
    }
}
