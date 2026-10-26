package net.bradball.teetimecaddie.features.games

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import net.bradbal.teetimecaddie.core.storage.GameStorage
import net.bradbal.teetimecaddie.core.storage.documents.CourseDocument
import net.bradbal.teetimecaddie.core.storage.documents.GameDocument
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.analytics.LoggableExceptionTypes
import net.bradball.teetimecaddie.core.extensions.toPhoneDigits
import net.bradball.teetimecaddie.core.extensions.today
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.GameInvitee
import net.bradball.teetimecaddie.core.models.GamePlayer
import net.bradball.teetimecaddie.core.models.GameTeeTime
import net.bradball.teetimecaddie.core.models.InviteStatus
import net.bradball.teetimecaddie.core.models.NewInvitee
import net.bradball.teetimecaddie.core.models.Player
import net.bradball.teetimecaddie.core.models.TtcLookup
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
     * Each invitee's number is normalized and matched against players' numbers: a match is
     * recorded as an app-user invitee, anyone else as an invitee the organizer manages. The
     * caller never says which is which. A number listed twice is invited once, under the first
     * name it came with, and the organizer's own number is skipped — they are already playing.
     *
     * @param organizer The player publishing the game.
     * @param courseName The course as typed. Leading and trailing spaces are ignored.
     * @param slots How many players the tee time holds, from 1 to 4.
     * @param invitees The people to invite, in the order they were added. May outnumber the slots.
     * @return The published game, carrying the stored course spelling. [GameErrors.NO_COURSE] for
     *   a blank course, [GameErrors.INVALID_SLOTS] for an out-of-range slot count and
     *   [GameErrors.INVALID_INVITEE] for an invitee without a name or a usable number, all reported
     *   without writing anything.
     */
    suspend fun publishGame(
        organizer: Player,
        courseName: String,
        date: LocalDate,
        time: LocalTime,
        slots: Int,
        invitees: List<NewInvitee>
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

// The digit counts the `players` and `games` rules accept for a stored phone number.
private val STORED_PHONE_LENGTHS = 10..15

/**
 * @param findPlayerByPhone Looks up the player who owns a phone number, as
 *   `PlayerRepository.findPlayerByPhone` does. A function rather than the repository so this
 *   module does not depend on another feature module.
 */
class GamesRepositoryImpl(
    private val eventManager: EventManager,
    private val gameStorage: GameStorage,
    private val findPlayerByPhone: suspend (phone: String) -> TtcLookup<Player>
) : GamesRepository {

    override suspend fun publishGame(
        organizer: Player,
        courseName: String,
        date: LocalDate,
        time: LocalTime,
        slots: Int,
        invitees: List<NewInvitee>
    ): TtcResult<Game> {
        if (courseName.isBlank()) return TtcResult.Failure(GameException(GameErrors.NO_COURSE))
        if (slots !in 1..MAX_TEE_TIME_SLOTS) {
            return TtcResult.Failure(GameException(GameErrors.INVALID_SLOTS))
        }
        val cleanInvitees = invitees.map { it.name.trim() to it.phone.toPhoneDigits() }
        if (cleanInvitees.any { (name, phone) -> name.isEmpty() || phone.length !in STORED_PHONE_LENGTHS }) {
            return TtcResult.Failure(GameException(GameErrors.INVALID_INVITEE))
        }
        val gameInvitees = when (val classified = classify(organizer, cleanInvitees)) {
            is TtcResult.Success -> classified.data
            is TtcResult.Failure -> return classified
        }

        val key = courseKey(courseName)
        val newCourse = CourseDocument(name = cleanCourseName(courseName), nameKey = key)
        val teeTime = GameTeeTime(
            time = time,
            slots = slots,
            players = listOf(GamePlayer(playerId = organizer.id, name = organizer.name))
        )

        return tryStorage("publish_game", GameErrors.PUBLISH_FAILED) {
            val game = gameStorage.publishGame(key, newCourse) { course ->
                GameDocument(
                    organizerId = organizer.id,
                    courseId = course.nameKey,
                    courseName = course.name,
                    date = date,
                    teeTimes = listOf(teeTime.toDocument()),
                    playerIds = listOf(organizer.id),
                    invitees = gameInvitees.map { it.toDocument() },
                    invitedPhones = gameInvitees.map { it.phone }
                )
            }
            TtcResult.Success(game.toModel())
        }.also {
            if (it is TtcResult.Success) eventManager.logEvent(AnalyticsEvent.PublishGame(slots))
        }
    }

    override fun upcomingGames(playerId: String): Flow<List<Game>> =
        gameStorage.gamesFlow(playerId, LocalDate.today)
            .map { documents -> documents.map { it.toModel() } }
            .catch { ex ->
                eventManager.logException(
                    ex, LoggableExceptionTypes.GAMES, hashMapOf("action" to "upcoming_games")
                )
            }

    override suspend fun getGame(gameId: String): TtcResult<Game> =
        tryStorage("get_game", GameErrors.LOAD_FAILED) {
            val document = gameStorage.getGame(gameId)
            if (document == null) {
                TtcResult.Failure(GameException(GameErrors.NOT_FOUND))
            } else {
                TtcResult.Success(document.toModel())
            }
        }

    /**
     * Turn the cleaned invitees into [GameInvitee]s, recording which numbers belong to a player.
     *
     * One lookup per number: the `players` rules allow only single-result queries. Done before the
     * publish transaction, which cannot run queries. A failed lookup fails the publish rather
     * than recording an app user as someone the organizer manages.
     */
    private suspend fun classify(
        organizer: Player,
        invitees: List<Pair<String, String>>
    ): TtcResult<List<GameInvitee>> {
        val unique = invitees
            .distinctBy { (_, phone) -> phone }
            .filter { (_, phone) -> phone != organizer.phone.toPhoneDigits() }

        return TtcResult.Success(
            unique.map { (name, phone) ->
                val playerId = when (val match = findPlayerByPhone(phone)) {
                    is TtcLookup.Success -> match.data?.id
                    is TtcLookup.Failure -> return TtcResult.Failure(
                        GameException(GameErrors.PUBLISH_FAILED, cause = match.error as? Exception)
                    )
                }
                GameInvitee(name = name, phone = phone, playerId = playerId, status = InviteStatus.PENDING)
            }
        )
    }

    /**
     * Run a storage call, turning anything it throws into a [failure] result. [block] reports its
     * own outcomes, such as a read that found nothing.
     *
     * The storage layer still throws — it wraps Firestore — so this is the boundary where those
     * exceptions stop and become values.
     */
    private suspend inline fun <T : Any> tryStorage(
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
