import SwiftUI
import ThemeUI
import TeeTimeCaddieKit

/// A bottom sheet from the Fairway Morning design system.
///
/// This view is the sheet's *content*: a header row with a ``title`` and, at its end, either the
/// caller's `action` or a ✕ that closes the sheet, then the caller's content. Present it with
/// ``SwiftUI/View/ttcBottomSheet(isPresented:size:onDismiss:content:)``, which adds the rest of the
/// design's sheet: the system drag indicator, a `surfaceContainerLow` background, and the height.
///
/// ```swift
/// .ttcBottomSheet(isPresented: $showPrePrompt) {
///     TtcBottomSheet(title: "Find your golf buddies") {
///         Text("…")
///         TtcButton("Not now", action: { showPrePrompt = false })
///     }
/// }
/// ```
///
/// Close the sheet by setting the presenting binding to `false`, or with the environment's
/// `dismiss` action from inside it — that is what the built-in ✕ does. Either way the system's own
/// slide-down plays, and so does dragging down or tapping outside.
///
/// A sheet with an `action` has no ✕, matching the design's contacts picker: it closes by dragging
/// down, tapping outside, or through the action itself. The Android twin is `TtcBottomSheet`.
struct TtcBottomSheet<Action: View, Content: View>: View {
    @EnvironmentObject private var theme: AppTheme
    @Environment(\.dismiss) private var dismiss

    private let title: String
    private let contentPadding: EdgeInsets
    private let action: Action?
    private let content: Content

    /// A sheet whose header ends in a ✕ that closes it.
    init(
        title: String,
        contentPadding: EdgeInsets = TtcBottomSheetStyle.contentPadding,
        @ViewBuilder content: () -> Content
    ) where Action == EmptyView {
        self.title = title
        self.contentPadding = contentPadding
        self.action = nil
        self.content = content()
    }

    /// A sheet whose header ends in `action` in place of the ✕, e.g. the picker's "Done (3)".
    init(
        title: String,
        contentPadding: EdgeInsets = TtcBottomSheetStyle.contentPadding,
        @ViewBuilder action: () -> Action,
        @ViewBuilder content: () -> Content
    ) {
        self.title = title
        self.contentPadding = contentPadding
        self.action = action()
        self.content = content()
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(spacing: 0) {
                Text(title)
                    .font(.title2.weight(.semibold))
                    .foregroundStyle(theme.colorScheme.onSurface)
                    .frame(maxWidth: .infinity, alignment: .leading)
                if let action {
                    action
                } else {
                    closeButton
                }
            }
            .padding(TtcBottomSheetStyle.headerPadding)

            VStack(alignment: .leading, spacing: 0) { content }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(contentPadding)
        }
        .frame(maxWidth: .infinity, alignment: .topLeading)
    }

    private var closeButton: some View {
        Button(action: { dismiss() }) {
            Image(SfSymbol.xmark)
                .font(.body.weight(.semibold))
                .foregroundStyle(theme.colorScheme.onSurfaceVariant)
                .frame(width: TtcBottomSheetStyle.closeButtonSize, height: TtcBottomSheetStyle.closeButtonSize)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(GR.strings().close.desc().localized())
    }
}

extension View {

    /// Presents `content`, usually a ``TtcBottomSheet``, as a Fairway Morning bottom sheet: the
    /// system drag indicator, a `surfaceContainerLow` background, and a height from `size`.
    ///
    /// The twin of Android's `TtcBottomSheet`, which owns its own presentation. iOS splits the two
    /// because the system draws the sheet itself — its corners, scrim and slide.
    func ttcBottomSheet<Content: View>(
        isPresented: Binding<Bool>,
        size: TtcBottomSheetSize = .fitted,
        onDismiss: (() -> Void)? = nil,
        @ViewBuilder content: @escaping () -> Content
    ) -> some View {
        sheet(isPresented: isPresented, onDismiss: onDismiss) {
            content().modifier(TtcBottomSheetPresentation(size: size))
        }
    }
}

/// The presentation settings ``SwiftUI/View/ttcBottomSheet(isPresented:size:onDismiss:content:)``
/// applies inside the sheet.
private struct TtcBottomSheetPresentation: ViewModifier {
    @EnvironmentObject private var theme: AppTheme

    let size: TtcBottomSheetSize

    /// The content's measured height, for ``TtcBottomSheetSize/fitted``. Zero until the first layout.
    @State private var fittedHeight: CGFloat = 0

    func body(content: Content) -> some View {
        sized(content)
            .presentationDragIndicator(.visible)
            .presentationBackground(theme.colorScheme.surfaceContainerLow)
    }

    @ViewBuilder
    private func sized(_ content: Content) -> some View {
        switch size {
        case .large:
            content.presentationDetents([.large])
        case .fitted:
            // The content takes its ideal height rather than the sheet's, and that height becomes
            // the detent. Until it is measured, `.medium` stands in for a single frame.
            content
                .fixedSize(horizontal: false, vertical: true)
                .onGeometryChange(for: CGFloat.self) { $0.size.height } action: { fittedHeight = $0 }
                .frame(maxHeight: .infinity, alignment: .top)
                .presentationDetents(fittedHeight > 0 ? [.height(fittedHeight)] : [.medium])
        }
    }
}

// MARK: - Previews

#Preview("Light") {
    TeeTimeCaddieTheme {
        TtcBottomSheetPreviewContent()
    }
}

#Preview("Dark") {
    TeeTimeCaddieTheme {
        TtcBottomSheetPreviewContent()
    }
    .preferredColorScheme(.dark)
}

/// Presents both kinds of sheet from a pair of buttons, so the preview shows the real presentation
/// rather than the content on its own.
private struct TtcBottomSheetPreviewContent: View {
    @EnvironmentObject private var theme: AppTheme

    @State private var showFitted = true
    @State private var showLarge = false

    var body: some View {
        VStack(spacing: 16) {
            Button("Fitted sheet", action: { showFitted = true })
            Button("Large sheet with an action", action: { showLarge = true })
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(theme.colorScheme.surface)
        .ttcBottomSheet(isPresented: $showFitted) {
            TtcBottomSheet(title: "Sheet title") {
                Text("Sheet content sits below the header, inset by the design's sheet padding.")
                    .foregroundStyle(theme.colorScheme.onSurfaceVariant)
                    .padding(.horizontal, 8)
            }
        }
        .ttcBottomSheet(isPresented: $showLarge, size: .large) {
            TtcBottomSheet(title: "Sheet title") {
                Button("Done (2)", action: { showLarge = false })
                    .fontWeight(.semibold)
                    .padding(.horizontal, 14)
            } content: {
                ForEach(1...3, id: \.self) { row in
                    Text("Row \(row)").padding(8)
                }
            }
        }
    }
}
