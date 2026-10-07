import SwiftUI

extension View {
    /// Runs [action] when this screen is popped from its `NavigationStack`.
    ///
    /// The SwiftUI twin of Android's `BackHandler`. Use it when leaving a screen has to *do*
    /// something — clean up a half-finished sign-up, discard a draft — rather than merely stop
    /// showing it.
    ///
    /// Fires for every way out: the navigation bar's back button, the interactive swipe-back
    /// gesture, and a programmatic pop. It does **not** fire when the screen is merely covered by a
    /// push, which is what makes it usable for cleanup and what `.onDisappear` cannot offer —
    /// `.onDisappear` cannot tell "popped" from "covered", or from the whole stack going away.
    ///
    /// Unlike Android's `BackHandler` this does not *intercept* the gesture; the pop has already
    /// been committed by the time [action] runs. So use it for cleanup, not to ask "are you sure?".
    ///
    /// ```swift
    /// CreateAccountContent(...)
    ///     .backNavigationHandler { viewModel.abandonSignUp() }
    /// ```
    func backNavigationHandler(perform action: @escaping () -> Void) -> some View {
        // In a `background` so it has no effect on layout: it renders nothing and exists only to
        // get a UIViewController into the hierarchy to observe.
        background(BackNavigationObserver(onBack: action))
    }
}

/// Reports when the SwiftUI screen hosting it is popped.
///
/// SwiftUI has no hook for this, but UIKit does: a view controller being *removed* from its parent
/// reports `isMovingFromParent` during `viewWillDisappear`, while one being covered by a push does
/// not. This gets a controller into the hierarchy purely so that flag can be read.
private struct BackNavigationObserver: UIViewControllerRepresentable {
    let onBack: () -> Void

    func makeUIViewController(context: Context) -> Controller {
        Controller(onBack: onBack)
    }

    func updateUIViewController(_ controller: Controller, context: Context) {
        // Re-captured each update so the closure never outlives the state it refers to.
        controller.onBack = onBack
    }

    final class Controller: UIViewController {
        var onBack: () -> Void

        /// Latches so a single controller can report at most once.
        private var hasReported = false

        init(onBack: @escaping () -> Void) {
            self.onBack = onBack
            super.init(nibName: nil, bundle: nil)
        }

        required init?(coder: NSCoder) {
            fatalError("init(coder:) has not been implemented")
        }

        override func loadView() {
            view = UIView()
            view.isUserInteractionEnabled = false
        }

        /// Reports when this controller is removed from its parent.
        ///
        /// `didMove(toParent: nil)` rather than the more obvious `viewWillDisappear` +
        /// `isMovingFromParent`. That UIKit idiom does **not** work under a SwiftUI
        /// `NavigationStack`: the stack is driven by its `path` binding and the controller
        /// hierarchy is reconciled afterwards, so at `viewWillDisappear` time *nothing* in the
        /// chain — not this controller, not the `NavigationStackHostingController`, not the
        /// `UIKitNavigationController` — reports `isMovingFromParent`. Removal is only announced
        /// here. Verified against iOS 26.5; see the commit message for the trace.
        ///
        /// Being *covered* by a push does not remove this controller, so that case correctly does
        /// not report — which is the discrimination `.onDisappear` cannot make.
        override func didMove(toParent parent: UIViewController?) {
            super.didMove(toParent: parent)
            guard parent == nil, !hasReported else { return }
            hasReported = true
            onBack()
        }
    }
}
