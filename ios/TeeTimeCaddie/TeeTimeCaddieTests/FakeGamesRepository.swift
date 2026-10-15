//
//  FakeGamesRepository.swift
//  TeeTimeCaddieTests
//

import Foundation
import TeeTimeCaddieKit
@testable import TeeTimeCaddie

/// Fixed values the games tests share.
enum TestData {
    static let organizer = Player(
        id: "organizer",
        name: "Dana Park",
        email: "dana@golf.app",
        phone: "5025552345",
        photoUrl: nil
    )

    static let otherPlayer = Player(
        id: "other",
        name: "Sam Pruitt",
        email: "sam@golf.app",
        phone: "5025553456",
        photoUrl: nil
    )

    static let eventManager = EventManager.companion.getInstance(enableLogging: false)

    static func game(
        id: String = "game-1",
        course: String = "Pebble Beach",
        date: LocalDate = LocalDate(year: 2026, month: 6, day: 13),
        time: LocalTime = LocalTime(hour: 9, minute: 0, second: 0, nanosecond: 0),
        slots: Int32 = 4,
        organizer: Player = organizer
    ) -> Game {
        Game(
            id: id,
            organizerId: organizer.id,
            course: Course(id: course.lowercased(), name: course),
            date: date,
            teeTimes: [
                GameTeeTime(
                    time: time,
                    slots: slots,
                    players: [GamePlayer(playerId: organizer.id, name: organizer.name)]
                )
            ]
        )
    }
}

/// A hand-written `GamesRepository` for ViewModel tests — the twin of the Android fake.
///
/// SKIE turns the Kotlin interface's suspend functions into `__`-prefixed async requirements; the
/// clean `publishGame(...)` callers use is an extension on top. ``upcomingGames(playerId:)`` is not
/// usable from Swift tests (see ``GamesListViewModelTests``), so it traps.
final class FakeGamesRepository: GamesRepository {

    struct PublishRequest: Equatable {
        let organizerId: String
        let courseName: String
        let date: String
        let time: String
        let slots: Int32
    }

    private(set) var games: [Game]
    private(set) var publishRequests: [PublishRequest] = []

    /// When set, ``__publishGame`` fails with this instead of publishing.
    var publishFailure: GameErrors?

    /// The stored spelling a published course comes back with. Defaults to the name as typed.
    var storedCourseName: (String) -> String = { $0.trimmingCharacters(in: .whitespaces) }

    init(games: [Game] = []) {
        self.games = games
    }

    func __publishGame(
        organizer: Player,
        courseName: String,
        date: LocalDate,
        time: LocalTime,
        slots: Int32
    ) async throws -> TtcResult<Game> {
        publishRequests.append(PublishRequest(
            organizerId: organizer.id,
            courseName: courseName,
            date: date.description(),
            time: time.description(),
            slots: slots
        ))
        if let publishFailure {
            return failure(publishFailure)
        }
        let name = storedCourseName(courseName)
        let game = TestData.game(
            id: "game-\(publishRequests.count)",
            course: name,
            date: date,
            time: time,
            slots: slots,
            organizer: organizer
        )
        games.append(game)
        return TtcResultSuccess(data: game)
    }

    func __getGame(gameId: String) async throws -> TtcResult<Game> {
        if let game = games.first(where: { $0.id == gameId }) {
            return TtcResultSuccess(data: game)
        }
        return failure(.notFound)
    }

    /// Kotlin's `Failure` is a `TtcResult<Nothing>`. Objective-C generics are erased at runtime,
    /// so viewing it as a `TtcResult<Game>` is exactly what Kotlin's covariance does.
    private func failure(_ error: GameErrors) -> TtcResult<Game> {
        TtcResultFailure(error: GameException(error: error, messageArgs: [], cause: nil)) as Any as! TtcResult<Game>
    }

    func upcomingGames(playerId: String) -> SkieSwiftFlow<[Game]> {
        fatalError("Kotlin Flows can't be built from Swift; see GamesListViewModelTests.")
    }
}
