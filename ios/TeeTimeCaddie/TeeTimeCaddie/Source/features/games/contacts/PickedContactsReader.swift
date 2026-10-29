//
//  PickedContactsReader.swift
//  TeeTimeCaddie
//

import Contacts
import TeeTimeCaddieKit

/// The contacts the organizer checked in the system contact picker (see `contactPicker`), as a
/// `DeviceContactsReader`. The twin of Android's `PickedContactsReader`.
///
/// The picker hands back the contacts themselves, so there's nothing left to fetch. Each keeps
/// every number it has, in the contact's own order, with mobile numbers marked so the contact is
/// invited at one. Each number's label is the system's own wording for its type, in the device's
/// language.
final class PickedContactsReader: DeviceContactsReader {
    private let contacts: [DeviceContact]

    init(_ picked: [CNContact]) {
        contacts = picked.map { contact in
            DeviceContact(
                id: contact.identifier,
                name: contact.displayName,
                phones: contact.phoneNumbers.map { phone in
                    DevicePhone(
                        number: phone.value.stringValue,
                        label: phone.label.map { CNLabeledValue<CNPhoneNumber>.localizedString(forLabel: $0) },
                        isMobile: phone.label.map(mobileLabels.contains) ?? false
                    )
                }
            )
        }
    }

    func __readContacts() async throws -> [DeviceContact] {
        contacts
    }
}

/// The phone labels that count as a mobile number.
private let mobileLabels: Set<String> = [CNLabelPhoneNumberMobile, CNLabelPhoneNumberiPhone]

private extension CNContact {
    /// The name as the system shows it, or the given and family names when the picker didn't
    /// return what the formatter needs.
    var displayName: String {
        if areKeysAvailable([CNContactFormatter.descriptorForRequiredKeys(for: .fullName)]),
           let name = CNContactFormatter.string(from: self, style: .fullName) {
            return name
        }
        return "\(givenName) \(familyName)"
    }
}
