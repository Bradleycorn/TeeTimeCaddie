package net.bradball.teetimecaddie.features.games

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import net.bradbal.teetimecaddie.core.storage.GameStorage
import net.bradbal.teetimecaddie.core.storage.documents.CourseDocument
import net.bradbal.teetimecaddie.core.storage.documents.GameDocument
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.analytics.LoggableExceptionTypes
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.GamePlayer
import net.bradball.teetimecaddie.core.models.GameTeeTime
import net.bradball.teetimecaddie.core.models.Player
import net.bradball.teetimecaddie.core.models.TtcResult

/**
 * Games: publishing them, and reading them back.
 *
 * Like `PlayerRepository`, this takes players as parameters and knows nothing about who is signed
 * in — that is `SessionManager`'s concern.
 *
 * **Nothing here throws.** Failures come back as [TtcResult.Failure] carrying a [GameException].
 */
interface GamesRepository {

    /**
     * Publish a new game with a single tee time.
     *
     * The course is matched by name, ignoring case and extra spaces: a name matching an existing
     * course reuses it, keeping that course's stored spelling, and any other name records a new
     * course. The organizer automatically takes the tee time's first slot.
     *
     * @param organizer The player publishing the game.
     * @param courseName The course as typed. Leading and trailing spaces are ignored.
     * @param slots How many players the tee time holds, from 1 to 4.
     * @return The published game, carrying the stored course spelling. [GameErrors.NO_COURSE] for
     *   a blank course and [GameErrors.INVALID_SLOTS] for an out-of-range slot count, both reported
     *   without writing anything.
     */
    suspend fun publishGame(
        organizer: Player,
        courseName: String,
        date: LocalDate,
        time: LocalTime,
        slots: Int
    ): TtcResult<Game>

    /**
     * Observe the games a player holds a slot in, from today on, earliest first.
     *
     * A flow, so it reports values rather than results; a read failure is logged and simply
     * means no further emission.
     */
    fun upcomingGames(playerId: String): Flow<List<Game>>

    /**
     * Read a single game.
     *
     * A [TtcResult] rather than a `TtcLookup`, because the id came from somewhere that said the
     * game exists: a missing game is [GameErrors.NOT_FOUND].
     */
    suspend fun getGame(gameId: String): TtcResult<Game>
}

/** The most players a tee time can hold. */
const val MAX_TEE_TIME_SLOTS = 4

class GamesRepositoryImpl(
    private val eventManager: EventManager,
    private val gameStorage: GameStorage
) : GamesRepository {

    override suspend fun publishGame(
        organizer: Player,
        courseName: String,
        date: LocalDate,
        time: LocalTime,
        slots: Int
    ): TtcResult<Game> {
        if (courseName.isBlank()) return TtcResult.Failure(GameException(GameErrors.NO_COURSE))
        if (slots !in 1..MAX_TEE_TIME_SLOTS) {
            return TtcResult.Failure(GameException(GameErrors.INVALID_SLOTS))
        }

        val key = courseKey(courseName)
        val newCourse = CourseDocument(name = cleanCourseName(courseName), nameKey = key)
        val teeTime = GameTeeTime(
            time = time,
            slots = slots,
            players = listOf(GamePlayer(playerId = organizer.id, name = organizer.name))
        )

        return runStorage("publish_game", GameErrors.PUBLISH_FAILED) {
            val game = gameStorage.publishGame(key, newCourse) { course ->
                GameDocument(
                    organizerId = organizer.id,
                    courseId = course.nameKey,
                    courseName = course.name,
                    date = date,
                    teeTimes = listOf(teeTime.toDocument()),
                    playerIds = listOf(organizer.id)
                )
            }
            TtcResult.Success(game.toModel())
        }.also {
            if (it is TtcResult.Success) eventManager.logEvent(AnalyticsEvent.PublishGame(slots))
        }
    }

    override fun upcomingGames(playerId: String): Flow<List<Game>> =
        gameStorage.gamesFlow(playerId, systemToday())
            .map { documents -> documents.map { it.toModel() } }
            .catch { ex ->
                eventManager.logException(
                    ex, LoggableExceptionTypes.GAMES, hashMapOf("action" to "upcoming_games")
                )
            }

    override suspend fun getGame(gameId: String): TtcResult<Game> =
        runStorage("get_game", GameErrors.LOAD_FAILED) {
            val document = gameStorage.getGame(gameId)
            if (document == null) {
                TtcResult.Failure(GameException(GameErrors.NOT_FOUND))
            } else {
                TtcResult.Success(document.toModel())
            }
        }

    /**
     * Run a storage call, turning anything it throws into a [failure] result. [block] reports its
     * own outcomes, such as a read that found nothing.
     *
     * The storage layer still throws — it wraps Firestore — so this is the boundary where those
     * exceptions stop and become values.
     */
    private suspend inline fun <T : Any> runStorage(
        action: String,
        failure: GameErrors,
        block: () -> TtcResult<T>
    ): TtcResult<T> = try {
        block()
    } catch (ex: Exception) {
        // Re-throws if this coroutine was cancelled, so cancellation is not swallowed into a
        // Failure. See the note in AuthRepository for why this beats catching the type.
        currentCoroutineContext().ensureActive()
        eventManager.logException(ex, LoggableExceptionTypes.GAMES, hashMapOf("action" to action))
        TtcResult.Failure(GameException(failure, cause = ex))
    }
}

@OptIn(ExperimentalTime::class)
private fun systemToday(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
