package net.bradball.teetimecaddie.features.games.state.gamesListUiState

import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.features.games.GMR

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
