package net.bradball.teetimecaddie.features.games.state.gamesListUiState

import net.bradball.teetimecaddie.core.extensions.toPhoneDigits
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.InviteStatus
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.core.models.Player
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
