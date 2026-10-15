//
//  NewGameScreen.swift
//  TeeTimeCaddie
//

import SwiftUI
import ThemeUI
import TeeTimeCaddieKit

/// The form for creating and publishing a new game.
struct NewGameScreen: View {
    let onPublished: () -> Void

    @State private var viewModel = NewGameViewModel()

    var body: some View {
        Screen(.NewGame(viewName: self.viewName)) {
            NewGameContent(
                state: viewModel.uiState,
                onCourseChange: viewModel.onCourseChange,
                onDateSelected: viewModel.onDateSelected,
                onTimeSelected: viewModel.onTimeSelected,
                onSlotsChange: viewModel.onSlotsChange,
                onPublishClick: viewModel.onPublishClick,
                onKeepEditing: viewModel.onKeepEditing,
                onPublishAnyway: {
                    Task { await viewModel.onPublishAnyway(onPublished: onPublished) }
                }
            )
        }
    }
}

// MARK: - Content

fileprivate struct NewGameContent: View {
    @EnvironmentObject private var theme: AppTheme
    @FocusState private var courseFocused: Bool

    // Which picker is open, and the value it is editing, are purely presentational: the
    // ViewModel only hears about a value once Done commits it.
    @State private var showDatePicker = false
    @State private var showTimePicker = false
    @State private var draftDate: Date = .today
    @State private var draftTime: Date = Self.seedTime

    let state: NewGameUiState
    let onCourseChange: (String) -> Void
    let onDateSelected: (Date) -> Void
    let onTimeSelected: (Date) -> Void
    let onSlotsChange: (Int) -> Void
    let onPublishClick: () -> Void
    let onKeepEditing: () -> Void
    let onPublishAnyway: () -> Void

    /// With no time set, the time picker opens at 7:00 AM.
    private static var seedTime: Date {
        Calendar.current.date(bySettingHour: 7, minute: 0, second: 0, of: .today) ?? .today
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: Metrics.fieldSpacing) {
                SectionLabel(GMR.strings().new_game_section_where_when.localized())

                TtcTextField(
                    GMR.strings().new_game_field_course.localized(),
                    text: Binding(get: { state.course }, set: onCourseChange),
                    leadingIcon: .symbol(.flag),
                    placeholder: GMR.strings().new_game_field_course_placeholder.localized(),
                    autocapitalization: .words,
                    autocorrectionDisabled: true,
                    focused: $courseFocused
                )
                .disabled(state.isSubmitting)
                .submitLabel(.done)

                PickerField(
                    label: GMR.strings().new_game_field_date.localized(),
                    icon: .symbol(.calendar),
                    value: state.dateText,
                    placeholder: GMR.strings().new_game_field_date_placeholder.localized(),
                    onActivate: {
                        courseFocused = false
                        // Reopening starts on the date already chosen.
                        draftDate = state.date ?? .today
                        showDatePicker = true
                    }
                )
                .disabled(state.isSubmitting)

                PickerField(
                    label: GMR.strings().new_game_field_tee_time.localized(),
                    icon: .symbol(.clock),
                    value: state.timeText,
                    placeholder: GMR.strings().new_game_field_tee_time_placeholder.localized(),
                    onActivate: {
                        courseFocused = false
                        draftTime = state.time ?? Self.seedTime
                        showTimePicker = true
                    }
                )
                .disabled(state.isSubmitting)

                SectionLabel(GMR.strings().new_game_section_players.localized())

                SlotSelector(selected: state.slots, onSelect: onSlotsChange)
                    .disabled(state.isSubmitting)
            }
            .padding(.horizontal, Metrics.horizontalPadding)
            .padding(.bottom, Metrics.horizontalPadding)
        }
        .scrollDismissesKeyboard(.interactively)
        .background(theme.colorScheme.surface)
        // Lifted above the keyboard, with the form scrolling beneath it.
        .safeAreaInset(edge: .bottom) {
            TtcButton(
                GMR.strings().new_game_publish_button.localized(),
                isLoading: state.isSubmitting,
                fullWidth: true,
                action: onPublishClick
            )
            .disabled(!state.canSubmit)
            .padding(.horizontal, Metrics.horizontalPadding)
            .padding(.vertical, Metrics.ctaPadding)
            .background(.bar)
        }
        .sheet(isPresented: $showDatePicker) {
            TtcDatePickerDialog(
                selection: $draftDate,
                onCancel: { showDatePicker = false },
                onConfirm: {
                    onDateSelected(draftDate)
                    showDatePicker = false
                }
            )
            .presentationDetents([.height(TtcDatePickerStyle.sheetHeight)])
        }
        .sheet(isPresented: $showTimePicker) {
            TtcTimePickerDialog(
                selection: $draftTime,
                onCancel: { showTimePicker = false },
                onConfirm: {
                    onTimeSelected(draftTime)
                    showTimePicker = false
                }
            )
            .presentationDetents([.height(TtcTimePickerStyle.sheetHeight)])
        }
        // Not drawn in the design: the platform's own alert, as the story specifies.
        .alert(
            GMR.strings().new_game_no_invitees_title.localized(),
            isPresented: Binding(
                get: { state.showNoInviteeConfirm },
                set: { isPresented in if !isPresented { onKeepEditing() } }
            )
        ) {
            Button(GMR.strings().new_game_no_invitees_keep_editing.localized(), role: .cancel) {
                onKeepEditing()
            }
            Button(GMR.strings().new_game_no_invitees_publish_anyway.localized()) {
                onPublishAnyway()
            }
        } message: {
            Text(GMR.strings().new_game_no_invitees_message.localized())
        }
    }

    private enum Metrics {
        static let horizontalPadding: CGFloat = 16
        static let fieldSpacing: CGFloat = 8
        static let ctaPadding: CGFloat = 12
    }
}

fileprivate struct SectionLabel: View {
    @EnvironmentObject private var theme: AppTheme
    let text: String

    init(_ text: String) {
        self.text = text
    }

    var body: some View {
        Text(text.uppercased())
            .font(.caption.weight(.semibold))
            .foregroundStyle(theme.colorScheme.onSurfaceVariant)
            .padding(.leading, 4)
            .padding(.top, 12)
    }
}

/// A field whose value comes from a picker sheet: the label above the value, and a › at the end.
fileprivate struct PickerField: View {
    @EnvironmentObject private var theme: AppTheme

    let label: String
    let icon: ImageSource
    let value: String?
    let placeholder: String
    let onActivate: () -> Void

    var body: some View {
        TtcTextFieldContainer(
            label: label,
            leadingIcon: icon,
            isFocused: false,
            trailingIcon: .symbol(.chevronRight),
            onTrailingTap: onActivate,
            onActivate: onActivate
        ) {
            Text(value ?? placeholder)
                .foregroundStyle(value == nil ? theme.colorScheme.onSurfaceVariant : theme.colorScheme.onSurface)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(.isButton)
    }
}

/// How many players the tee time holds: four equal pills, exactly one selected.
///
/// Built from the shared buttons rather than `.pickerStyle(.segmented)`, which doesn't match the
/// design.
fileprivate struct SlotSelector: View {
    @EnvironmentObject private var theme: AppTheme

    let selected: Int
    let onSelect: (Int) -> Void

    var body: some View {
        TtcCard {
            VStack(alignment: .leading, spacing: 10) {
                Text(GMR.strings().new_game_player_slots_label.localized())
                    .font(.caption)
                    .foregroundStyle(theme.colorScheme.onSurfaceVariant)

                HStack(spacing: 8) {
                    ForEach(1...NewGameUiState.maxSlots, id: \.self) { slots in
                        if slots == selected {
                            TtcButton(String(slots), fullWidth: true, action: { onSelect(slots) })
                        } else {
                            TtcOutlinedButton(String(slots), fullWidth: true, action: { onSelect(slots) })
                        }
                    }
                }
            }
        }
    }
}

// MARK: - Previews

/// Never constructs ``NewGameScreen``: that wrapper builds a ViewModel, which resolves the SDK and
/// kills the preview process.
fileprivate struct NewGameContentPreviews: View {
    let state: NewGameUiState

    var body: some View {
        NewGameContent(
            state: state,
            onCourseChange: { _ in },
            onDateSelected: { _ in },
            onTimeSelected: { _ in },
            onSlotsChange: { _ in },
            onPublishClick: {},
            onKeepEditing: {},
            onPublishAnyway: {}
        )
    }
}

#Preview("Empty") {
    TeeTimeCaddieTheme {
        NewGameContentPreviews(state: NewGameUiState())
    }
}

#Preview("Filled - Dark") {
    TeeTimeCaddieTheme {
        NewGameContentPreviews(state: NewGameUiState(
            course: "Pebble Beach",
            date: .today,
            dateText: "Sat, Jun 13",
            time: .today,
            timeText: "9:00 AM",
            slots: 3
        ))
    }
    .preferredColorScheme(.dark)
}
