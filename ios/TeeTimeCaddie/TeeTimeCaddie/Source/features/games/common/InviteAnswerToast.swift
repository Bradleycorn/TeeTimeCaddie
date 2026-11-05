//
//  InviteAnswerToast.swift
//  TeeTimeCaddie
//

import Foundation
import TeeTimeCaddieKit

extension TtcToastPresenter {

    /// Shows the outcome of answering an invitation: "Pebble Beach confirmed" or "Invite declined"
    /// when it went through, "This game just filled up" when a yes lost the last slot, and the
    /// failure's own message otherwise.
    ///
    /// Shared by the Games tab and Game details, which show the same confirmations — the twin of
    /// `showInviteAnswer` on Android.
    ///
    /// - Parameters:
    ///   - result: What answering returned. The confirmation names the course of the game as
    ///     written.
    ///   - accepted: Whether the answer was Yes.
    func showInviteAnswer(_ result: TtcResult<Game>, accepted: Bool) {
        switch onEnum(of: result) {
        case .success(let success):
            show(InviteAnsweredMessageKt.inviteAnsweredMessage(course: success.data.course.name, accepted: accepted).localized())
        case .failure(let failure):
            showInviteAnswerFailure(failure.error)
        }
    }

    /// Shows why an answer didn't go through. The invitation stays pending after a lost race, and
    /// the screen shows the game as full once it updates.
    func showInviteAnswerFailure(_ error: any TeeTimeCaddieException) {
        if let gameError = error as? GameException, gameError.error == .gameFull {
            show(gameError.title.localized())
        } else {
            show(error.displayMessage.localized(args: error.messageArgs))
        }
    }
}
