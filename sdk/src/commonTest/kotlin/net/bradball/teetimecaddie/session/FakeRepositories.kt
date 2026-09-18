package net.bradball.teetimecaddie.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import net.bradball.teetimecaddie.core.models.Player
import net.bradball.teetimecaddie.features.auth.AuthUser
import net.bradball.teetimecaddie.features.auth.AuthRepository
import net.bradball.teetimecaddie.features.players.PlayerRepository

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

    var signInResult: Result<AuthUser> = Result.success(AuthUser("user-1", "dana@golf.app"))
    var createAccountResult: Result<AuthUser> = Result.success(AuthUser("user-1", "dana@golf.app"))
    var deleteShouldFail = false

    var deleteCount = 0
        private set
    var signOutCount = 0
        private set
    var displayNameUpdates = mutableListOf<String>()
        private set

    override val currentUser: AuthUser?
        get() = users.value

    override val authChanges: Flow<AuthUser?> = users

    override suspend fun signIn(email: String, password: String): AuthUser =
        signInResult.getOrThrow().also { users.value = it }

    override suspend fun createAccount(email: String, password: String): AuthUser =
        createAccountResult.getOrThrow().also { users.value = it }

    override suspend fun signOut() {
        signOutCount++
        users.value = null
    }

    override suspend fun deleteCurrentUser() {
        deleteCount++
        if (deleteShouldFail) throw IllegalStateException("stale session")
        users.value = null
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

    var createResult: ((String) -> Result<Player>)? = null

    override fun playerFlow(playerId: String): Flow<Player?> = store.map { it[playerId] }

    override suspend fun getPlayer(playerId: String): Player? = store.value[playerId]

    override suspend fun findPlayerByPhone(phone: String): Player? =
        store.value.values.firstOrNull { it.phone == phone }

    override suspend fun isPhoneInUse(phone: String, excludingPlayerId: String?): Boolean =
        findPlayerByPhone(phone)?.let { it.id != excludingPlayerId } ?: false

    override suspend fun createPlayer(
        playerId: String,
        name: String,
        email: String,
        phone: String,
        photo: ByteArray?
    ): Player {
        createResult?.let { return it(playerId).getOrThrow() }
        val player = Player(id = playerId, name = name, email = email, phone = phone)
        store.value = store.value + (playerId to player)
        return player
    }

    override suspend fun updatePlayer(player: Player): Player {
        store.value = store.value + (player.id to player)
        return player
    }
}
