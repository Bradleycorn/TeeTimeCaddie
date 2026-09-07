import SwiftUI

/// Shared spacing and sizing values for ``TtcTimePickerDialog``, derived from the Fairway Morning
/// "Time pickers" spec. (Parallels ``TtcDatePickerStyle``; the Android twin is `TtcTimePickerDefaults`.)
///
/// There is deliberately no corner-radius token, for the same reason as ``TtcDatePickerStyle``: the
/// design's 14pt top-rounded sheet is drawn by the system's own sheet presentation, which the
/// *caller* attaches — so a radius here would be an unused constant that quietly disagreed with
/// whatever iOS actually draws.
///
/// There is also no seed-time constant, unlike the Android twin's `SeedTime`. ``TtcTimePickerDialog``
/// binds a non-optional `Date`, so there is always a value to open on and nothing to default; the
/// caller supplies the starting time.
enum TtcTimePickerStyle {

    /// Padding around the wheels — Fairway Morning's `8px 0 20px` inset.
    ///
    /// Zero horizontal: `UIDatePicker` lays out and sizes its own columns, so side padding would
    /// squeeze them rather than inset them.
    static let wheelPadding = EdgeInsets(top: 8, leading: 0, bottom: 20, trailing: 0)

    /// Sheet height for the caller's `.presentationDetents([.height(…)])`.
    ///
    /// Sized for the wheel's 216pt intrinsic height, the inline toolbar, and ``wheelPadding``. Much
    /// shorter than ``TtcDatePickerStyle/sheetHeight`` — which is exactly why this is its own token
    /// rather than a value shared between the two pickers.
    static let sheetHeight: CGFloat = 300
}
