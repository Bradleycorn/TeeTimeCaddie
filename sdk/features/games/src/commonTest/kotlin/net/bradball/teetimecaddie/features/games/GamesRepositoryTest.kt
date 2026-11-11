package net.bradball.teetimecaddie.features.games

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.LocalTime
import net.bradbal.teetimecaddie.core.storage.documents.CourseDocument
import net.bradbal.teetimecaddie.core.storage.documents.GameDocument
import net.bradbal.teetimecaddie.core.storage.documents.GameInviteeDocument
import net.bradbal.teetimecaddie.core.storage.documents.GamePlayerDocument
import net.bradbal.teetimecaddie.core.storage.documents.GameTeeTimeDocument
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.analytics.EventPlugin
import net.bradball.teetimecaddie.core.extensions.today
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.GamePlayer
import net.bradball.teetimecaddie.core.models.GameInvitee
import net.bradball.teetimecaddie.core.models.InviteStatus
import net.bradball.teetimecaddie.core.models.NewInvitee
import net.bradball.teetimecaddie.core.models.Player
import net.bradball.teetimecaddie.core.models.TtcLookup
import net.bradball.teetimecaddie.core.models.TtcResult
import net.bradball.teetimecaddie.core.models.exceptions.TeeTimeCaddieException
import net.bradball.teetimecaddie.core.models.previewPlayer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GamesRepositoryTest {

    // Relative to the real clock, because observeGames only returns games from today on.
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

    // Players by their digits-only phone number, for the fake lookup.
    private val playersByPhone = mutableMapOf<String, Player>()
    private val lookedUpPhones = mutableListOf<String>()
    private var lookupFailure: TeeTimeCaddieException? = null

    private fun repository(storage: FakeGameStorage) =
        GamesRepositoryImpl(eventManager, storage) { phone ->
            lookedUpPhones += phone
            lookupFailure?.let { TtcLookup.Failure(it) } ?: TtcLookup.Success(playersByPhone[phone])
        }

    private suspend fun GamesRepositoryImpl.publish(
        course: String = "Pebble Beach",
        slots: Int = 4,
        invitees: List<NewInvitee> = emptyList()
    ) = publishGame(organizer, course, gameDate, teeTime, slots, invitees)

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

    // MARK: invitees

    @Test
    fun publishGame_recordsAMatchedNumberAsAnAppUserAndTheRestAsManaged() = runTest {
        val jeff = previewPlayer.copy(id = "jeff-uid", name = "Jeff Morrison", phone = "4155550142")
        playersByPhone[jeff.phone] = jeff
        val storage = FakeGameStorage()

        val game = repository(storage).publish(
            invitees = listOf(
                NewInvitee("Jeff Morrison", "+1 (415) 555-0142"),
                NewInvitee("Matt Chen", "415.555.0188")
            )
        ).game()

        assertEquals(
            listOf(
                GameInvitee("Jeff Morrison", "4155550142", "jeff-uid", InviteStatus.PENDING),
                GameInvitee("Matt Chen", "4155550188", null, InviteStatus.PENDING)
            ),
            game.invitees
        )
        val written = storage.writtenGames.single()
        assertEquals(listOf("4155550142", "4155550188"), written.invitedPhones)
        assertEquals(written.invitedPhones.toSet(), written.invitees.keys)
        // Invitees don't hold slots until they say yes.
        assertEquals(listOf(organizer.id), written.playerIds)
        assertEquals(1, game.teeTimes.single().filled)
    }

    @Test
    fun publishGame_allowsMoreInviteesThanSlots() = runTest {
        val invitees = (0..4).map { NewInvitee("Player $it", "415555010$it") }

        val game = repository(FakeGameStorage()).publish(slots = 2, invitees = invitees).game()

        assertEquals(5, game.invitees.size)
    }

    @Test
    fun publishGame_invitesARepeatedNumberOnceUnderTheFirstName() = runTest {
        val game = repository(FakeGameStorage()).publish(
            invitees = listOf(
                NewInvitee("Matt Chen", "(415) 555-0188"),
                NewInvitee("Matthew Chen", "4155550188")
            )
        ).game()

        assertEquals(listOf("Matt Chen"), game.invitees.map { it.name })
        assertEquals(listOf("4155550188"), lookedUpPhones)
    }

    @Test
    fun publishGame_skipsTheOrganizersOwnNumber() = runTest {
        val game = repository(FakeGameStorage()).publish(
            invitees = listOf(NewInvitee("Me", organizer.phone), NewInvitee("Matt Chen", "4155550188"))
        ).game()

        assertEquals(listOf("Matt Chen"), game.invitees.map { it.name })
    }

    @Test
    fun publishGame_trimsInviteeNames() = runTest {
        val game = repository(FakeGameStorage())
            .publish(invitees = listOf(NewInvitee("  Matt Chen ", "4155550188")))
            .game()

        assertEquals("Matt Chen", game.invitees.single().name)
    }

    @Test
    fun publishGame_rejectsAnInviteeWithoutANameOrUsableNumber() = runTest {
        for (bad in listOf(NewInvitee(" ", "4155550188"), NewInvitee("Matt", "555-0188"))) {
            val storage = FakeGameStorage()

            val result = repository(storage).publish(invitees = listOf(bad))

            assertEquals(GameErrors.INVALID_INVITEE, result.error())
            assertTrue(storage.writtenGames.isEmpty())
        }
    }

    @Test
    fun publishGame_failsWithoutWritingWhenALookupFails() = runTest {
        lookupFailure = GameException(GameErrors.LOAD_FAILED)
        val storage = FakeGameStorage()

        val result = repository(storage).publish(invitees = listOf(NewInvitee("Matt", "4155550188")))

        assertEquals(GameErrors.PUBLISH_FAILED, result.error())
        assertTrue(storage.writtenGames.isEmpty())
        assertTrue(events.isEmpty())
    }

    @Test
    fun publishGame_withNoInviteesLooksNothingUp() = runTest {
        val game = repository(FakeGameStorage()).publish().game()

        assertEquals(emptyList(), game.invitees)
        assertTrue(lookedUpPhones.isEmpty())
    }

    // MARK: reading games

    @Test
    fun observeGame_readsAPublishedGame() = runTest {
        val repo = repository(FakeGameStorage())
        val published = repo.publish().game()

        assertEquals(published, repo.observeGame(published.id).first().game())
    }

    // MARK: invitations

    // Invited by phone after the game was published, so no invitee entry carries her id.
    private val dana = Player(id = "dana-uid", name = "Dana Park", email = "dana@golf.app", phone = "5025552345")
    private val sam = Player(id = "sam-uid", name = "Sam Ortiz", email = "sam@golf.app", phone = "5025553456")

    /**
     * A game [organizer] published, as storage holds it, inviting [invitees] by phone with each
     * entry's status. [players] hold its slots after the organizer.
     */
    private fun storedGame(
        id: String = "game-1",
        date: LocalDate = gameDate,
        slots: Int = 4,
        players: List<Player> = emptyList(),
        invitees: Map<Player, InviteStatus> = mapOf(dana to InviteStatus.PENDING)
    ) = GameDocument(
        organizerId = organizer.id,
        courseId = "pebble beach",
        courseName = "Pebble Beach",
        date = date,
        teeTimes = listOf(
            GameTeeTimeDocument(
                teeTime,
                slots,
                (listOf(organizer) + players).map { GamePlayerDocument(it.id, it.name) }
            )
        ),
        playerIds = listOf(organizer.id) + players.map { it.id },
        invitees = invitees.entries.mapIndexed { order, (player, status) ->
            val answeredBy = if (status == InviteStatus.PENDING) null else player.id
            player.phone to GameInviteeDocument(player.firstName, answeredBy, status.name, order)
        }.toMap(),
        invitedPhones = invitees.keys.map { it.phone },
        createdAt = null
    ).also { it.id = id }

    private suspend fun GamesRepositoryImpl.gameIds(player: Player) =
        observeGames(player).first().map { it.id }

    @Test
    fun observeGames_findsAnInvitationByPhoneEvenWithoutAPlayerIdOnTheInvitee() = runTest {
        val storage = FakeGameStorage().apply { seed(storedGame()) }

        assertEquals(listOf("game-1"), repository(storage).gameIds(dana))
    }

    @Test
    fun observeGames_includesGamesThePlayerHoldsASlotIn() = runTest {
        val storage = FakeGameStorage().apply { seed(storedGame()) }

        assertEquals(listOf("game-1"), repository(storage).gameIds(organizer))
    }

    @Test
    fun observeGames_listsAnAcceptedInvitationOnce() = runTest {
        // An accepted invitee is in both queries: they hold a slot, and their number is invited.
        val accepted = storedGame(players = listOf(dana), invitees = mapOf(dana to InviteStatus.ACCEPTED))
        val storage = FakeGameStorage().apply { seed(accepted) }

        assertEquals(listOf("game-1"), repository(storage).gameIds(dana))
    }

    @Test
    fun observeGames_keepsADeclinedGame() = runTest {
        val storage = FakeGameStorage().apply {
            seed(storedGame(invitees = mapOf(dana to InviteStatus.DECLINED)))
        }

        assertEquals(listOf("game-1"), repository(storage).gameIds(dana))
    }

    @Test
    fun observeGames_ordersEarliestFirstAcrossBothQueries() = runTest {
        val storage = FakeGameStorage().apply {
            seed(
                storedGame(id = "fifth-day", date = gameDate.plus(DatePeriod(days = 5))),
                storedGame(id = "third-day", date = gameDate.plus(DatePeriod(days = 3)), players = listOf(dana), invitees = emptyMap()),
                storedGame(id = "first-day", date = gameDate, invitees = mapOf(dana to InviteStatus.DECLINED)),
                storedGame(id = "second-day", date = gameDate.plus(DatePeriod(days = 1)))
            )
        }

        assertEquals(
            listOf("first-day", "second-day", "third-day", "fifth-day"),
            repository(storage).gameIds(dana)
        )
    }

    @Test
    fun observeGames_leavesOutGamesBeforeToday() = runTest {
        val storage = FakeGameStorage().apply {
            seed(storedGame(date = LocalDate.today.plus(DatePeriod(days = -1))))
        }

        assertEquals(emptyList(), repository(storage).gameIds(dana))
    }

    @Test
    fun observeGames_dropsTheDaysGamesAtMidnight() = runTest {
        val dates = MutableStateFlow(gameDate)
        val storage = FakeGameStorage().apply {
            seed(storedGame(id = "today"), storedGame(id = "tomorrow", date = gameDate.plus(DatePeriod(days = 1))))
        }
        val repo = GamesRepositoryImpl(eventManager, storage, dates) { TtcLookup.Success(null) }

        val emissions = mutableListOf<List<String>>()
        val collecting = launch(UnconfinedTestDispatcher(testScheduler)) {
            repo.observeGames(dana).collect { games -> emissions += games.map { it.id } }
        }
        dates.value = gameDate.plus(DatePeriod(days = 1))
        collecting.cancel()

        assertEquals(listOf(listOf("today", "tomorrow"), listOf("tomorrow")), emissions)
    }

    @Test
    fun observeGames_carriesOnWithoutInvitationsWhenTheyCantBeRead() = runTest {
        val storage = FakeGameStorage().apply {
            seed(storedGame(players = listOf(dana), invitees = emptyMap()), storedGame(id = "invited"))
            invitationsFailure = IllegalStateException("no index")
        }

        assertEquals(listOf("game-1"), repository(storage).gameIds(dana))
    }

    @Test
    fun observeGames_endsQuietlyWhenTheGamesCantBeRead() = runTest {
        val storage = FakeGameStorage().apply { failure = IllegalStateException("offline") }

        val emissions = repository(storage).observeGames(dana).toList()

        assertTrue(emissions.isEmpty())
    }

    @Test
    fun observeGame_emitsAgainWhenSomeoneAnswers() = runTest {
        val storage = FakeGameStorage().apply { seed(storedGame()) }
        val repo = repository(storage)

        val seen = mutableListOf<Int>()
        repo.observeGame("game-1").take(2).collect { result ->
            seen += result.game().teeTimes.single().filled
            if (seen.size == 1) repo.respondToInvite("game-1", dana, accept = true)
        }

        assertEquals(listOf(1, 2), seen)
    }

    @Test
    fun observeGame_reportsAMissingGameAsNotFound() = runTest {
        val result = repository(FakeGameStorage()).observeGame("nope").first()

        assertEquals(GameErrors.NOT_FOUND, result.error())
    }

    @Test
    fun observeGame_reportsAReadFailureAndEnds() = runTest {
        val storage = FakeGameStorage().apply { failure = IllegalStateException("offline") }

        val emissions = repository(storage).observeGame("game-1").toList()

        assertEquals(listOf(GameErrors.LOAD_FAILED), emissions.map { it.error() })
    }

    @Test
    fun respondToInvite_yesTakesTheNextSlotAndRecordsTheAnswer() = runTest {
        val storage = FakeGameStorage().apply { seed(storedGame()) }

        val game = repository(storage).respondToInvite("game-1", dana, accept = true).game()

        assertEquals(
            listOf(GamePlayer(organizer.id, organizer.name), GamePlayer(dana.id, dana.name)),
            game.teeTimes.single().players
        )
        assertEquals(GameInvitee("Dana", dana.phone, dana.id, InviteStatus.ACCEPTED), game.inviteeFor(dana.phone))
        assertEquals(listOf(organizer.id, dana.id), storage.writtenGames.single().playerIds)
        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.AnswerInvite(accepted = true)), events)
    }

    @Test
    fun respondToInvite_noRecordsTheAnswerWithoutTakingASlot() = runTest {
        val storage = FakeGameStorage().apply { seed(storedGame()) }

        val game = repository(storage).respondToInvite("game-1", dana, accept = false).game()

        assertEquals(1, game.teeTimes.single().filled)
        assertEquals(InviteStatus.DECLINED, game.inviteeFor(dana.phone)?.status)
        assertEquals(dana.id, game.inviteeFor(dana.phone)?.playerId)
        assertEquals(listOf(organizer.id), storage.writtenGames.single().playerIds)
        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.AnswerInvite(accepted = false)), events)
    }

    @Test
    fun respondToInvite_yesToAFullGameFailsAndLeavesTheInvitationPending() = runTest {
        val storage = FakeGameStorage().apply { seed(storedGame(slots = 2, players = listOf(sam))) }

        val result = repository(storage).respondToInvite("game-1", dana, accept = true)

        assertEquals(GameErrors.GAME_FULL, result.error())
        assertEquals("PENDING", storage.writtenGames.single().invitees.getValue(dana.phone).status)
        assertTrue(events.isEmpty())
    }

    @Test
    fun respondToInvite_noToAFullGameStillWorks() = runTest {
        val storage = FakeGameStorage().apply { seed(storedGame(slots = 2, players = listOf(sam))) }

        val game = repository(storage).respondToInvite("game-1", dana, accept = false).game()

        assertEquals(InviteStatus.DECLINED, game.inviteeFor(dana.phone)?.status)
    }

    @Test
    fun respondToInvite_theLastSlotGoesToTheFirstYes() = runTest {
        val storage = FakeGameStorage().apply {
            seed(storedGame(slots = 2, invitees = mapOf(dana to InviteStatus.PENDING, sam to InviteStatus.PENDING)))
        }
        val repo = repository(storage)

        repo.respondToInvite("game-1", dana, accept = true).game()
        val second = repo.respondToInvite("game-1", sam, accept = true)

        assertEquals(GameErrors.GAME_FULL, second.error())
        assertEquals(listOf(organizer.id, dana.id), storage.writtenGames.single().playerIds)
    }

    @Test
    fun respondToInvite_leavesAnAnsweredInvitationAsItIs() = runTest {
        val declined = storedGame(invitees = mapOf(dana to InviteStatus.DECLINED))
        val storage = FakeGameStorage().apply { seed(declined) }

        val game = repository(storage).respondToInvite("game-1", dana, accept = true).game()

        assertEquals(InviteStatus.DECLINED, game.inviteeFor(dana.phone)?.status)
        assertEquals(1, game.teeTimes.single().filled)
        assertTrue(events.isEmpty())
    }

    @Test
    fun respondToInvite_reportsSomeoneWhoWasNotInvitedAsNotFound() = runTest {
        val storage = FakeGameStorage().apply { seed(storedGame()) }

        val result = repository(storage).respondToInvite("game-1", sam, accept = true)

        assertEquals(GameErrors.NOT_FOUND, result.error())
        assertEquals(1, storage.writtenGames.single().teeTimes.single().players.size)
    }

    @Test
    fun respondToInvite_reportsAMissingGameAsNotFound() = runTest {
        val result = repository(FakeGameStorage()).respondToInvite("nope", dana, accept = true)

        assertEquals(GameErrors.NOT_FOUND, result.error())
    }

    @Test
    fun respondToInvite_reportsAWriteFailure() = runTest {
        val storage = FakeGameStorage().apply {
            seed(storedGame())
            failure = IllegalStateException("offline")
        }

        val result = repository(storage).respondToInvite("game-1", dana, accept = true)

        assertEquals(GameErrors.RESPOND_FAILED, result.error())
    }
}
