import SwiftUI
import ThemeUI

/// A brief confirmation shown at the bottom of the screen.
///
/// The declared twin of Android's `SnackbarHostState` + `TtcMessenger`. SwiftUI ships no snackbar,
/// so unlike Android — where re-skinning the platform component would have been the wrong instinct —
/// iOS genuinely needs this one built.
///
/// Presented through the environment and applied **once at the root**, above the branch between the
/// auth flow and the tabs. That placement is the whole point: the view that knows "you are signed
/// in" is destroyed by the very change that signing in triggers, so a message owned by that view
/// would go with it.
@MainActor
@Observable
final class TtcToastPresenter {

    /// The message on screen, or nil when nothing is showing.
    private(set) var message: String?

    /// How long a message stays up. Matches Android's `SnackbarDuration.Short`.
    static let duration: Duration = .seconds(4)

    private var dismissal: Task<Void, Never>?

    /// `nonisolated` so this can be built off the main actor.
    ///
    /// The class is `@MainActor` because its state is read during view updates, but construction
    /// touches nothing isolated. Without this, `AppModule.toastPresenter` would have to be
    /// main-actor isolated too — and default-argument expressions are evaluated in a *nonisolated*
    /// context, so every ViewModel taking one would need a nullable parameter to work around it.
    nonisolated init() {}

    /// Shows [message], replacing anything already up.
    ///
    /// Named to match Android's `SnackbarHostState.showSnackbar` / `TtcMessenger.show`, so the same
    /// call reads the same on both platforms.
    func show(_ message: String) {
        self.message = message

        // Restart the clock rather than letting an earlier message's timer cut this one short.
        dismissal?.cancel()
        dismissal = Task { [weak self] in
            try? await Task.sleep(for: Self.duration)
            guard !Task.isCancelled else { return }
            self?.message = nil
        }
    }

    func dismiss() {
        dismissal?.cancel()
        message = nil
    }
}

private struct TtcToastPresenterKey: EnvironmentKey {
    /// A live instance, so a `*Content` preview that happens to sit under `.ttcToast()` — or one
    /// that doesn't — needs no setup either way.
    @MainActor static let defaultValue = TtcToastPresenter()
}

extension EnvironmentValues {
    var ttcToastPresenter: TtcToastPresenter {
        get { self[TtcToastPresenterKey.self] }
        set { self[TtcToastPresenterKey.self] = newValue }
    }
}

extension View {
    /// Renders [presenter]'s messages over this view, and publishes it to descendants.
    ///
    /// Apply once, at the root.
    func ttcToast(_ presenter: TtcToastPresenter) -> some View {
        modifier(TtcToastModifier(presenter: presenter))
    }
}

private struct TtcToastModifier: ViewModifier {
    let presenter: TtcToastPresenter

    func body(content: Content) -> some View {
        content
            .environment(\.ttcToastPresenter, presenter)
            .overlay(alignment: .bottom) {
                if let message = presenter.message {
                    TtcToastView(message: message)
                        .padding(.horizontal, TtcToastView.horizontalInset)
                        .padding(.bottom, TtcToastView.bottomInset)
                        .transition(.move(edge: .bottom).combined(with: .opacity))
                }
            }
            .animation(.snappy, value: presenter.message)
    }
}

/// The floating card a toast is drawn as.
private struct TtcToastView: View {
    static let horizontalInset: CGFloat = 16
    static let bottomInset: CGFloat = 16

    let message: String

    var body: some View {
        TtcAccentCard(accent: .primary) {
            Text(message)
                .font(.subheadline)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        // Hug the text vertically. An `.overlay` offers its content the *whole* parent, and the
        // card's accent rail is a greedy shape — without this the toast grows to fill the screen.
        .fixedSize(horizontal: false, vertical: true)
        .shadow(radius: 8, y: 2)
        .accessibilityAddTraits(.isStaticText)
    }
}

// MARK: - Previews

#Preview("Light") {
    TeeTimeCaddieTheme {
        TtcToastPreviewContent()
    }
}

#Preview("Dark") {
    TeeTimeCaddieTheme {
        TtcToastPreviewContent()
    }
    .preferredColorScheme(.dark)
}

fileprivate struct TtcToastPreviewContent: View {
    @State private var presenter = TtcToastPresenter()

    var body: some View {
        VStack(spacing: 16) {
            Button("Welcome back") { presenter.show("Welcome back, Dana") }
            Button("Signed out") { presenter.show("Signed out") }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .ttcToast(presenter)
    }
}
