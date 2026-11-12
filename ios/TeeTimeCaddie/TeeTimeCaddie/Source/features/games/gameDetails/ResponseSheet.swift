//
//  ResponseSheet.swift
//  TeeTimeCaddie
//

import SwiftUI
import ThemeUI
import TeeTimeCaddieKit

/// The sheet the organizer records an invitee's Yes or No in — the twin of `ResponseSheet` on
/// Android.
///
/// Present it with ``SwiftUI/View/ttcBottomSheet(isPresented:size:onDismiss:content:)``. Picking an
/// answer calls `onRecordResponse`, and the caller closes the sheet.
struct ResponseSheet: View {
    @EnvironmentObject private var theme: AppTheme

    let uiState: ResponseSheetUiState
    /// Called with the answer picked: true for Yes.
    let onRecordResponse: (Bool) -> Void

    var body: some View {
        // The design's response sheet has no ✕; it closes by dragging down, tapping outside, or
        // picking an answer. The spacer keeps the header the height it has with one.
        TtcBottomSheet(title: uiState.title.localized()) {
            Spacer().frame(width: 0, height: TtcBottomSheetStyle.closeButtonSize)
        } content: {
            Text(uiState.prompt.localized())
                .font(.subheadline)
                .foregroundStyle(theme.colorScheme.onSurfaceVariant)
                .padding(ResponseSheetStyle.promptPadding)
            ResponseOption(
                symbol: .checkmarkCircleFill,
                symbolColor: theme.colorScheme.primary,
                label: GR.strings().yes.localized(),
                detail: uiState.yesDetail.localized(),
                action: { onRecordResponse(true) }
            )
            .disabled(!uiState.canAccept)
            ResponseOption(
                symbol: .xmarkCircleFill,
                symbolColor: theme.colorScheme.onSurfaceVariant,
                label: GR.strings().no.localized(),
                detail: uiState.noDetail.localized(),
                action: { onRecordResponse(false) }
            )
        }
    }
}

/// A response sheet is for one invitee, so presenting one for another invitee is a new sheet.
extension ResponseSheetUiState: @retroactive Identifiable {
    public var id: String { inviteePhone }
}

/// One answer in the sheet: its symbol, the answer, and a line saying what it means.
fileprivate struct ResponseOption: View {
    @EnvironmentObject private var theme: AppTheme
    @Environment(\.isEnabled) private var isEnabled

    let symbol: SfSymbol
    let symbolColor: Color
    let label: String
    let detail: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: ResponseSheetStyle.optionSpacing) {
                Image(symbol)
                    .font(.title2)
                    .foregroundStyle(symbolColor)
                VStack(alignment: .leading, spacing: ResponseSheetStyle.optionTextSpacing) {
                    Text(label)
                        .font(.body.weight(.medium))
                        .foregroundStyle(theme.colorScheme.onSurface)
                    Text(detail)
                        .font(.subheadline)
                        .foregroundStyle(theme.colorScheme.onSurfaceVariant)
                }
                Spacer(minLength: 0)
            }
            .padding(ResponseSheetStyle.optionPadding)
            .frame(minHeight: ResponseSheetStyle.optionMinHeight)
            .contentShape(Rectangle())
        }
        .buttonStyle(ResponseOptionButtonStyle(isEnabled: isEnabled))
    }
}

/// Dims a disabled answer to the design's opacity, and only that: the system's plain style would
/// dim it a second time.
fileprivate struct ResponseOptionButtonStyle: ButtonStyle {
    let isEnabled: Bool

    func makeBody(configuration: Configuration) -> some View {
        configuration.label.opacity(opacity(isPressed: configuration.isPressed))
    }

    private func opacity(isPressed: Bool) -> CGFloat {
        guard isEnabled else { return ResponseSheetStyle.disabledOpacity }
        return isPressed ? ResponseSheetStyle.pressedOpacity : 1
    }
}

/// Spacing and sizes for the response sheet — the twin of `ResponseSheetDefaults` on Android.
fileprivate enum ResponseSheetStyle {
    static let promptPadding = EdgeInsets(top: 0, leading: 12, bottom: 8, trailing: 12)
    static let optionPadding = EdgeInsets(top: 8, leading: 12, bottom: 8, trailing: 12)
    static let optionMinHeight: CGFloat = 64
    static let optionSpacing: CGFloat = 16
    static let optionTextSpacing: CGFloat = 2
    // The design's dimming for an answer that can't be picked.
    static let disabledOpacity: CGFloat = 0.45
    static let pressedOpacity: CGFloat = 0.6
}

// MARK: - Previews

#Preview("Open") {
    TeeTimeCaddieTheme {
        ResponseSheet(uiState: ResponseSheetUiStateKt.previewResponseSheetUiState, onRecordResponse: { _ in })
            .background(Color(.secondarySystemBackground))
    }
}

#Preview("Tee time full - Dark") {
    TeeTimeCaddieTheme {
        ResponseSheet(uiState: ResponseSheetUiStateKt.previewFullResponseSheetUiState, onRecordResponse: { _ in })
            .background(Color(.secondarySystemBackground))
    }
    .preferredColorScheme(.dark)
}
