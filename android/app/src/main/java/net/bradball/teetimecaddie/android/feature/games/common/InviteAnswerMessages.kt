package net.bradball.teetimecaddie.android.feature.games.common

import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessage
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessenger
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.TtcResult
import net.bradball.teetimecaddie.features.games.GameErrors
import net.bradball.teetimecaddie.features.games.GameException
import net.bradball.teetimecaddie.features.games.state.gamesListUiState.inviteAnsweredMessage

/**
 * Shows the outcome of answering an invitation to [course]: "Pebble Beach confirmed" or "Invite
 * declined" when it went through, "This game just filled up" when a yes lost the last slot, and
 * the failure's own message otherwise.
 *
 * Shared by the Games tab and Game details, which show the same confirmations.
 */
fun TtcMessenger.showInviteAnswer(result: TtcResult<Game>, course: String, accepted: Boolean) {
    when (result) {
        is TtcResult.Success -> {
            val message = inviteAnsweredMessage(course, accepted)
            show(TtcMessage(message.resource, message.args))
        }

        is TtcResult.Failure -> {
            val error = result.error
            // The invitation stays pending, and the screen shows the game as full once it updates.
            if (error is GameException && error.error == GameErrors.GAME_FULL) {
                show(error.title)
            } else {
                show(error)
            }
        }
    }
}
