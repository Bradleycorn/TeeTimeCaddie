//
//  ContactPicker.swift
//  TeeTimeCaddie
//

import ContactsUI
import SwiftUI

extension View {

    /// Presents the system's contact picker while `isPresented` is true. The twin of Android's
    /// `PickContacts`.
    ///
    /// The picker lists every contact, with those that have no number greyed out, and lets the
    /// organizer check several before tapping Done. The app holds no contacts permission: the
    /// picker runs outside the app and hands back just the contacts checked. Read them with
    /// ``PickedContactsReader``.
    ///
    /// - Parameter onDismiss: Called when the picker closes, with the checked contacts when the
    ///   organizer taps Done, or none when they cancel.
    func contactPicker(
        isPresented: Binding<Bool>,
        onDismiss: @escaping ([CNContact]) -> Void
    ) -> some View {
        background(ContactPickerPresenter(isPresented: isPresented, onDismiss: onDismiss))
    }
}

/// Presents the picker itself rather than being the content of a `.sheet`: the picker dismisses
/// itself when it's done, which would leave an empty sheet behind.
private struct ContactPickerPresenter: UIViewControllerRepresentable {
    @Binding var isPresented: Bool
    let onDismiss: ([CNContact]) -> Void

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    func makeUIViewController(context: Context) -> UIViewController {
        UIViewController()
    }

    func updateUIViewController(_ host: UIViewController, context: Context) {
        context.coordinator.presenter = self
        guard isPresented, host.presentedViewController == nil else { return }

        let picker = CNContactPickerViewController()
        picker.delegate = context.coordinator
        picker.displayedPropertyKeys = [CNContactPhoneNumbersKey]
        picker.predicateForEnablingContact = NSPredicate(format: "phoneNumbers.@count > 0")
        host.present(picker, animated: true)
    }

    final class Coordinator: NSObject, CNContactPickerDelegate {
        var presenter: ContactPickerPresenter

        init(_ presenter: ContactPickerPresenter) {
            self.presenter = presenter
        }

        // Picks whole contacts, every number included, as Android 17's picker does; each is invited
        // at one of them. The plural form is what lets the organizer check several. Picking single
        // numbers instead (the `didSelectContactProperties` form) returns nothing for a checked
        // contact.
        func contactPicker(_ picker: CNContactPickerViewController, didSelect contacts: [CNContact]) {
            presenter.isPresented = false
            presenter.onDismiss(contacts)
        }

        func contactPickerDidCancel(_ picker: CNContactPickerViewController) {
            presenter.isPresented = false
            presenter.onDismiss([])
        }
    }
}
