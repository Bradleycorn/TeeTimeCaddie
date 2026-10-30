package net.bradball.teetimecaddie.features.games.state

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.core.extensions.formattedTime
import net.bradball.teetimecaddie.core.extensions.weekdayMonthDay
import net.bradball.teetimecaddie.features.games.MAX_TEE_TIME_SLOTS

/**
 * State of the New game form.
 *
 * A data class rather than a sealed hierarchy, because its parts are independent: the confirmation
 * opens over a filled-in form, and a failed publish leaves the form as it was.
 *
 * @property showNoInviteeConfirm Whether the "Publish with no invitees?" alert is up. Until
 *   invitees arrive (TTC-69) every publish asks.
 */
data class NewGameUiState(
    val course: String = "",
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val slots: Int = MAX_TEE_TIME_SLOTS,
    val showNoInviteeConfirm: Boolean = false,
    val isSubmitting: Boolean = false,
) {
    /** A course of only spaces doesn't count. Slots never block publishing; they start at 4. */
    val canSubmit: Boolean
        get() = course.isNotBlank() && date != null && time != null && !isSubmitting

    /** The chosen date as displayed, e.g. "Sat, Jun 13", or null until one is chosen. */
    val dateText: String?
        get() = date?.weekdayMonthDay

    /** The chosen tee time as displayed, e.g. "9:00 AM", or null until one is chosen. */
    val timeText: String?
        get() = time?.formattedTime

    companion object {
        /** An empty form, for callers that can't use the constructor's default arguments (Swift). */
        val defaultValue = NewGameUiState()
    }
}

/** A filled-in form: Pebble Beach, Sat, Jun 13 at 9:00 AM, for three players. */
val previewNewGameUiState = NewGameUiState(
    course = "Pebble Beach",
    date = LocalDate(2026, 6, 13),
    time = LocalTime(9, 0),
    slots = 3
)
