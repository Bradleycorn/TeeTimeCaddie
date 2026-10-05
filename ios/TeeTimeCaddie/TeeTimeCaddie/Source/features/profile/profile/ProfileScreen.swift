//
//  ProfileScreen.swift
//  TeeTimeCaddie
//

import SwiftUI
import ThemeUI
import TeeTimeCaddieKit

/// The Profile tab: who you are, and the way out.
///
/// Read-only by design. There is deliberately **no settings list and no version line** — editing a
/// profile is its own story, and the design gives this screen nothing else to do.
struct ProfileScreen: View {
    @State private var viewModel = ProfileViewModel()

    var body: some View {
        Screen(.Profile(viewName: self.viewName)) {
            ProfileContent(
                state: viewModel.uiState,
                onSignOut: { Task { await viewModel.signOut() } }
            )
        }
        .task { await viewModel.observeSession() }
    }
}

// MARK: - Content

fileprivate struct ProfileContent: View {
    @EnvironmentObject private var theme: AppTheme

    let state: ProfileUiState
    let onSignOut: () -> Void

    var body: some View {
        if state.isLoaded {
            ScrollView {
                VStack(spacing: Metrics.spacing) {
                    // The initial is the fallback, not a placeholder: TtcRemoteAvatar shows it
                    // while loading and on failure, so a broken photo URL degrades to a letter
                    // rather than to a hole.
                    TtcRemoteAvatar(
                        urlString: state.photoUrl,
                        initials: state.initial,
                        size: TtcAvatarStyle.pickerSize
                    )

                    Text(state.name)
                        .font(.title2.weight(.semibold))

                    VStack(spacing: Metrics.rowSpacing) {
                        ProfileDetailRow(icon: .symbol(.envelopeFill), value: state.email)
                        ProfileDetailRow(icon: .symbol(.phone), value: state.phone)
                    }

                    TtcOutlinedButton(
                        PR.strings().profile_sign_out_button.localized(),
                        color: .error,
                        icon: .symbol(.rectanglePortraitAndArrowRight),
                        action: onSignOut
                    )
                    .padding(.top, Metrics.signOutSpacing)
                }
                .frame(maxWidth: .infinity)
                .padding(Metrics.padding)
            }
        } else {
            ContentLoadingIndicator()
        }
    }

    private enum Metrics {
        static let spacing: CGFloat = 16
        static let rowSpacing: CGFloat = 12
        static let signOutSpacing: CGFloat = 16
        static let padding: CGFloat = 24
    }
}

fileprivate struct ProfileDetailRow: View {
    @EnvironmentObject private var theme: AppTheme

    let icon: ImageSource
    let value: String

    var body: some View {
        HStack(spacing: 12) {
            Icon(icon, size: 24)
                .foregroundStyle(theme.colorScheme.onSurfaceVariant)
            Text(value)
                .font(.body)
            Spacer(minLength: 0)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

// MARK: - Previews

/// Never constructs ``ProfileScreen``: that wrapper builds a ViewModel, which resolves the SDK and
/// kills the preview process.
fileprivate struct ProfileContentPreviews: View {
    let state: ProfileUiState

    init(state: ProfileUiState = .preview) {
        self.state = state
    }

    var body: some View {
        ProfileContent(state: state, onSignOut: {})
    }
}

fileprivate extension ProfileUiState {
    /// Preview data, written out rather than taken from the SDK's `previewPlayer` — touching a KMP
    /// type from a preview pulls in the framework.
    static let preview = ProfileUiState(
        name: "Dana Whitfield",
        email: "dana@example.com",
        phone: "(502) 555-1234",
        photoUrl: nil,
        initial: "D",
        isLoaded: true
    )
}

#Preview("Light") {
    TeeTimeCaddieTheme {
        ProfileContentPreviews()
    }
}

#Preview("Dark") {
    TeeTimeCaddieTheme {
        ProfileContentPreviews()
    }
    .preferredColorScheme(.dark)
}

#Preview("Loading") {
    TeeTimeCaddieTheme {
        ProfileContentPreviews(state: ProfileUiState())
    }
}
