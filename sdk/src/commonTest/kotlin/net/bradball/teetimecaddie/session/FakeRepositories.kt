package net.bradball.teetimecaddie.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import net.bradball.teetimecaddie.core.models.Player
import net.bradball.teetimecaddie.core.models.TtcResult
import net.bradball.teetimecaddie.core.models.exceptions.TeeTimeCaddieException
import net.bradball.teetimecaddie.features.auth.AuthRepository
import net.bradball.teetimecaddie.features.auth.AuthUser
import net.bradball.teetimecaddie.features.players.PlayerErrors
import net.bradball.teetimecaddie.features.players.PlayerException
import net.bradball.teetimecaddie.features.players.PlayerRepository
import net.bradball.teetimecaddie.features.players.isNotFound

/**
 * Hand-written fakes for [SessionManager]'s two collaborators.
 *
 * These are the reason the repositories are interfaces: `SessionManager` touches no Firebase
 * itself, so with these in place its state machine — and in particular the guard that stops
 * [SessionManager.abandonSignUp] deleting a provisioned account — is genuinely testable.
 */
class FakeAuthRepository(
    initialUser: AuthUser? = null
) : AuthRepository {

    private val users = MutableStateFlow(initialUser)

    var signInResult: TtcResult<AuthUser> = TtcResult.Success(AuthUser("user-1", "dana@golf.app"))
    var createAccountResult: TtcResult<AuthUser> = TtcResult.Success(AuthUser("user-1", "dana@golf.app"))
    var deleteSucceeds = true

    var deleteCount = 0
        private set
    var signOutCount = 0
        private set
    val displayNameUpdates = mutableListOf<String>()

    override val currentUser: AuthUser?
        get() = users.value

    override val authChanges: Flow<AuthUser?> = users

    override suspend fun signIn(email: String, password: String): TtcResult<AuthUser> =
        signInResult.also { if (it is TtcResult.Success) users.value = it.data }

    override suspend fun createAccount(email: String, password: String): TtcResult<AuthUser> =
        createAccountResult.also { if (it is TtcResult.Success) users.value = it.data }

    override suspend fun signOut() {
        signOutCount++
        users.value = null
    }

    override suspend fun deleteCurrentUser(): Boolean {
        deleteCount++
        if (deleteSucceeds) users.value = null
        return deleteSucceeds
    }

    override suspend fun refreshAuthentication() = Unit

    override suspend fun updateDisplayName(name: String) {
        displayNameUpdates.add(name)
    }
}

class FakePlayerRepository(
    vararg players: Player
) : PlayerRepository {

    private val store = MutableStateFlow(players.associateBy { it.id })

    /** When set, every read fails with it — used to prove a failed read is not read as "absent". */
    var readFailure: TeeTimeCaddieException? = null

    /** When set, [createPlayer] fails with it. */
    var createFailure: TeeTimeCaddieException? = null

    override fun playerFlow(playerId: String): Flow<Player?> = store.map { it[playerId] }

    override suspend fun getPlayer(playerId: String): TtcResult<Player> =
        readFailure?.let { TtcResult.Failure(it) }
            ?: store.value[playerId]?.let { TtcResult.Success(it) }
            ?: TtcResult.Failure(PlayerException(PlayerErrors.NOT_FOUND))

    override suspend fun findPlayerByPhone(phone: String): TtcResult<Player> =
        readFailure?.let { TtcResult.Failure(it) }
            ?: store.value.values.firstOrNull { it.phone == phone }?.let { TtcResult.Success(it) }
            ?: TtcResult.Failure(PlayerException(PlayerErrors.NOT_FOUND))

    override suspend fun isPhoneInUse(phone: String, excludingPlayerId: String?): TtcResult<Boolean> =
        when (val owner = findPlayerByPhone(phone)) {
            is TtcResult.Success -> TtcResult.Success(owner.data.id != excludingPlayerId)
            is TtcResult.Failure -> if (owner.isNotFound) TtcResult.Success(false) else owner
        }

    override suspend fun createPlayer(
        playerId: String,
        name: String,
        email: String,
        phone: String,
        photo: ByteArray?
    ): TtcResult<Player> {
        createFailure?.let { return TtcResult.Failure(it) }
        val player = Player(id = playerId, name = name, email = email, phone = phone)
        store.value = store.value + (playerId to player)
        return TtcResult.Success(player)
    }

    override suspend fun updatePlayer(player: Player): TtcResult<Player> {
        store.value = store.value + (player.id to player)
        return TtcResult.Success(player)
    }
}

/**
 * A failure that is emphatically NOT [PlayerErrors.NOT_FOUND] — used to prove that a read which
 * genuinely failed is never mistaken for a profile that simply does not exist.
 */
fun readFailure() = PlayerException(PlayerErrors.SAVE_FAILED)

/** Convenience for building a phone collision in tests. */
fun phoneInUseFailure() = PlayerException(PlayerErrors.PHONE_IN_USE)
