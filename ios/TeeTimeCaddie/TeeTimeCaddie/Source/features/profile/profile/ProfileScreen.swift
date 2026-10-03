//
//  ProfileScreen.swift
//  TeeTimeCaddie
//

import SwiftUI
import ThemeUI
import TeeTimeCaddieKit

/// Placeholder profile screen.
///
/// Carries only what the app shell needs to be exercised end to end — the player's name and a
/// working sign-out. TTC-82 replaces the body with the designed avatar / name / email / phone
/// layout.
struct ProfileScreen: View {
    @State private var viewModel = ProfileViewModel()

    var body: some View {
        Screen(.Profile(viewName: self.viewName)) {
            ProfileContent(
                player: viewModel.player,
                onSignOut: { viewModel.signOut() }
            )
        }
    }
}

fileprivate struct ProfileContent: View {
    let player: Player?
    let onSignOut: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            if let player {
                Text(player.name).font(.title2)
            } else {
                Text("Loading profile…")
            }

            TtcOutlinedButton(
                PR.strings().profile_sign_out_button.desc().localized(),
                color: .error,
                icon: .symbol(.rectanglePortraitAndArrowRight),
                action: onSignOut
            )
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

#Preview("Light") {
    TeeTimeCaddieTheme {
        ProfileContent(player: PlayerKt.previewPlayer, onSignOut: {})
    }
}

#Preview("Loading") {
    TeeTimeCaddieTheme {
        ProfileContent(player: nil, onSignOut: {})
    }
}
