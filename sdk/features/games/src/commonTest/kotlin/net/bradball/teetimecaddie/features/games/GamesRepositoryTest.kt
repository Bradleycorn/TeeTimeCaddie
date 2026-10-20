package net.bradball.teetimecaddie.features.games

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.LocalTime
import net.bradbal.teetimecaddie.core.storage.documents.CourseDocument
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.analytics.EventPlugin
import net.bradball.teetimecaddie.core.extensions.today
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.TtcResult
import net.bradball.teetimecaddie.core.models.previewPlayer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GamesRepositoryTest {

    // Relative to the real clock, because upcomingGames only returns games from today on.
    private val gameDate = LocalDate.today.plus(DatePeriod(days = 1))
    private val teeTime = LocalTime(9, 0)
    private val organizer = previewPlayer

    private val pebbleBeach = CourseDocument(name = "Pebble Beach", nameKey = "pebble beach")

    private val events = mutableListOf<AnalyticsEvent>()
    private val eventManager = EventManager.getInstance(enableLogging = false).apply {
        registerPlugin(object : EventPlugin {
            override fun logEvent(event: AnalyticsEvent): Boolean = events.add(event)
            override fun setUserId(userId: String) = Unit
        })
    }

    private fun repository(storage: FakeGameStorage) =
        GamesRepositoryImpl(eventManager, storage)

    private suspend fun GamesRepositoryImpl.publish(course: String = "Pebble Beach", slots: Int = 4) =
        publishGame(organizer, course, gameDate, teeTime, slots)

    private fun TtcResult<Game>.game(): Game = assertIs<TtcResult.Success<Game>>(this).data

    private fun TtcResult<Game>.error(): GameErrors =
        assertIs<GameException>(assertIs<TtcResult.Failure>(this).error).error

    // MARK: validation

    @Test
    fun publishGame_rejectsABlankCourseWithoutWriting() = runTest {
        val storage = FakeGameStorage()

        val result = repository(storage).publish(course = "")

        assertEquals(GameErrors.NO_COURSE, result.error())
        assertTrue(storage.writtenGames.isEmpty())
    }

    @Test
    fun publishGame_rejectsAWhitespaceOnlyCourse() = runTest {
        val storage = FakeGameStorage()

        val result = repository(storage).publish(course = "   ")

        assertEquals(GameErrors.NO_COURSE, result.error())
        assertTrue(storage.writtenGames.isEmpty())
    }

    @Test
    fun publishGame_rejectsSlotCountsOutsideOneToFour() = runTest {
        val storage = FakeGameStorage()
        val repo = repository(storage)

        assertEquals(GameErrors.INVALID_SLOTS, repo.publish(slots = 0).error())
        assertEquals(GameErrors.INVALID_SLOTS, repo.publish(slots = 5).error())
        assertTrue(storage.writtenGames.isEmpty())
    }

    @Test
    fun publishGame_acceptsTheSlotCountBoundaries() = runTest {
        val repo = repository(FakeGameStorage())

        assertEquals(1, repo.publish(slots = 1).game().teeTimes.single().slots)
        assertEquals(4, repo.publish(slots = 4).game().teeTimes.single().slots)
    }

    // MARK: courses

    @Test
    fun publishGame_reusesAnExistingCourseInADifferentCase() = runTest {
        val storage = FakeGameStorage(pebbleBeach)

        val game = repository(storage).publish(course = "pebble beach").game()

        assertEquals("Pebble Beach", game.course.name)
        assertEquals("pebble beach", game.course.id)
        assertEquals(listOf(pebbleBeach), storage.courses.values.toList())
    }

    @Test
    fun publishGame_ignoresSpacesAroundAnExistingCourseName() = runTest {
        val storage = FakeGameStorage(pebbleBeach)

        val game = repository(storage).publish(course = "  PEBBLE   beach ").game()

        assertEquals("Pebble Beach", game.course.name)
        assertEquals(1, storage.courses.size)
    }

    @Test
    fun publishGame_recordsANewCourseWithTheTrimmedSpelling() = runTest {
        val storage = FakeGameStorage(pebbleBeach)

        val game = repository(storage).publish(course = "  Bethpage Black ").game()

        assertEquals("Bethpage Black", game.course.name)
        assertEquals(
            CourseDocument(name = "Bethpage Black", nameKey = "bethpage black"),
            storage.courses["bethpage black"]
        )
    }

    // MARK: the published game

    @Test
    fun publishGame_putsTheOrganizerInTheFirstSlot() = runTest {
        val storage = FakeGameStorage()

        val game = repository(storage).publish(slots = 3).game()

        val slot = game.teeTimes.single()
        assertEquals(organizer.id, slot.players.single().playerId)
        assertEquals(organizer.name, slot.players.single().name)
        assertEquals(1, slot.filled)
        assertEquals(2, slot.openSlots)
        assertEquals(organizer.id, game.organizerId)
        assertEquals(listOf(organizer.id), storage.writtenGames.single().playerIds)
    }

    @Test
    fun publishGame_storesTheDateAndTeeTime() = runTest {
        val game = repository(FakeGameStorage()).publish().game()

        assertEquals(gameDate, game.date)
        assertEquals(teeTime, game.teeTimes.single().time)
    }

    @Test
    fun publishGame_logsThePublishEvent() = runTest {
        repository(FakeGameStorage()).publish(slots = 2)

        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.PublishGame(2)), events)
    }

    @Test
    fun publishGame_reportsAStorageFailure() = runTest {
        val storage = FakeGameStorage().apply { failure = IllegalStateException("offline") }

        val result = repository(storage).publish()

        assertEquals(GameErrors.PUBLISH_FAILED, result.error())
        assertTrue(events.isEmpty())
    }

    // MARK: reading games

    @Test
    fun getGame_readsAPublishedGame() = runTest {
        val repo = repository(FakeGameStorage())
        val published = repo.publish().game()

        assertEquals(published, repo.getGame(published.id).game())
    }

    @Test
    fun getGame_reportsAMissingGameAsNotFound() = runTest {
        val result = repository(FakeGameStorage()).getGame("nope")

        assertEquals(GameErrors.NOT_FOUND, result.error())
    }

    @Test
    fun getGame_reportsAReadFailure() = runTest {
        val storage = FakeGameStorage().apply { failure = IllegalStateException("offline") }

        val result = repository(storage).getGame("game-1")

        assertEquals(GameErrors.LOAD_FAILED, result.error())
    }

    @Test
    fun upcomingGames_emitsThePlayersGamesAsModels() = runTest {
        val repo = repository(FakeGameStorage())
        val published = repo.publish().game()

        assertEquals(listOf(published), repo.upcomingGames(organizer.id).first())
        assertEquals(emptyList(), repo.upcomingGames("someone-else").first())
    }

    @Test
    fun upcomingGames_endsQuietlyWhenTheReadFails() = runTest {
        val storage = FakeGameStorage().apply { failure = IllegalStateException("no index") }

        val emissions = repository(storage).upcomingGames(organizer.id).toList()

        assertTrue(emissions.isEmpty())
    }
}
