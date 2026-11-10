package net.bradball.teetimecaddie.features.games.state.gameDetailsUiState

/**
 * One row of the organizer's Declined section: someone who said no to their invitation.
 *
 * @property initial The letter in the person's avatar.
 * @property name The name they were invited by.
 */
data class DeclinedInviteeUiState(
    val initial: String,
    val name: String
)
