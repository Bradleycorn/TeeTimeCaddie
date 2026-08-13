//
//  FirebaseTransactionLogger.swift
//  TeeTimeCaddie
//
//  Platform (iOS) implementation of the shared TransactionLogger interface, backed by the
//  native Firebase Performance SDK. Created by the app and passed into the SDK during
//  initialization, so the shared code carries no Firebase dependency.
//

import Foundation
import TeeTimeCaddieKit
import FirebasePerformance

class FirebaseTransactionLogger: TransactionLogger {

    private var traces: [String: Trace] = [:]

    func startTransaction(name: String) {
        guard let trace = Performance.startTrace(name: name) else { return }
        traces[name] = trace
    }

    func stopTransaction(name: String) {
        traces[name]?.stop()
        traces.removeValue(forKey: name)
    }

    func incrementPerformanceEvent(transactionName: String, metricName: String, increment: Int64) {
        traces[transactionName]?.incrementMetric(metricName, by: increment)
    }

    func logPerformanceAttribute(transactionName: String, attributeName: String, attribute: String) {
        traces[transactionName]?.setValue(attribute, forAttribute: attributeName)
    }

    func removePerformanceAttribute(transactionName: String, attributeName: String) {
        traces[transactionName]?.removeAttribute(attributeName)
    }
}
