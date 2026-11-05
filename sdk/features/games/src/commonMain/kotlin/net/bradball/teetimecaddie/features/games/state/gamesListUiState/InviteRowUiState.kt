package net.bradball.teetimecaddie.features.games.state.gamesListUiState

import net.bradball.teetimecaddie.core.models.AvatarTone
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.core.models.previewGameInvitee
import net.bradball.teetimecaddie.core.models.previewGameList
import net.bradball.teetimecaddie.core.models.previewGamePlayerList
import net.bradball.teetimecaddie.core.models.previewPlayer
import net.bradball.teetimecaddie.features.games.dateAndTime

/**
 * One invitation waiting for the player's answer, already formatted.
 *
 * @property id The game's id, for opening its details and for answering.
 * @property course The course name.
 * @property dateAndTime The date and first tee time, e.g. "Sat, Jun 13 · 9:00 AM".
 * @property players The avatars of everyone holding a slot, in slot order.
 * @property isFull Whether confirmed players already fill every slot. A full invitation shows a
 *   "Full" chip, and its Yes is disabled; No still works.
 */
data class InviteRowUiState(
    val id: String,
    val course: String,
    val dateAndTime: LocalizedText,
    val players: List<PlayerAvatarUiState>,
    val isFull: Boolean
)

internal fun Game.toInviteRowUiState(): InviteRowUiState {
    // Every game has exactly one tee time for now, and it is the one an invitee joins.
    val teeTime = teeTimes.firstOrNull()
    return InviteRowUiState(
        id = id,
        course = course.name,
        dateAndTime = dateAndTime(),
        players = teeTime?.players.orEmpty().map { player ->
            PlayerAvatarUiState(
                initial = player.initial,
                tone = if (player.playerId == organizerId) AvatarTone.SECONDARY else AvatarTone.forName(player.name)
            )
        },
        isFull = teeTime == null || teeTime.openSlots == 0
    )
}

/** An invitation to a game with three of its four slots taken. */
val previewInviteRowUiState = previewGameList[1]
    .copy(
        teeTimes = listOf(previewGameList[1].teeTimes.single().copy(slots = 4, players = previewGamePlayerList.take(3))),
        invitees = listOf(previewGameInvitee.copy(phone = previewPlayer.phone))
    )
    .toInviteRowUiState()

/** An invitation to a game whose slots are all taken. */
val previewFullInviteRowUiState = previewInviteRowUiState.copy(id = "previewFullInvite", isFull = true)

val previewInviteRowUiStateList = listOf(
    previewInviteRowUiState,
    previewFullInviteRowUiState.copy(course = previewGameList[2].course.name)
)
