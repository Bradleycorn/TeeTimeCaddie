package net.bradball.teetimecaddie.features.games.state

import net.bradball.teetimecaddie.core.extensions.formattedTime
import net.bradball.teetimecaddie.core.extensions.toPhoneDigits
import net.bradball.teetimecaddie.core.extensions.weekdayMonthDay
import net.bradball.teetimecaddie.core.models.AvatarTone
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.InviteStatus
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.core.models.Player
import net.bradball.teetimecaddie.core.models.previewGame
import net.bradball.teetimecaddie.core.models.previewGameInvitee
import net.bradball.teetimecaddie.core.models.previewGameList
import net.bradball.teetimecaddie.core.models.previewGamePlayerList
import net.bradball.teetimecaddie.core.models.previewPlayer
import net.bradball.teetimecaddie.features.games.GMR

/**
 * State for the Games tab.
 *
 * A sealed hierarchy rather than a data class, because the three cases never occur together: the
 * tab shows a spinner, the empty state, or the list.
 */
sealed interface GamesListUiState {
    data object Loading : GamesListUiState
    data object Empty : GamesListUiState

    /**
     * @property games The upcoming games, earliest first: those the player holds a slot in, and
     *   those they were invited to and have answered.
     * @property invites The invitations still waiting for the player's answer, earliest first.
     *   Shown together in one card above [games].
     */
    data class Content(
        val games: List<GameRowUiState>,
        val invites: List<InviteRowUiState> = emptyList()
    ) : GamesListUiState {
        /** The invite card's header: "New invite", or "3 new invites". */
        val invitesHeader: LocalizedText
            get() = if (invites.size == 1) {
                LocalizedText(GMR.strings.games_invites_header_one)
            } else {
                LocalizedText(GMR.strings.games_invites_header_many, listOf(invites.size.toString()))
            }

        /** Whether to show the "Upcoming games" header. Hidden when only invitations are listed. */
        val showsUpcomingHeader: Boolean
            get() = games.isNotEmpty()
    }
}

/**
 * One game on the Games tab, already formatted.
 *
 * @property id The game's id, for opening its details.
 * @property course The course name.
 * @property dateAndTime The date and first tee time, e.g. "Sat, Jun 13 · 9:00 AM".
 */
data class GameRowUiState(
    val id: String,
    val course: String,
    val dateAndTime: LocalizedText
)

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

/**
 * One avatar in a stack of players.
 *
 * @property initial The letter in the avatar.
 * @property tone The avatar's color: the organizer's gold, or the player's own color from their
 *   name — the same one they have on every screen.
 */
data class PlayerAvatarUiState(
    val initial: String,
    val tone: AvatarTone
)

/**
 * What the Games tab shows for these games, as [viewer] sees them: the empty state, or the
 * invitations still waiting for their answer and a row for every other game.
 *
 * An invitation is waiting when [viewer]'s number was invited and hasn't answered yet. A game
 * they declined is listed with the rest.
 */
fun List<Game>.toGamesListUiState(viewer: Player): GamesListUiState {
    if (isEmpty()) return GamesListUiState.Empty

    val phone = viewer.phone.toPhoneDigits()
    val (waiting, others) = partition { game ->
        game.inviteeFor(phone)?.status == InviteStatus.PENDING && !game.holdsSlot(viewer.id)
    }
    return GamesListUiState.Content(
        games = others.map { it.toRowUiState() },
        invites = waiting.map { it.toInviteRowUiState() }
    )
}

/**
 * The confirmation shown once the player has answered an invitation: "Pebble Beach confirmed"
 * after a yes, "Invite declined" after a no.
 */
fun inviteAnsweredMessage(course: String, accepted: Boolean): LocalizedText =
    if (accepted) {
        LocalizedText(GMR.strings.game_invite_accepted_message, listOf(course))
    } else {
        LocalizedText(GMR.strings.game_invite_declined_message)
    }

internal fun Game.toRowUiState(): GameRowUiState = GameRowUiState(
    id = id,
    course = course.name,
    dateAndTime = dateAndTime()
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

private fun Game.dateAndTime(): LocalizedText = LocalizedText(
    GMR.strings.games_row_date_time,
    listOf(date.weekdayMonthDay, teeTimes.firstOrNull()?.time?.formattedTime.orEmpty())
)

val previewGameRowUiState = previewGame.toRowUiState()

val previewGameRowUiStateList = previewGameList.map { it.toRowUiState() }

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
