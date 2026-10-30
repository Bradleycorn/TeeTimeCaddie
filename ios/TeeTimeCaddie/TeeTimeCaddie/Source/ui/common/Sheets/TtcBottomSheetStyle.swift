import SwiftUI

/// Shared spacing values and sizes for ``TtcBottomSheet``, derived from the Fairway Morning bottom
/// sheet in the Tee Time Caddie design. (Parallels `TtcCardStyle`; the Android twin is
/// `TtcBottomSheetDefaults`.)
///
/// There is deliberately no corner-radius token, for the same reason as `TtcDatePickerStyle`: the
/// rounded top is drawn by the system's sheet presentation, so a radius here would be an unused
/// constant that quietly disagreed with whatever iOS draws.
enum TtcBottomSheetStyle {

    /// Padding around the header row: the design's `4/8/8/20`, plus room at the top for the system
    /// drag indicator, which iOS overlays on the sheet rather than laying out above it.
    static let headerPadding = EdgeInsets(top: 20, leading: 20, bottom: 8, trailing: 8)

    /// Padding around the sheet's content: the design's `4/8/24`. Content that runs edge to edge,
    /// such as a list whose rows pad themselves, passes its own value.
    static let contentPadding = EdgeInsets(top: 4, leading: 8, bottom: 24, trailing: 8)

    /// The ✕ button's touch target — the design's 44pt square.
    static let closeButtonSize: CGFloat = 44
}

/// How tall a ``TtcBottomSheet`` presented with ``SwiftUI/View/ttcBottomSheet(isPresented:size:onDismiss:content:)``
/// stands.
enum TtcBottomSheetSize {
    /// Exactly as tall as its content — for a short sheet, such as a message with two buttons.
    case fitted

    /// The system's large detent — for a sheet that scrolls, such as a list.
    case large
}
