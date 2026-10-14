//
//  AuthMessage.swift
//  TeeTimeCaddie
//

import Foundation
import TeeTimeCaddieKit

/// A message block shown on an auth screen — the designed alternative to an alert or a toast for
/// something the person has to read and act on.
///
/// The Swift twin of `AuthMessage.kt`, deliberately a **closed** set rather than a mapper over every
/// `AuthErrors` / `PlayerErrors` case. These three are the ones the design draws a block for;
/// everything else is a transient failure that belongs in a toast, and listing them here would
/// invite a block the design has no treatment for.
///
/// Each case carries its own presentation, so ``AuthMessageCard`` renders any message without
/// knowing which one it has.
enum AuthMessage: Equatable {

    /// Sign-in was rejected.
    ///
    /// Says nothing about *which* half was wrong. With Firebase email enumeration protection on,
    /// "no such account" and "wrong password" are indistinguishable to us — and disclosing either
    /// would be the leak the protection exists to prevent.
    case signInFailed

    /// Creating an account was rejected because the address already has one.
    ///
    /// Not an error in the person's eyes — they have an account and typed the right address — so it
    /// is drawn in `.secondary` rather than `.error`, and its action takes them to signing in.
    case emailInUse(email: String)

    /// The mobile number already belongs to another player.
    ///
    /// Shown **together with** an inline error on the field itself: the block explains and offers a
    /// way out, the field marks what to change.
    case phoneInUse

    /// The tone the block is drawn in.
    var role: TtcColorRole {
        switch self {
        case .signInFailed: .error
        case .emailInUse, .phoneInUse: .secondary
        }
    }

    /// The glyph shown beside the title.
    var icon: ImageSource {
        switch self {
        case .signInFailed: .symbol(.lockBadgeXmarkFill)
        case .emailInUse: .symbol(.envelopeBadgePersonCropFill)
        case .phoneInUse: .symbol(.phoneBadgeWaveformFill)
        }
    }

    var title: String {
        switch self {
        case .signInFailed:
            AR.strings().login_error_invalid_credentials_title.localized()
        case .emailInUse:
            AR.strings().reg_error_email_in_use_title.localized()
        case .phoneInUse:
            PR.strings().player_error_phone_in_use_title.localized()
        }
    }

    var body: String {
        switch self {
        case .signInFailed:
            AR.strings().login_error_invalid_credentials_message.localized()
        case .emailInUse(let email):
            AR.strings().reg_error_email_in_use_message.localized(email)
        case .phoneInUse:
            PR.strings().player_error_phone_in_use_message.localized()
        }
    }

    /// The block's action, or nil for one that is purely informational.
    ///
    /// Nil because ``signInFailed`` has nothing to offer: there is no recovery beyond correcting
    /// what was typed, and a button that re-submits the same credentials would be noise.
    var actionTitle: String? {
        switch self {
        case .signInFailed:
            nil
        case .emailInUse:
            AR.strings().reg_error_email_in_use_recovery.localized()
        case .phoneInUse:
            PR.strings().player_error_phone_in_use_recovery.localized()
        }
    }
}
