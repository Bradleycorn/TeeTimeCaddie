package net.bradball.teetimecaddie.features.games

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import net.bradbal.teetimecaddie.core.storage.documents.GameDocument
import net.bradbal.teetimecaddie.core.storage.documents.GamePlayerDocument
import net.bradbal.teetimecaddie.core.storage.documents.GameTeeTimeDocument
import net.bradball.teetimecaddie.core.models.Course
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GameExtensionsTest {

    // MARK: courseKey

    @Test
    fun courseKey_ignoresCase() {
        assertEquals(courseKey("Pebble Beach"), courseKey("pEBBLE bEACH"))
    }

    @Test
    fun courseKey_ignoresLeadingTrailingAndRepeatedSpaces() {
        assertEquals("pebble beach", courseKey("  Pebble \t  Beach  "))
    }

    @Test
    fun courseKey_replacesSlashesSoItIsAValidDocumentId() {
        assertEquals("torrey pines n-s", courseKey("Torrey Pines N/S"))
    }

    @Test
    fun cleanCourseName_keepsCaseButTidiesSpaces() {
        assertEquals("Pebble Beach", cleanCourseName("  Pebble   Beach "))
    }

    // MARK: mappers

    private val organizer = GamePlayerDocument(playerId = "brad", name = "Brad Ball")

    private fun document(vararg teeTimes: GameTeeTimeDocument) = GameDocument(
        organizerId = "brad",
        courseId = "pebble beach",
        courseName = "Pebble Beach",
        date = LocalDate(2026, 6, 13),
        teeTimes = teeTimes.toList(),
        playerIds = listOf("brad"),
        invitees = emptyList(),
        invitedPhones = emptyList(),
        createdAt = null
    ).also { it.id = "game-1" }

    @Test
    fun gameDocument_toModel_carriesTheCourseAndOrganizer() {
        val game = document(GameTeeTimeDocument(LocalTime(9, 0), 4, listOf(organizer))).toModel()

        assertEquals("game-1", game.id)
        assertEquals(Course(id = "pebble beach", name = "Pebble Beach"), game.course)
        assertEquals("brad", game.organizerId)
        assertEquals("Brad Ball", game.teeTimes.single().players.single().name)
        assertEquals(1, game.teeTimes.single().filled)
        assertEquals(3, game.teeTimes.single().openSlots)
    }

    @Test
    fun gameDocument_toModel_ordersTeeTimesEarliestFirst() {
        val game = document(
            GameTeeTimeDocument(LocalTime(9, 10), 4, emptyList()),
            GameTeeTimeDocument(LocalTime(9, 0), 4, listOf(organizer))
        ).toModel()

        assertEquals(listOf(LocalTime(9, 0), LocalTime(9, 10)), game.teeTimes.map { it.time })
    }

    @Test
    fun gameDocument_toModel_requiresAnId() {
        val withoutId = document().also { it.id = null }

        assertFailsWith<IllegalArgumentException> { withoutId.toModel() }
    }

    @Test
    fun gameTeeTime_roundTripsThroughItsDocument() {
        val teeTimeDocument = GameTeeTimeDocument(LocalTime(7, 45), 2, listOf(organizer))

        assertEquals(teeTimeDocument, teeTimeDocument.toModel().toDocument())
    }
}
