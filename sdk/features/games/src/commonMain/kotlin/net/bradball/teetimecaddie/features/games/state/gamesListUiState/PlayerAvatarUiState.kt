package net.bradball.teetimecaddie.features.games.state.gamesListUiState

import net.bradball.teetimecaddie.core.models.AvatarTone
import net.bradball.teetimecaddie.core.models.GamePlayer

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

/** This player's avatar in a game [organizerId] organized: the organizer's gold, or their own color. */
internal fun GamePlayer.toAvatarUiState(organizerId: String): PlayerAvatarUiState = PlayerAvatarUiState(
    initial = initial,
    tone = if (playerId == organizerId) AvatarTone.SECONDARY else AvatarTone.forName(name)
)
