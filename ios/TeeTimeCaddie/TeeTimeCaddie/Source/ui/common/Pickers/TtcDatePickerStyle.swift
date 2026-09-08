import SwiftUI

/// Shared bounds and spacing values for ``TtcDatePickerDialog``, derived from the Fairway Morning
/// "Date pickers" spec. (Parallels `TtcCardStyle` / `TtcChipStyle`; the Android twin is
/// `TtcDatePickerDefaults`.)
///
/// There is deliberately no corner-radius token. The design's 14pt top-rounded sheet is drawn by the
/// system's own sheet presentation, which the *caller* attaches — so a radius here would be an unused
/// constant that quietly disagreed with whatever iOS actually draws.
enum TtcDatePickerStyle {

    /// Padding around the calendar — Fairway Morning's `6/16/24` calendar body inset.
    static let calendarPadding = EdgeInsets(top: 6, leading: 16, bottom: 24, trailing: 16)

    /// Sheet height for the caller's `.presentationDetents([.height(…)])`.
    ///
    /// The graphical picker is tall enough that `.medium` clips its last week row, so the detent is
    /// pinned rather than left to the system. Sized for the calendar plus the toolbar and
    /// ``calendarPadding``.
    static let sheetHeight: CGFloat = 440

    /// The default earliest selectable date — today.
    ///
    /// The design dims and disables every past day on the date field, so "today" is the floor for
    /// every current caller. Computed rather than stored so it re-reads the clock instead of freezing
    /// on whichever day this type was first touched. Twin of `TtcDatePickerDefaults.MinDate`.
    static var minDate: Date { .today }

    /// The default latest selectable date — one year out from ``minDate``.
    ///
    /// This is a *product* choice, not a design constraint: the design specifies no upper bound, but
    /// nobody schedules a golf game more than a season or two ahead, and a bounded range keeps the
    /// picker to a single `ClosedRange` initializer. Twin of `TtcDatePickerDefaults.MaxDate`.
    static var maxDate: Date { minDate.add(1, interval: .year) }
}
