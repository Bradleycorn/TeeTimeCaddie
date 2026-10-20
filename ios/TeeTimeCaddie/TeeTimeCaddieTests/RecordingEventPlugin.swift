//
//  RecordingEventPlugin.swift
//  TeeTimeCaddieTests
//

import Foundation
import TeeTimeCaddieKit

/// An `EventManager` with no loggers, plus the events it was asked to log — the twin of the
/// Android tests' `RecordingEventManager`.
final class RecordingEventManager {
    private(set) var events: [AnalyticsEvent] = []

    let eventManager = EventManager.companion.getInstance(enableLogging: false)

    init() {
        eventManager.registerPlugin(eventPlugin: Plugin(record: { [unowned self] in events.append($0) }))
    }

    private final class Plugin: EventPlugin {
        private let record: (AnalyticsEvent) -> Void

        init(record: @escaping (AnalyticsEvent) -> Void) {
            self.record = record
        }

        func logEvent(event: AnalyticsEvent) -> Bool {
            record(event)
            return true
        }

        func logScreenView(screen: AnalyticsScreen, displayMethod: ScreenType) -> Bool { false }

        func setUserId(userId: String) {}
    }
}
