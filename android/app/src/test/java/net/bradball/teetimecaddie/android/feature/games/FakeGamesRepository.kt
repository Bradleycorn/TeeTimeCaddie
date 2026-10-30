package net.bradball.teetimecaddie.android.feature.games

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.core.models.Course
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.GameInvitee
import net.bradball.teetimecaddie.core.models.GamePlayer
import net.bradball.teetimecaddie.core.models.GameTeeTime
import net.bradball.teetimecaddie.core.models.InviteStatus
import net.bradball.teetimecaddie.core.models.NewInvitee
import net.bradball.teetimecaddie.core.models.Player
import net.bradball.teetimecaddie.core.models.TtcResult
import net.bradball.teetimecaddie.features.games.GameErrors
import net.bradball.teetimecaddie.features.games.GameException
import net.bradball.teetimecaddie.features.games.GamesRepository

/**
 * A hand-written [GamesRepository] for ViewModel tests.
 *
 * It does not reproduce the SDK's rules (those are tested in the SDK); it records what it was
 * asked and answers with whatever the test configured.
 */
class FakeGamesRepository(vararg games: Game) : GamesRepository {

    private val games = MutableStateFlow(games.toList())

    /** Each publish request, in order. */
    val publishRequests = mutableListOf<PublishRequest>()

    /** When set, [publishGame] fails with this instead of publishing. */
    var publishFailure: GameErrors? = null

    /** When set, [publishGame] waits for this before answering, so a test can see it in flight. */
    var publishGate: CompletableDeferred<Unit>? = null

    /** The stored spelling a published course comes back with. Defaults to the name as typed. */
    var storedCourseName: ((String) -> String) = { it.trim() }

    data class PublishRequest(
        val organizer: Player,
        val courseName: String,
        val date: LocalDate,
        val time: LocalTime,
        val slots: Int,
        val invitees: List<NewInvitee> = emptyList(),
    )

    fun setGames(vararg updated: Game) {
        games.value = updated.toList()
    }

    override suspend fun publishGame(
        organizer: Player,
        courseName: String,
        date: LocalDate,
        time: LocalTime,
        slots: Int,
        invitees: List<NewInvitee>,
    ): TtcResult<Game> {
        publishRequests += PublishRequest(organizer, courseName, date, time, slots, invitees)
        publishGate?.await()
        publishFailure?.let { return TtcResult.Failure(GameException(it)) }

        val name = storedCourseName(courseName)
        val game = Game(
            id = "game-${publishRequests.size}",
            organizerId = organizer.id,
            course = Course(id = name.lowercase(), name = name),
            date = date,
            teeTimes = listOf(
                GameTeeTime(time, slots, listOf(GamePlayer(organizer.id, organizer.name)))
            ),
            invitees = invitees.map {
                GameInvitee(it.name, it.phone, playerId = null, status = InviteStatus.PENDING)
            },
        )
        games.value = games.value + game
        return TtcResult.Success(game)
    }

    override fun upcomingGames(playerId: String): Flow<List<Game>> =
        games.map { all -> all.filter { game -> game.teeTimes.any { t -> t.players.any { it.playerId == playerId } } } }

    /** How many times [getGame] was called. */
    var getGameCalls = 0
        private set

    override suspend fun getGame(gameId: String): TtcResult<Game> =
        also { getGameCalls++ }.games.value.firstOrNull { it.id == gameId }
            ?.let { TtcResult.Success(it) }
            ?: TtcResult.Failure(GameException(GameErrors.NOT_FOUND))
}
