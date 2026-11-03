package net.bradball.teetimecaddie.core.models

/**
 * The games a player's Games tab lists, from today on, earliest first.
 *
 * @property invitations Games the player was invited to and hasn't answered yet.
 * @property upcoming Games the player holds a slot in, or was invited to and has answered —
 *   whether they said yes or no.
 */
data class GamesTab(
    val invitations: List<Game>,
    val upcoming: List<Game>
)

val previewGamesTab = GamesTab(
    invitations = listOf(
        previewGameList[1].copy(
            organizerId = "previewOrganizer",
            invitees = listOf(previewGameInvitee.copy(name = previewPlayer.name, phone = previewPlayer.phone))
        )
    ),
    upcoming = listOf(previewGameList[0], previewGameList[2])
)
