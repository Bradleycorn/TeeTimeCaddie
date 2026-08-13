//
//  FirebaseFirestoreClient.swift
//  TeeTimeCaddie
//
//  Platform (iOS) implementation of the shared FirestoreClient interface, backed by the native
//  Firebase Firestore SDK. Created by the app and passed into the SDK during initialization, so
//  the shared code carries no Firebase dependency. Documents cross the boundary as JSON strings.
//

import Foundation
import TeeTimeCaddieKit
import FirebaseFirestore

class FirebaseFirestoreClient: FirestoreClient {

    private let db: Firestore

    init(useEmulator: Bool) {
        let firestore = Firestore.firestore()
        if useEmulator {
            let settings = firestore.settings
            settings.host = "\(Self.emulatorHost):\(Self.emulatorPort)"
            settings.isSSLEnabled = false
            settings.cacheSettings = MemoryCacheSettings()
            firestore.settings = settings
        }
        db = firestore
    }

    // These satisfy the Kotlin `suspend` requirements. SKIE names the underlying protocol
    // requirements with a `__` prefix (the clean `add`/`get`/... async methods are caller-side
    // sugar that SKIE generates as an extension), so the implementation uses the `__` names.
    func __add(collection: String, json: String) async throws -> String {
        let ref = try await db.collection(collection).addDocument(data: json.toFirestoreDict())
        return ref.documentID
    }

    func __set(collection: String, id: String, json: String) async throws {
        try await db.collection(collection).document(id).setData(json.toFirestoreDict())
    }

    func __get(collection: String, id: String) async throws -> StoredDoc? {
        let snapshot = try await db.collection(collection).document(id).getDocument()
        guard snapshot.exists, let data = snapshot.data() else { return nil }
        return StoredDoc(id: snapshot.documentID, json: data.toJsonString())
    }

    func __query(collection: String, whereField: String, whereValue: String, orderByField: String, descending: Bool) async throws -> [StoredDoc] {
        let snapshot = try await db.collection(collection)
            .whereField(whereField, isEqualTo: whereValue)
            .order(by: orderByField, descending: descending)
            .getDocuments()
        return snapshot.documents.map { StoredDoc(id: $0.documentID, json: $0.data().toJsonString()) }
    }

    func observeQuery(
        collection: String,
        whereField: String,
        whereValue: String,
        orderByField: String,
        descending: Bool,
        onChange: @escaping ([StoredDoc]) -> Void,
        onError: @escaping (String) -> Void
    ) -> TeeTimeCaddieKit.Cancellable {
        let registration = db.collection(collection)
            .whereField(whereField, isEqualTo: whereValue)
            .order(by: orderByField, descending: descending)
            .addSnapshotListener { snapshot, error in
                if let error = error {
                    onError(error.localizedDescription)
                } else if let snapshot = snapshot {
                    onChange(snapshot.documents.map { StoredDoc(id: $0.documentID, json: $0.data().toJsonString()) })
                }
            }
        return ListenerCancellable(registration)
    }

    private static let emulatorHost = "127.0.0.1"
    private static let emulatorPort = 9399
}

private class ListenerCancellable: TeeTimeCaddieKit.Cancellable {
    private let registration: ListenerRegistration
    init(_ registration: ListenerRegistration) { self.registration = registration }
    func cancel() { registration.remove() }
}

private extension String {
    /// Parse this JSON object string into a Firestore-compatible dictionary.
    func toFirestoreDict() -> [String: Any] {
        guard let data = data(using: .utf8),
              let dict = try? JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            return [:]
        }
        return dict
    }
}

private extension Dictionary where Key == String, Value == Any {
    /// Serialize this Firestore document dictionary into a JSON object string.
    func toJsonString() -> String {
        guard let data = try? JSONSerialization.data(withJSONObject: self),
              let string = String(data: data, encoding: .utf8) else {
            return "{}"
        }
        return string
    }
}
