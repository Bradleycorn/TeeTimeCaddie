//
//  AddPlayersSheet.swift
//  TeeTimeCaddie
//

import SwiftUI
import ThemeUI
import TeeTimeCaddieKit

/// The Add players sheet: choose people from contacts or by name and number, then add them all.
///
/// This is the sheet's content. Present it with
/// ``SwiftUI/View/ttcBottomSheet(isPresented:size:onDismiss:content:)`` at `.large`. It has no ✕:
/// dragging it down or tapping outside discards the selection, and only `onAddChosen` changes the
/// game. The Android twin is `AddPlayersSheet`.
struct AddPlayersSheet: View {
    private let uiState: AddPlayersUiState
    private let onChooseFromContacts: () -> Void
    private let onNameChange: (String) -> Void
    private let onPhoneChange: (String) -> Void
    private let onAddEntered: () -> Void
    private let onUnchoose: (NewInvitee) -> Void
    private let onAddChosen: () -> Void

    init(
        uiState: AddPlayersUiState,
        onChooseFromContacts: @escaping () -> Void,
        onNameChange: @escaping (String) -> Void,
        onPhoneChange: @escaping (String) -> Void,
        onAddEntered: @escaping () -> Void,
        onUnchoose: @escaping (NewInvitee) -> Void,
        onAddChosen: @escaping () -> Void
    ) {
        self.uiState = uiState
        self.onChooseFromContacts = onChooseFromContacts
        self.onNameChange = onNameChange
        self.onPhoneChange = onPhoneChange
        self.onAddEntered = onAddEntered
        self.onUnchoose = onUnchoose
        self.onAddChosen = onAddChosen
    }

    var body: some View {
        TtcBottomSheet(title: GMR.strings().add_players_title.localized(), contentPadding: EdgeInsets()) {
            // No ✕ (design criterion 11). This keeps the header the height it has with one.
            Color.clear.frame(width: 0, height: TtcBottomSheetStyle.closeButtonSize)
        } content: {
            AddPlayersSheetBody(
                uiState: uiState,
                onChooseFromContacts: onChooseFromContacts,
                onNameChange: onNameChange,
                onPhoneChange: onPhoneChange,
                onAddEntered: onAddEntered,
                onUnchoose: onUnchoose,
                onAddChosen: onAddChosen
            )
        }
    }
}

// MARK: - Body

fileprivate struct AddPlayersSheetBody: View {
    @EnvironmentObject private var theme: AppTheme

    let uiState: AddPlayersUiState
    let onChooseFromContacts: () -> Void
    let onNameChange: (String) -> Void
    let onPhoneChange: (String) -> Void
    let onAddEntered: () -> Void
    let onUnchoose: (NewInvitee) -> Void
    let onAddChosen: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    ContactsRow(summary: uiState.contactsSummary?.localized(), onClick: onChooseFromContacts)
                    Text(GMR.strings().add_players_enter_prompt.localized())
                        .font(.subheadline)
                        .foregroundStyle(theme.colorScheme.onSurfaceVariant)
                        .padding(.top, AddPlayersSheetStyle.promptTopPadding)
                        .padding(.bottom, AddPlayersSheetStyle.fieldSpacing)
                    EntryForm(
                        uiState: uiState,
                        onNameChange: onNameChange,
                        onPhoneChange: onPhoneChange,
                        onAdd: onAddEntered
                    )
                }
                .padding(AddPlayersSheetStyle.bodyPadding)
            }
            .scrollDismissesKeyboard(.interactively)

            Tray(uiState: uiState, onUnchoose: onUnchoose, onAddChosen: onAddChosen)
        }
    }
}

/// *Choose from contacts*, with how many have been picked once any are.
fileprivate struct ContactsRow: View {
    @EnvironmentObject private var theme: AppTheme

    let summary: String?
    let onClick: () -> Void

    var body: some View {
        TtcCard(contentPadding: AddPlayersSheetStyle.contactsRowPadding, onClick: onClick) {
            HStack(spacing: AddPlayersSheetStyle.rowSpacing) {
                Image(SfSymbol.personCropRectangleStack)
                    .font(.system(size: AddPlayersSheetStyle.contactsIconSize))
                    .foregroundStyle(theme.colorScheme.onSurface)
                    .frame(width: AddPlayersSheetStyle.contactsBadgeSize, height: AddPlayersSheetStyle.contactsBadgeSize)
                    .background(theme.colorScheme.surfaceContainerHighest, in: Circle())
                    .accessibilityHidden(true)

                VStack(alignment: .leading, spacing: 0) {
                    Text(GMR.strings().add_players_choose_from_contacts.localized())
                        .font(.body.weight(.medium))
                        .foregroundStyle(theme.colorScheme.onSurface)
                    if let summary {
                        Text(summary)
                            .font(.footnote)
                            .foregroundStyle(theme.colorScheme.onSurfaceVariant)
                            .lineLimit(1)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)

                Image(SfSymbol.chevronRight)
                    .font(.body.weight(.semibold))
                    .foregroundStyle(theme.colorScheme.onSurfaceVariant)
                    .accessibilityHidden(true)
            }
        }
    }
}

/// Name and Phone number, then the "selected" message and Add.
fileprivate struct EntryForm: View {
    @EnvironmentObject private var theme: AppTheme
    @FocusState private var nameFocused: Bool
    @FocusState private var phoneFocused: Bool

    /// The phone field as shown, grouped. State holds only its digits; see ``PhoneNumber``.
    @State private var phoneText: String = ""

    let uiState: AddPlayersUiState
    let onNameChange: (String) -> Void
    let onPhoneChange: (String) -> Void
    let onAdd: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: AddPlayersSheetStyle.fieldSpacing) {
            TtcTextField(
                GMR.strings().add_players_field_name.localized(),
                text: Binding(get: { uiState.name }, set: onNameChange),
                leadingIcon: .symbol(.person),
                placeholder: GMR.strings().add_players_field_name_placeholder.localized(),
                error: uiState.nameError?.localized(),
                textContentType: .name,
                autocapitalization: .words,
                autocorrectionDisabled: true,
                focused: $nameFocused
            )
            .submitLabel(.next)
            .onSubmit { phoneFocused = true }

            TtcTextField(
                GMR.strings().add_players_field_phone.localized(),
                text: $phoneText,
                leadingIcon: .symbol(.phone),
                placeholder: GMR.strings().add_players_field_phone_placeholder.localized(),
                error: uiState.phoneError?.localized(),
                keyboardType: .phonePad,
                textContentType: .telephoneNumber,
                focused: $phoneFocused
            )
            .onChange(of: phoneText) { _, typed in
                // Reshape what was typed, then report only the digits upward.
                let digits = PhoneNumber.digits(typed)
                let formatted = PhoneNumber.formatted(digits)
                if phoneText != formatted { phoneText = formatted }
                if digits != uiState.phone { onPhoneChange(digits) }
            }
            // The form clears after each Add; follow it.
            .onChange(of: uiState.phone) { _, digits in
                if PhoneNumber.digits(phoneText) != digits { phoneText = PhoneNumber.formatted(digits) }
            }

            HStack(spacing: AddPlayersSheetStyle.rowSpacing) {
                if let message = uiState.chosenMessage?.localized() {
                    HStack(spacing: AddPlayersSheetStyle.messageIconSpacing) {
                        Image(SfSymbol.checkmarkCircleFill)
                            .font(.system(size: AddPlayersSheetStyle.messageIconSize))
                            .foregroundStyle(theme.colorScheme.primary)
                            .accessibilityHidden(true)
                        Text(message)
                            .font(.footnote)
                            .foregroundStyle(theme.colorScheme.onSurfaceVariant)
                            .lineLimit(1)
                    }
                }
                Spacer(minLength: 0)
                TtcOutlinedButton(
                    GMR.strings().add_players_add_button.localized(),
                    color: .primary,
                    icon: .symbol(.plus),
                    dense: true,
                    action: onAdd
                )
            }
        }
        // After each successful Add the form clears; put the cursor back in Name for the next person.
        .onChange(of: uiState.chosenMessage?.localized()) { _, message in
            if message != nil { nameFocused = true }
        }
        .onAppear {
            if phoneText.isEmpty && !uiState.phone.isEmpty {
                phoneText = PhoneNumber.formatted(uiState.phone)
            }
        }
    }
}

/// The selection's chips, when there are any, above *Add N players*.
fileprivate struct Tray: View {
    @EnvironmentObject private var theme: AppTheme

    let uiState: AddPlayersUiState
    let onUnchoose: (NewInvitee) -> Void
    let onAddChosen: () -> Void

    var body: some View {
        VStack(spacing: AddPlayersSheetStyle.traySpacing) {
            if !uiState.chosen.isEmpty {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: AddPlayersSheetStyle.chipSpacing) {
                        ForEach(uiState.chosen, id: \.self) { person in
                            ChosenPlayerChip(person: person, onClick: { onUnchoose(person) })
                        }
                    }
                }
            }
            TtcButton(uiState.addPlayersLabel.localized(), fullWidth: true, action: onAddChosen)
                .disabled(!uiState.canAddPlayers)
        }
        .padding(AddPlayersSheetStyle.trayPadding)
        .background {
            // Only the top corners should round. The theme's shapes are type-erased, so the whole
            // shape is drawn and carried on under the home indicator or the keyboard, where the
            // sheet's own edge hides its bottom corners.
            theme.shapes.large
                .fill(theme.colorScheme.surfaceContainerHigh)
                .ignoresSafeArea(edges: .bottom)
        }
    }
}

/// A chosen person in the tray: their avatar, first name and a ✕. Tapping anywhere on it unchooses
/// them. VoiceOver reads it as a button named for the person, with the hint "Remove".
fileprivate struct ChosenPlayerChip: View {
    @EnvironmentObject private var theme: AppTheme

    let person: NewInvitee
    let onClick: () -> Void

    var body: some View {
        TtcChip(
            color: .highest,
            size: .small,
            onClick: onClick,
            accessibilityHint: GMR.strings().add_players_remove_chip.localized()
        ) { _ in
            TtcAvatar(person.initial, color: person.avatarTone.toColorRole(), size: AddPlayersSheetStyle.chipAvatarSize)
                .accessibilityHidden(true)
            Text(person.firstName)
                .font(.subheadline.weight(.medium))
                .lineLimit(1)
            Image(SfSymbol.xmark)
                .font(.system(size: AddPlayersSheetStyle.chipRemoveIconSize, weight: .semibold))
                .foregroundStyle(theme.colorScheme.onSurfaceVariant)
                .accessibilityHidden(true)
        }
    }
}

/// Spacing and sizes for the Add players sheet — the twin of `AddPlayersSheetDefaults` on Android.
fileprivate enum AddPlayersSheetStyle {
    static let bodyPadding = EdgeInsets(top: 4, leading: 16, bottom: 16, trailing: 16)
    static let contactsRowPadding = EdgeInsets(top: 8, leading: 12, bottom: 8, trailing: 12)
    static let contactsBadgeSize: CGFloat = 40
    static let contactsIconSize: CGFloat = 18
    static let rowSpacing: CGFloat = 12
    static let promptTopPadding: CGFloat = 20
    static let fieldSpacing: CGFloat = 12
    static let messageIconSpacing: CGFloat = 6
    static let messageIconSize: CGFloat = 16
    static let trayPadding = EdgeInsets(top: 12, leading: 16, bottom: 20, trailing: 16)
    static let traySpacing: CGFloat = 12
    static let chipSpacing: CGFloat = 8
    static let chipAvatarSize: CGFloat = 24
    static let chipRemoveIconSize: CGFloat = 12
}

// MARK: - Previews

/// Previews the content on its own, without the sheet's presentation.
fileprivate struct AddPlayersSheetPreview: View {
    @EnvironmentObject private var theme: AppTheme

    let uiState: AddPlayersUiState

    var body: some View {
        AddPlayersSheet(
            uiState: uiState,
            onChooseFromContacts: {},
            onNameChange: { _ in },
            onPhoneChange: { _ in },
            onAddEntered: {},
            onUnchoose: { _ in },
            onAddChosen: {}
        )
        .background(theme.colorScheme.surfaceContainerLow)
    }
}

#Preview("Light") {
    TeeTimeCaddieTheme {
        AddPlayersSheetPreview(uiState: AddPlayersUiStateKt.previewAddPlayersUiState)
    }
}

#Preview("Dark") {
    TeeTimeCaddieTheme {
        AddPlayersSheetPreview(uiState: AddPlayersUiStateKt.previewAddPlayersUiState)
    }
    .preferredColorScheme(.dark)
}

#Preview("Empty") {
    TeeTimeCaddieTheme {
        AddPlayersSheetPreview(uiState: AddPlayersUiState.companion.open(organizerPhone: "5025551234", invitees: []))
    }
}
