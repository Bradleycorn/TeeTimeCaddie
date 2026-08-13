//
//  FirebaseErrorLogger.swift
//  TeeTimeCaddie
//
//  Platform (iOS) implementation of the shared ErrorLogger interface, backed by the
//  native Firebase Crashlytics SDK. Created by the app and passed into the SDK during
//  initialization, so the shared code carries no Firebase dependency.
//

import Foundation
import TeeTimeCaddieKit
import FirebaseCrashlytics

class FirebaseErrorLogger: ErrorLogger {

    private var crashlytics: Crashlytics { Crashlytics.crashlytics() }

    func logException(throwable: KotlinThrowable) {
        let error = NSError(domain: "TeeTimeCaddieKit", code: 0, userInfo: [
            NSLocalizedDescriptionKey: throwable.message ?? "Unknown Kotlin error",
            "kotlinExceptionType": String(describing: type(of: throwable))
        ])
        crashlytics.record(error: error)
    }

    func logMessage(message: String) {
        crashlytics.log(message)
    }

    // The shared ErrorLogger has six recordStateValue overloads (one per primitive type).
    // SKIE exposes them to Swift as clean type-based overloads (all labelled `value:`).
    func recordStateValue(key: String, value: Bool) {
        crashlytics.setCustomValue(value, forKey: key)
    }

    func recordStateValue(key: String, value: Double) {
        crashlytics.setCustomValue(value, forKey: key)
    }

    func recordStateValue(key: String, value: Float) {
        crashlytics.setCustomValue(value, forKey: key)
    }

    func recordStateValue(key: String, value: Int32) {
        crashlytics.setCustomValue(value, forKey: key)
    }

    func recordStateValue(key: String, value: Int64) {
        crashlytics.setCustomValue(value, forKey: key)
    }

    func recordStateValue(key: String, value: String) {
        crashlytics.setCustomValue(value, forKey: key)
    }

    func setUserId(userId: String) {
        crashlytics.setUserID(userId)
    }
}
