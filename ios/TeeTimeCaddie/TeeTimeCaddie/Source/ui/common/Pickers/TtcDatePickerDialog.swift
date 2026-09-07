import SwiftUI
import ThemeUI
import TeeTimeCaddieKit

/// A date picker dialog from the Fairway Morning design system.
///
/// Renders SwiftUI's graphical date picker — a calendar with a tappable month/year label, `‹ ›` month
/// navigation, a weekday header, and a `primary` disc on the selected day — above a Cancel / Done
/// toolbar. Selection is a draft until Done is pressed: Cancel leaves `selection` untouched and
/// ``onConfirm`` never fires.
///
/// Dates outside `[minDate, maxDate]` are dimmed and inert, and the `‹` chevron stops at
/// ``minDate``'s month. Both bounds default to the design's behavior for a tee-time date: no earlier
/// than today, no more than a year out.
///
/// This view is the *content* of a sheet, not the presentation — the caller owns that, so it can pick
/// its own detents and dismissal:
///
/// ```swift
/// .sheet(isPresented: $showDatePicker) {
///     TtcDatePickerDialog(
///         selection: $teeTimeDate,
///         onCancel: { showDatePicker = false },
///         onConfirm: { showDatePicker = false }
///     )
///     .presentationDetents([.height(TtcDatePickerStyle.sheetHeight)])
/// }
/// ```
///
/// It speaks Swift `Date`, not the SDK's `LocalDate`, so it binds straight to `DatePicker` with no
/// conversion in the view layer. ViewModels convert at their own boundary — see
/// `Date.toLocalDate()` / `LocalDate.toDate()` in `util/DateExtensions.swift`.
///
/// **Deliberate divergence from the design and from the Android twin.** The design disables the
/// confirm button until a day is tapped, and Android honors that — Compose's `DatePickerState` has a
/// genuinely nullable selection. SwiftUI's `DatePicker` binds a non-optional `Date` and `.graphical`
/// emits no "the user tapped a day" signal, so there is no honest way to tell an untouched picker
/// from a deliberate one. Rather than infer it by diffing the binding — which leaves Done stuck
/// disabled when the user taps the day it already opened on — this seeds the picker with
/// ``selection`` and keeps Done always enabled, which is the idiomatic iOS behavior anyway.
///
/// There is deliberately no `title` parameter: the design's toolbar carries only the two actions.
struct TtcDatePickerDialog: View {
    @EnvironmentObject private var theme: AppTheme

    @Binding private var selection: Date
    private let minDate: Date
    private let maxDate: Date
    private let onCancel: () -> Void
    private let onConfirm: () -> Void

    /// The in-progress selection. Seeded from ``selection`` and only written back on Done, so Cancel
    /// discards — `DatePicker` writes through its binding on every tap, so the draft is what makes
    /// the design's commit-on-Done behavior possible.
    @State private var draft: Date

    init(
        selection: Binding<Date>,
        minDate: Date = TtcDatePickerStyle.minDate,
        maxDate: Date = TtcDatePickerStyle.maxDate,
        onCancel: @escaping () -> Void,
        onConfirm: @escaping () -> Void
    ) {
        assert(minDate <= maxDate, "TtcDatePickerDialog: minDate must not be after maxDate.")

        // `minDate...maxDate` traps on an inverted range, and the graphical picker misbehaves if its
        // selection sits outside the range — so order the bounds and clamp the seed rather than
        // letting a bad caller crash the app.
        let lowerBound = min(minDate, maxDate)
        let upperBound = max(minDate, maxDate)

        self._selection = selection
        self.minDate = lowerBound
        self.maxDate = upperBound
        self.onCancel = onCancel
        self.onConfirm = onConfirm
        self._draft = State(initialValue: min(max(selection.wrappedValue, lowerBound), upperBound))
    }

    var body: some View {
        let scheme = theme.colorScheme

        NavigationStack {
            DatePicker(
                "",
                selection: $draft,
                in: minDate...maxDate,
                displayedComponents: .date
            )
            .datePickerStyle(.graphical)
            .labelsHidden()
            .padding(TtcDatePickerStyle.calendarPadding)
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
            .background(scheme.surfaceContainerLow)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(GR.strings().cancel.desc().localized()) { onCancel() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(GR.strings().done.desc().localized()) {
                        selection = draft
                        onConfirm()
                    }
                    .fontWeight(.semibold)
                }
            }
            .toolbarBackground(scheme.surfaceContainerLow, for: .navigationBar)
            .toolbarBackground(.visible, for: .navigationBar)
        }
        // Tints the selected-day disc, today's accent text, the month chevrons and both toolbar
        // buttons — everything the design paints `primary`.
        .tint(scheme.primary)
    }
}

#Preview("Light") {
    TeeTimeCaddieTheme {
        TtcDatePickerDialogPreviewContent()
    }
}

#Preview("Dark") {
    TeeTimeCaddieTheme {
        TtcDatePickerDialogPreviewContent()
    }
    .preferredColorScheme(.dark)
}

private struct TtcDatePickerDialogPreviewContent: View {
    @State private var date: Date = .today.add(3, interval: .day)

    var body: some View {
        TtcDatePickerDialog(
            selection: $date,
            onCancel: {},
            onConfirm: {}
        )
        .frame(height: TtcDatePickerStyle.sheetHeight)
    }
}
