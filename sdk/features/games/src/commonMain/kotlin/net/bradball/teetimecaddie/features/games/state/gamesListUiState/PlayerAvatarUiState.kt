package net.bradball.teetimecaddie.features.games.state.gamesListUiState

import net.bradball.teetimecaddie.core.models.AvatarTone

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
