//
//  NewGameScreen.swift
//  TeeTimeCaddie
//

import SwiftUI
import ThemeUI
import TeeTimeCaddieKit

/// The form for creating and publishing a new game.
struct NewGameScreen: View {
    private let onPublished: () -> Void

    @State private var viewModel = NewGameViewModel()
    @State private var showContactPicker = false

    init(onPublished: @escaping () -> Void) {
        self.onPublished = onPublished
    }

    var body: some View {
        Screen(.NewGame(viewName: self.viewName)) {
            NewGameContent(
                uiState: viewModel.uiState,
                onCourseChange: viewModel.onCourseChange,
                onDateSelected: viewModel.onDateSelected,
                onTimeSelected: viewModel.onTimeSelected,
                onSlotsChange: viewModel.onSlotsChange,
                onAddFromContactsClick: {
                    viewModel.onAddFromContactsClick(pickContacts: { showContactPicker = true })
                },
                onRemoveInvitee: viewModel.onRemoveInvitee,
                onPublishClick: {
                    Task { await viewModel.onPublishClick(onPublished: onPublished) }
                },
                onKeepEditing: viewModel.onKeepEditing,
                onDismissNoInviteeConfirm: viewModel.onDismissNoInviteeConfirm,
                onPublishAnyway: {
                    Task { await viewModel.onPublishAnyway(onPublished: onPublished) }
                }
            )
            .contactPicker(
                isPresented: $showContactPicker,
                onDismiss: { picked in
                    let reader = picked.isEmpty ? nil : PickedContactsReader(picked)
                    Task { await viewModel.onContactPickerDismissed(reader) }
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
    @State private var draftTime: Date = NewGameDefaults.seedTime

    private let uiState: NewGameUiState
    private let onCourseChange: (String) -> Void
    private let onDateSelected: (Date) -> Void
    private let onTimeSelected: (Date) -> Void
    private let onSlotsChange: (Int) -> Void
    private let onAddFromContactsClick: () -> Void
    private let onRemoveInvitee: (NewGameInviteeRow.ID) -> Void
    private let onPublishClick: () -> Void
    private let onKeepEditing: () -> Void
    private let onDismissNoInviteeConfirm: () -> Void
    private let onPublishAnyway: () -> Void

    init(
        uiState: NewGameUiState,
        onCourseChange: @escaping (String) -> Void,
        onDateSelected: @escaping (Date) -> Void,
        onTimeSelected: @escaping (Date) -> Void,
        onSlotsChange: @escaping (Int) -> Void,
        onAddFromContactsClick: @escaping () -> Void,
        onRemoveInvitee: @escaping (NewGameInviteeRow.ID) -> Void,
        onPublishClick: @escaping () -> Void,
        onKeepEditing: @escaping () -> Void,
        onDismissNoInviteeConfirm: @escaping () -> Void,
        onPublishAnyway: @escaping () -> Void
    ) {
        self.uiState = uiState
        self.onCourseChange = onCourseChange
        self.onDateSelected = onDateSelected
        self.onTimeSelected = onTimeSelected
        self.onSlotsChange = onSlotsChange
        self.onAddFromContactsClick = onAddFromContactsClick
        self.onRemoveInvitee = onRemoveInvitee
        self.onPublishClick = onPublishClick
        self.onKeepEditing = onKeepEditing
        self.onDismissNoInviteeConfirm = onDismissNoInviteeConfirm
        self.onPublishAnyway = onPublishAnyway
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: NewGameDefaults.fieldSpacing) {
                SectionLabel(GMR.strings().new_game_section_where_when.localized())

                TtcTextField(
                    GMR.strings().new_game_field_course.localized(),
                    text: Binding(get: { uiState.course }, set: onCourseChange),
                    leadingIcon: .symbol(.flag),
                    placeholder: GMR.strings().new_game_field_course_placeholder.localized(),
                    autocapitalization: .words,
                    autocorrectionDisabled: true,
                    focused: $courseFocused
                )
                .disabled(uiState.isSubmitting)
                .submitLabel(.done)

                PickerField(
                    label: GMR.strings().new_game_field_date.localized(),
                    icon: .symbol(.calendar),
                    value: uiState.dateText,
                    placeholder: GMR.strings().new_game_field_date_placeholder.localized(),
                    onActivate: {
                        courseFocused = false
                        // Reopening starts on the date already chosen.
                        draftDate = uiState.date ?? .today
                        showDatePicker = true
                    }
                )
                .disabled(uiState.isSubmitting)

                PickerField(
                    label: GMR.strings().new_game_field_tee_time.localized(),
                    icon: .symbol(.clock),
                    value: uiState.timeText,
                    placeholder: GMR.strings().new_game_field_tee_time_placeholder.localized(),
                    onActivate: {
                        courseFocused = false
                        draftTime = uiState.time ?? NewGameDefaults.seedTime
                        showTimePicker = true
                    }
                )
                .disabled(uiState.isSubmitting)

                SectionLabel(GMR.strings().new_game_section_players.localized())

                SlotSelector(selected: uiState.slots, onSelect: onSlotsChange)
                    .disabled(uiState.isSubmitting)

                SectionLabel(GMR.strings().new_game_section_invitees.localized())

                ForEach(uiState.invitees) { invitee in
                    InviteeRow(invitee: invitee, onRemove: { onRemoveInvitee(invitee.id) })
                        .disabled(uiState.isSubmitting)
                }

                TtcOutlinedButton(
                    uiState.addFromContactsLabel.localized(),
                    color: .primary,
                    icon: .symbol(.personBadgePlus),
                    fullWidth: true,
                    action: onAddFromContactsClick
                )
                .disabled(uiState.isSubmitting)
            }
            .padding(.horizontal, NewGameDefaults.horizontalPadding)
            .padding(.bottom, NewGameDefaults.horizontalPadding)
        }
        .scrollDismissesKeyboard(.interactively)
        .background(theme.colorScheme.surface)
        // Lifted above the keyboard, with the form scrolling beneath it.
        .safeAreaInset(edge: .bottom) {
            TtcButton(
                GMR.strings().new_game_publish_button.localized(),
                isLoading: uiState.isSubmitting,
                fullWidth: true,
                action: onPublishClick
            )
            .disabled(!uiState.canSubmit)
            .padding(.horizontal, NewGameDefaults.horizontalPadding)
            .padding(.vertical, NewGameDefaults.ctaPadding)
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
                get: { uiState.showNoInviteeConfirm },
                // Fires for every way the alert closes, buttons included, so it only closes it;
                // the buttons log which one was tapped.
                set: { isPresented in if !isPresented { onDismissNoInviteeConfirm() } }
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
}

fileprivate struct SectionLabel: View {
    @EnvironmentObject private var theme: AppTheme

    private let text: String

    init(_ text: String) {
        self.text = text
    }

    var body: some View {
        Text(text.uppercased())
            .font(.caption.weight(.semibold))
            .foregroundStyle(theme.colorScheme.onSurfaceVariant)
            .padding(.leading, NewGameDefaults.sectionLabelInset)
            .padding(.top, NewGameDefaults.sectionLabelTopPadding)
    }
}

/// A field whose value comes from a picker sheet: the label above the value, and a › at the end.
fileprivate struct PickerField: View {
    @EnvironmentObject private var theme: AppTheme

    private let label: String
    private let icon: ImageSource
    private let value: String?
    private let placeholder: String
    private let onActivate: () -> Void

    init(
        label: String,
        icon: ImageSource,
        value: String?,
        placeholder: String,
        onActivate: @escaping () -> Void
    ) {
        self.label = label
        self.icon = icon
        self.value = value
        self.placeholder = placeholder
        self.onActivate = onActivate
    }

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

    private let selected: Int
    private let onSelect: (Int) -> Void

    init(selected: Int, onSelect: @escaping (Int) -> Void) {
        self.selected = selected
        self.onSelect = onSelect
    }

    var body: some View {
        TtcCard {
            VStack(alignment: .leading, spacing: NewGameDefaults.slotLabelSpacing) {
                Text(GMR.strings().new_game_player_slots_label.localized())
                    .font(.caption)
                    .foregroundStyle(theme.colorScheme.onSurfaceVariant)

                HStack(spacing: NewGameDefaults.slotSpacing) {
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

/// Someone picked to invite: their avatar, name and number, and an ✕ that removes them at once.
fileprivate struct InviteeRow: View {
    @EnvironmentObject private var theme: AppTheme

    private let invitee: NewGameInviteeRow
    private let onRemove: () -> Void

    init(invitee: NewGameInviteeRow, onRemove: @escaping () -> Void) {
        self.invitee = invitee
        self.onRemove = onRemove
    }

    var body: some View {
        TtcCard(contentPadding: NewGameDefaults.inviteeRowPadding) {
            HStack(spacing: NewGameDefaults.inviteeRowSpacing) {
                TtcAvatar(invitee.initial, color: invitee.tone)
                VStack(alignment: .leading, spacing: NewGameDefaults.inviteeTextSpacing) {
                    Text(invitee.name)
                        .font(.body.weight(.medium))
                        .foregroundStyle(theme.colorScheme.onSurface)
                    Text(invitee.phone)
                        .font(.footnote)
                        .foregroundStyle(theme.colorScheme.onSurfaceVariant)
                }
                .lineLimit(1)
                .frame(maxWidth: .infinity, alignment: .leading)

                Button(action: onRemove) {
                    Image(SfSymbol.xmark)
                        .font(.body.weight(.semibold))
                        .foregroundStyle(theme.colorScheme.onSurfaceVariant)
                        .frame(width: NewGameDefaults.removeButtonSize, height: NewGameDefaults.removeButtonSize)
                        .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel(GMR.strings().new_game_remove_invitee.localized(invitee.name))
            }
        }
    }
}

/// Spacing, sizes and seeds for the New game form — the twin of `NewGameDefaults` on Android.
fileprivate enum NewGameDefaults {
    static let horizontalPadding: CGFloat = 16
    static let fieldSpacing: CGFloat = 8
    static let ctaPadding: CGFloat = 12
    static let sectionLabelInset: CGFloat = 4
    static let sectionLabelTopPadding: CGFloat = 12
    static let slotLabelSpacing: CGFloat = 10
    static let slotSpacing: CGFloat = 8
    static let inviteeRowPadding = EdgeInsets(top: 10, leading: 12, bottom: 10, trailing: 4)
    static let inviteeRowSpacing: CGFloat = 12
    static let inviteeTextSpacing: CGFloat = 1
    static let removeButtonSize: CGFloat = 36

    /// With no time set, the time picker opens at 7:00 AM.
    static var seedTime: Date {
        Calendar.current.date(bySettingHour: 7, minute: 0, second: 0, of: .today) ?? .today
    }
}

// MARK: - Previews

/// Never constructs ``NewGameScreen``: that wrapper builds a ViewModel, which resolves the SDK and
/// kills the preview process.
fileprivate struct NewGameContentPreviews: View {
    private let uiState: NewGameUiState

    init(uiState: NewGameUiState) {
        self.uiState = uiState
    }

    var body: some View {
        NewGameContent(
            uiState: uiState,
            onCourseChange: { _ in },
            onDateSelected: { _ in },
            onTimeSelected: { _ in },
            onSlotsChange: { _ in },
            onAddFromContactsClick: {},
            onRemoveInvitee: { _ in },
            onPublishClick: {},
            onKeepEditing: {},
            onDismissNoInviteeConfirm: {},
            onPublishAnyway: {}
        )
    }
}

#Preview("Empty") {
    TeeTimeCaddieTheme {
        NewGameContentPreviews(uiState: NewGameUiState())
    }
}

#Preview("Filled - Dark") {
    TeeTimeCaddieTheme {
        NewGameContentPreviews(uiState: NewGameUiState(
            course: "Pebble Beach",
            date: .today,
            dateText: "Sat, Jun 13",
            time: .today,
            timeText: "9:00 AM",
            slots: 3,
            invitees: [
                NewGameInviteeRow(
                    id: "matt", name: "Matt Chen", initial: "M", phone: "(415) 555-0188", tone: .primary
                ),
                NewGameInviteeRow(
                    id: "jeff", name: "Jeff Morrison", initial: "J", phone: "(415) 555-0142", tone: .secondary
                ),
            ]
        ))
    }
    .preferredColorScheme(.dark)
}
