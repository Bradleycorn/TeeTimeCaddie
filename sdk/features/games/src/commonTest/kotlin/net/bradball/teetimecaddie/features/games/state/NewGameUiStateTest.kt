package net.bradball.teetimecaddie.features.games.state

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.core.models.previewNewInvitee
import net.bradball.teetimecaddie.features.games.GMR
import net.bradball.teetimecaddie.features.games.MAX_TEE_TIME_SLOTS
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NewGameUiStateTest {

    private val date = LocalDate(2026, 6, 13)
    private val time = LocalTime(9, 0)

    @Test
    fun theFormStartsEmptyWithEverySlot() {
        assertEquals(NewGameUiState(), NewGameUiState.defaultValue)
        assertEquals(MAX_TEE_TIME_SLOTS, NewGameUiState.defaultValue.slots)
    }

    @Test
    fun canSubmitNeedsACourseADateAndATime() {
        assertFalse(NewGameUiState().canSubmit)
        assertFalse(NewGameUiState(course = "Pebble Beach", date = date).canSubmit)
        assertFalse(NewGameUiState(course = "Pebble Beach", time = time).canSubmit)
        assertFalse(NewGameUiState(date = date, time = time).canSubmit)
        assertTrue(NewGameUiState(course = "Pebble Beach", date = date, time = time).canSubmit)
    }

    @Test
    fun canSubmitIgnoresACourseOfOnlySpaces() {
        assertFalse(NewGameUiState(course = "   ", date = date, time = time).canSubmit)
    }

    @Test
    fun inviteesNeverAffectCanSubmit() {
        assertFalse(NewGameUiState(invitees = listOf(previewNewInvitee)).canSubmit)
        assertTrue(
            NewGameUiState(course = "Pebble Beach", date = date, time = time, invitees = listOf(previewNewInvitee))
                .canSubmit
        )
    }

    @Test
    fun canSubmitIsFalseWhilePublishing() {
        assertFalse(
            NewGameUiState(course = "Pebble Beach", date = date, time = time, isSubmitting = true).canSubmit
        )
    }

    @Test
    fun chosenValuesReadAsTheyAreDisplayed() {
        val state = NewGameUiState(date = date, time = time)

        assertEquals("Sat, Jun 13", state.dateText)
        assertEquals("9:00 AM", state.timeText)
    }

    @Test
    fun nothingIsDisplayedUntilChosen() {
        assertNull(NewGameUiState().dateText)
        assertNull(NewGameUiState().timeText)
    }

    @Test
    fun addFromContactsReadsAddMoreOnceAnyoneIsAdded() {
        assertEquals(GMR.strings.new_game_add_from_contacts, NewGameUiState().addFromContactsLabel)
        assertEquals(
            GMR.strings.new_game_add_more_from_contacts,
            NewGameUiState(invitees = listOf(previewNewInvitee)).addFromContactsLabel
        )
    }
}
