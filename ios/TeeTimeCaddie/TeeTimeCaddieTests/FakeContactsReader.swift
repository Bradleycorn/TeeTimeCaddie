//
//  FakeContactsReader.swift
//  TeeTimeCaddieTests
//

import TeeTimeCaddieKit

/// A `DeviceContactsReader` standing in for what the system contact picker returns: `contacts`,
/// or `error` thrown when set.
final class FakeContactsReader: DeviceContactsReader {
    struct ReadError: Error {}

    private let contacts: [DeviceContact]
    private let error: Error?

    init(contacts: [DeviceContact] = [], error: Error? = nil) {
        self.contacts = contacts
        self.error = error
    }

    func __readContacts() async throws -> [DeviceContact] {
        if let error { throw error }
        return contacts
    }
}
