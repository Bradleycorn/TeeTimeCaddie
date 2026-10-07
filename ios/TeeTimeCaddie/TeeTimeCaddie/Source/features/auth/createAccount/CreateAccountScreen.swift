//
//  CreateAccountScreen.swift
//  TeeTimeCaddie
//

import PhotosUI
import SwiftUI
import ThemeUI
import TeeTimeCaddieKit

/// The profile step of creating an account: name, mobile number and an optional photo.
///
/// Reached only once the Firebase account exists, so leaving without finishing has to clean up
/// after itself — see ``CreateAccountViewModel/abandonSignUp()``, wired through the screen's
/// back-navigation handler so the back button and the swipe gesture are both covered.
struct CreateAccountScreen: View {
    @State private var viewModel: CreateAccountViewModel
    private let onBack: () -> Void

    init(email: String, onBack: @escaping () -> Void = {}) {
        self.viewModel = CreateAccountViewModel(email: email)
        self.onBack = onBack
    }

    var body: some View {
        Screen(.CreateAccount(viewName: self.viewName)) {
            CreateAccountContent(
                state: viewModel.uiState,
                onNameChange: viewModel.onNameChange,
                onPhoneChange: viewModel.onPhoneChange,
                onPhotoPicked: viewModel.onPhotoPicked,
                onPhotoRemoved: viewModel.removePhoto,
                onSubmit: { Task { await viewModel.submit() } },
                onMessageAction: {
                    viewModel.onMessageAction()
                    onBack()
                }
            )
        }
        // Every way out of this screen deletes the half-made account: the back button, the swipe
        // gesture, and `onBack` when "Sign in instead" pops programmatically. Centralised here so
        // no exit path can forget.
        .backNavigationHandler { viewModel.abandonSignUp() }
    }
}

// MARK: - Content

fileprivate struct CreateAccountContent: View {
    @EnvironmentObject private var theme: AppTheme
    @FocusState private var nameFocused: Bool
    @FocusState private var phoneFocused: Bool
    @State private var photoItem: PhotosPickerItem?
    @State private var photoPickerPresented = false

    /// The phone field's displayed text, formatted.
    ///
    /// Held locally rather than derived from `state.phoneDigits` through a transforming `Binding`:
    /// a `TextField` being typed into keeps its own buffer and ignores a binding whose `get`
    /// reshapes the value. Writing the formatted text back from `onChange` is what actually
    /// updates the field. See ``PhoneNumber``.
    @State private var phoneText = ""

    let state: CreateAccountUiState
    let onNameChange: (String) -> Void
    let onPhoneChange: (String) -> Void
    let onPhotoPicked: (Image?, Data?) -> Void
    let onPhotoRemoved: () -> Void
    let onSubmit: () -> Void
    let onMessageAction: () -> Void

    var body: some View {
        ScrollView {
            VStack(spacing: Metrics.spacing) {
                header
                photoPicker

                if let message = state.message {
                    AuthMessageCard(message: message, onAction: onMessageAction)
                }

                fields
            }
            .padding(.horizontal, Metrics.horizontalPadding)
            .padding(.vertical, Metrics.verticalPadding)
        }
        .scrollDismissesKeyboard(.interactively)
        // SwiftUI lifts this above the keyboard *and* insets the ScrollView by its height, so the
        // pinned CTA satisfies the keyboard requirement with no manual offset maths.
        .safeAreaInset(edge: .bottom) {
            TtcButton(
                AR.strings().create_account_title.localized(),
                color: .primary,
                isLoading: state.isSubmitting,
                action: onSubmit
            )
            .frame(maxWidth: .infinity)
            .disabled(!state.canSubmit)
            .padding(.horizontal, Metrics.horizontalPadding)
            .padding(.vertical, Metrics.ctaPadding)
            .background(.bar)
        }
        .photosPicker(isPresented: $photoPickerPresented, selection: $photoItem, matching: .images)
        .task(id: photoItem) { await loadPickedPhoto() }
        .task {
            // Delayed for the same reason as the credentials screen: a first-responder assignment
            // made during a NavigationStack push transition is dropped.
            try? await Task.sleep(for: .milliseconds(50))
            nameFocused = true
        }
    }

    private var header: some View {
        VStack(spacing: Metrics.headerSpacing) {
            Text(AR.strings().create_account_heading.localized())
                .font(.title2.weight(.semibold))
            Text(AR.strings().create_account_subheading.localized())
                .font(.subheadline)
                .foregroundStyle(theme.colorScheme.onSurfaceVariant)
                .multilineTextAlignment(.center)
        }
    }

    private var photoPicker: some View {
        TtcPhotoPicker(
            photo: state.photo,
            caption: state.hasPhoto
                ? PR.strings().photo_caption_added.localized()
                : PR.strings().photo_caption_empty.localized(),
            onClick: {
                // Tap to add, tap again to remove.
                if state.hasPhoto {
                    photoItem = nil
                    onPhotoRemoved()
                } else {
                    photoPickerPresented = true
                }
            }
        )
    }

    private var fields: some View {
        VStack(spacing: Metrics.fieldSpacing) {
            TtcTextField(
                PR.strings().field_label_full_name.localized(),
                text: Binding(get: { state.name }, set: onNameChange),
                leadingIcon: .symbol(.person),
                textContentType: .name,
                autocapitalization: .words,
                autocorrectionDisabled: false,
                focused: $nameFocused
            )
            .disabled(state.isSubmitting)
            .submitLabel(.next)
            .onSubmit { phoneFocused = true }

            TtcTextField(
                PR.strings().field_label_mobile.localized(),
                text: $phoneText,
                leadingIcon: .symbol(.phone),
                hint: PR.strings().mobile_hint.localized(),
                error: state.phoneError,
                keyboardType: .phonePad,
                textContentType: .telephoneNumber,
                focused: $phoneFocused
            )
            .disabled(state.isSubmitting)
            .onChange(of: phoneText) { _, typed in
                // Reshape what was typed, then report only the digits upward. The ViewModel never
                // sees "(502) 555-1234"; grouping is presentation and stops here.
                let digits = PhoneNumber.digits(typed)
                let formatted = PhoneNumber.formatted(digits)
                if phoneText != formatted { phoneText = formatted }
                if digits != state.phoneDigits { onPhoneChange(digits) }
            }
            // Seeds the field when the screen is rebuilt with digits already in state.
            .onAppear {
                if phoneText.isEmpty && !state.phoneDigits.isEmpty {
                    phoneText = PhoneNumber.formatted(state.phoneDigits)
                }
            }
        }
    }

    /// Loads the picked item as both a drawable `Image` and the bytes to upload.
    private func loadPickedPhoto() async {
        guard let photoItem else { return }
        guard let data = try? await photoItem.loadTransferable(type: Data.self),
              let uiImage = UIImage(data: data) else { return }
        onPhotoPicked(Image(uiImage: uiImage), data)
    }

    private enum Metrics {
        static let spacing: CGFloat = 20
        static let headerSpacing: CGFloat = 8
        static let fieldSpacing: CGFloat = 12
        static let horizontalPadding: CGFloat = 24
        static let verticalPadding: CGFloat = 24
        static let ctaPadding: CGFloat = 12
    }
}

// MARK: - Previews

/// Never constructs ``CreateAccountScreen``: that wrapper builds a ViewModel, which resolves the
/// SDK and kills the preview process.
fileprivate struct CreateAccountContentPreviews: View {
    let state: CreateAccountUiState

    init(state: CreateAccountUiState = CreateAccountUiState(email: "dana@example.com")) {
        self.state = state
    }

    var body: some View {
        CreateAccountContent(
            state: state,
            onNameChange: { _ in },
            onPhoneChange: { _ in },
            onPhotoPicked: { _, _ in },
            onPhotoRemoved: {},
            onSubmit: {},
            onMessageAction: {}
        )
    }
}

#Preview("Light") {
    TeeTimeCaddieTheme {
        CreateAccountContentPreviews()
    }
}

#Preview("Dark") {
    TeeTimeCaddieTheme {
        CreateAccountContentPreviews()
    }
    .preferredColorScheme(.dark)
}

/// The one case that shows a block and an inline field error at the same time.
#Preview("Phone in use") {
    TeeTimeCaddieTheme {
        CreateAccountContentPreviews(
            state: CreateAccountUiState(
                email: "dana@example.com",
                name: "Dana Whitfield",
                phoneDigits: "5025551234",
                phoneError: PR.strings().player_error_phone_in_use_inline.localized(),
                message: .phoneInUse
            )
        )
    }
}
