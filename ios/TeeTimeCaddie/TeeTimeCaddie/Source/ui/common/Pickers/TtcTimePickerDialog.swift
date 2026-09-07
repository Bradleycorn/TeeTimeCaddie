import SwiftUI
import ThemeUI
import TeeTimeCaddieKit

/// A time picker dialog from the Fairway Morning design system.
///
/// Renders SwiftUI's wheel time picker — three spinning columns reading hour · minute · AM/PM — above
/// a Cancel / Done toolbar. Selection is a draft until Done is pressed: Cancel leaves `selection`
/// untouched and ``onConfirm`` never fires.
///
/// This view is the *content* of a sheet, not the presentation — the caller owns that, so it can pick
/// its own detents and dismissal:
///
/// ```swift
/// .sheet(isPresented: $showTimePicker) {
///     TtcTimePickerDialog(
///         selection: $teeTimeTime,
///         onCancel: { showTimePicker = false },
///         onConfirm: { showTimePicker = false }
///     )
///     .presentationDetents([.height(TtcTimePickerStyle.sheetHeight)])
/// }
/// ```
///
/// It speaks Swift `Date`, not the SDK's `LocalTime`, so it binds straight to `DatePicker` with no
/// conversion in the view layer. ViewModels convert at their own boundary — see `Date.toLocalTime()`
/// / `LocalTime.toDate()` in `util/DateExtensions.swift`. Only the time of day is read; the `Date`'s
/// calendar day is carried through untouched.
///
/// Unlike ``TtcDatePickerDialog`` this takes **no bounds**. Material 3 gives the Android twin no
/// time-side equivalent of `SelectableDates`, so neither platform offers a `minTime`/`maxTime`, and
/// the design specifies none. There is also no seed parameter: the binding is non-optional, so
/// there is always a value to open on (Android's nullable `LocalTime?` needs a seed constant; this
/// doesn't). And there is deliberately no `title` — the design's toolbar carries only the two actions.
///
/// **Deliberate divergence, shared with ``TtcDatePickerDialog``.** Done is always enabled. `DatePicker`
/// binds a non-optional `Date` and emits no "the user touched it" signal, so an untouched picker
/// can't be told from a deliberate one, and Done on an untouched sheet commits the time it opened on.
/// The Android twin can't gate its OK button either — `TimePickerState` has non-nullable
/// `hour`/`minute` — so for once the two platforms agree.
///
/// **Five details of the design mock are not reproduced.** The mock draws a hand-built wheel; every
/// one of these lives inside `UIDatePicker`, which SwiftUI exposes no API for, so matching them would
/// mean owning a UIKit surface purely to restyle a system control:
/// - the selection band is the system's own, not the mock's `surfaceContainerHighest` 8pt-radius bar
///   inset 24pt,
/// - wheel metrics — the mock's 36pt rows and five visible rows — are UIPickerView's to decide,
/// - wheel typography and color are system-drawn; `.tint` doesn't reach them,
/// - column widths and per-column alignment are `UIDatePicker`'s, not the mock's three 70pt columns
///   with the hour right-aligned, minutes centred and AM/PM left-aligned,
/// - on a device set to 24-hour time there are two columns and no AM/PM. That last one is
///   *consistent with* the Android twin, which likewise follows the device setting, so it's a shared
///   decision rather than a platform quirk.
///
/// Don't "fix" these without revisiting that trade-off.
struct TtcTimePickerDialog: View {
    @EnvironmentObject private var theme: AppTheme

    @Binding private var selection: Date
    private let onCancel: () -> Void
    private let onConfirm: () -> Void

    /// The in-progress selection. Seeded from ``selection`` and only written back on Done, so Cancel
    /// discards — `DatePicker` writes through its binding on every spin, so the draft is what makes
    /// the design's commit-on-Done behavior possible.
    @State private var draft: Date

    init(
        selection: Binding<Date>,
        onCancel: @escaping () -> Void,
        onConfirm: @escaping () -> Void
    ) {
        self._selection = selection
        self.onCancel = onCancel
        self.onConfirm = onConfirm
        self._draft = State(initialValue: selection.wrappedValue)
    }

    var body: some View {
        let scheme = theme.colorScheme

        NavigationStack {
            DatePicker("", selection: $draft, displayedComponents: .hourAndMinute)
                .datePickerStyle(.wheel)
                .labelsHidden()
                .padding(TtcTimePickerStyle.wheelPadding)
                // Centered, where the date picker's calendar hangs from the top — the design's wheel
                // sits in the middle of its sheet.
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
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
                // Also draws the hairline beneath the toolbar that the design shows as a
                // `0.5px outline-variant` rule.
                .toolbarBackground(.visible, for: .navigationBar)
        }
        // Tints both toolbar buttons, which the design paints `primary`. It does not reach the wheel
        // itself — see the divergence list above.
        .tint(scheme.primary)
    }
}

#Preview("Light") {
    TeeTimeCaddieTheme {
        TtcTimePickerDialogPreviewContent()
    }
}

#Preview("Dark") {
    TeeTimeCaddieTheme {
        TtcTimePickerDialogPreviewContent()
    }
    .preferredColorScheme(.dark)
}

private struct TtcTimePickerDialogPreviewContent: View {
    @State private var time: Date = LocalTime(hour: 7, minute: 0, second: 0, nanosecond: 0).toDate()

    var body: some View {
        TtcTimePickerDialog(
            selection: $time,
            onCancel: {},
            onConfirm: {}
        )
        .frame(height: TtcTimePickerStyle.sheetHeight)
    }
}
