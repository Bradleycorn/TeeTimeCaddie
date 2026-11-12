package net.bradball.teetimecaddie.features.games.state.gameDetailsUiState

import net.bradball.teetimecaddie.core.extensions.formattedTime
import net.bradball.teetimecaddie.core.models.GameInvitee
import net.bradball.teetimecaddie.core.models.GameTeeTime
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.core.models.previewGameInvitee
import net.bradball.teetimecaddie.core.models.previewGameTeeTime
import net.bradball.teetimecaddie.features.games.GMR

/**
 * The sheet the organizer opens from an invitee's row to record a Yes or No on their behalf, as it
 * stands while it's open. See [GameUiState.Organizer.responseSheet].
 *
 * Rebuilt from the game each time it changes, so it keeps up while it's open: if the tee time
 * fills, Yes turns off on its own.
 *
 * @property inviteePhone The number the invitee was invited by, digits only. Pass it back to
 *   `GamesRepository.recordResponse`.
 * @property title "Dana's response", with the name they were invited by.
 * @property prompt "Record a reply on Dana's behalf for the 9:00 AM tee time."
 * @property yesDetail Under Yes: "Playing at 9:00 AM", or "9:00 AM is full" once Yes answers fill
 *   every slot.
 * @property canAccept False once Yes answers fill every slot. Yes is then disabled; No still works.
 * @property noDetail Under No: "Can't make it".
 */
data class ResponseSheetUiState(
    val inviteePhone: String,
    val title: LocalizedText,
    val prompt: LocalizedText,
    val yesDetail: LocalizedText,
    val canAccept: Boolean,
    val noDetail: LocalizedText = LocalizedText(GMR.strings.game_details_response_no_detail),
    private val inviteeName: String
) {
    /** The confirmation once the answer is saved: "Dana marked as Yes" or "Dana marked as No". */
    fun recordedMessage(accepted: Boolean): LocalizedText = LocalizedText(
        if (accepted) GMR.strings.game_details_response_recorded_yes else GMR.strings.game_details_response_recorded_no,
        listOf(inviteeName)
    )
}

/** The response sheet for [invitee], who would join [teeTime] with a Yes. */
internal fun responseSheetFor(invitee: GameInvitee, teeTime: GameTeeTime): ResponseSheetUiState {
    val time = teeTime.time.formattedTime
    val canAccept = teeTime.openSlots > 0
    return ResponseSheetUiState(
        inviteePhone = invitee.phone,
        title = LocalizedText(GMR.strings.game_details_response_title, listOf(invitee.name)),
        prompt = LocalizedText(GMR.strings.game_details_response_prompt, listOf(invitee.name, time)),
        yesDetail = LocalizedText(
            if (canAccept) GMR.strings.game_details_response_yes_detail else GMR.strings.game_details_response_yes_full,
            listOf(time)
        ),
        canAccept = canAccept,
        inviteeName = invitee.name
    )
}

/** The response sheet for [previewGameInvitee], with a slot still open for a Yes. */
val previewResponseSheetUiState = responseSheetFor(previewGameInvitee, previewGameTeeTime)

/** The response sheet once Yes answers fill every slot: Yes is off, and says the tee time is full. */
val previewFullResponseSheetUiState = responseSheetFor(previewGameInvitee, previewGameTeeTime.copy(slots = 1))
