//
//  GameDetailsViewModelTests.swift
//  TeeTimeCaddieTests
//

import XCTest
import TeeTimeCaddieKit
@testable import TeeTimeCaddie

/// The Swift twin of `GameDetailsViewModelTest.kt`.
///
/// The live game isn't driven here: `observeGame` returns a Kotlin `Flow`, and SKIE gives Swift no
/// public way to build one, so a Swift fake can't serve it. Tests hand the ViewModel a game through
/// `receive(_:viewer:)` instead. What the screen shows for a given game and viewer is
/// `Game.toDetailsUiState(viewer:respondingFor:)`, which the SDK tests cover.
@MainActor
final class GameDetailsViewModelTests: XCTestCase {

    private func viewModel(
        _ repository: FakeGamesRepository,
        _ toastPresenter: TtcToastPresenter,
        gameId: String = "game-1",
        eventManager: EventManager = TestData.eventManager
    ) -> GameDetailsViewModel {
        GameDetailsViewModel(
            gameId: gameId,
            gamesRepository: repository,
            currentPlayer: { TestData.otherPlayer },
            eventManager: eventManager,
            toastPresenter: toastPresenter
        )
    }

    func testStartsLoading() {
        let viewModel = viewModel(FakeGamesRepository(), TtcToastPresenter())

        XCTAssertTrue(viewModel.uiState is GameDetailsUiStateLoading)
    }

    func testAYesIsSentAndConfirmedWithTheCourse() async {
        let repository = FakeGamesRepository(games: [TestData.game(course: "Pebble Beach")])
        let toastPresenter = TtcToastPresenter()

        await viewModel(repository, toastPresenter).onAnswer(accept: true)

        XCTAssertEqual(repository.respondRequests.map(\.gameId), ["game-1"])
        XCTAssertEqual(repository.respondRequests.map(\.accept), [true])
        XCTAssertEqual(toastPresenter.message, "Pebble Beach confirmed")
    }

    func testANoIsConfirmedWithoutTheCourse() async {
        let toastPresenter = TtcToastPresenter()

        await viewModel(FakeGamesRepository(games: [TestData.game()]), toastPresenter).onAnswer(accept: false)

        XCTAssertEqual(toastPresenter.message, "Invite declined")
    }

    func testAYesThatLosesTheLastSlotSaysTheGameJustFilledUp() async {
        let repository = FakeGamesRepository(games: [TestData.game()])
        repository.respondFailure = .gameFull
        let toastPresenter = TtcToastPresenter()

        await viewModel(repository, toastPresenter).onAnswer(accept: true)

        XCTAssertEqual(toastPresenter.message, "This game just filled up")
    }

    func testAMissingGameShowsItsOwnMessage() async {
        let toastPresenter = TtcToastPresenter()

        await viewModel(FakeGamesRepository(), toastPresenter, gameId: "nope").onAnswer(accept: true)

        XCTAssertEqual(toastPresenter.message, GameErrors.notFound.message.localized())
    }

    func testEachTapOnYesOrNoIsLoggedOnceWithItsResponseAndSource() async {
        let analytics = RecordingEventManager()
        let viewModel = viewModel(
            FakeGamesRepository(games: [TestData.game()]), TtcToastPresenter(), eventManager: analytics.eventManager
        )

        await viewModel.onAnswer(accept: false)

        XCTAssertEqual(analytics.events, [AnalyticsEvent.AnswerInviteClick(response: .no, source: .gameDetails)])
    }

    func testAYesIsLoggedEvenWhenTheAnswerFails() async {
        let analytics = RecordingEventManager()
        let repository = FakeGamesRepository(games: [TestData.game()])
        repository.respondFailure = .gameFull
        let viewModel = viewModel(repository, TtcToastPresenter(), eventManager: analytics.eventManager)

        await viewModel.onAnswer(accept: true)

        XCTAssertEqual(analytics.events, [AnalyticsEvent.AnswerInviteClick(response: .yes, source: .gameDetails)])
    }

    // MARK: - The organizer recording a response

    private let mattPhone = "4155550188"

    /// The organizer's game, with Matt invited and not yet answering.
    private func organizedGame(mattStatus: InviteStatus = .pending) -> Game {
        let game = TestData.game()
        return Game(
            id: game.id,
            organizerId: game.organizerId,
            course: game.course,
            date: game.date,
            teeTimes: game.teeTimes,
            invitees: [GameInvitee(name: "Matt Chen", phone: mattPhone, playerId: nil, status: mattStatus)]
        )
    }

    private func organizerViewModel(
        _ repository: FakeGamesRepository,
        _ toastPresenter: TtcToastPresenter = TtcToastPresenter(),
        eventManager: EventManager = TestData.eventManager
    ) -> GameDetailsViewModel {
        let viewModel = GameDetailsViewModel(
            gameId: "game-1",
            gamesRepository: repository,
            currentPlayer: { TestData.organizer },
            eventManager: eventManager,
            toastPresenter: toastPresenter
        )
        viewModel.receive(TtcResultSuccess(data: organizedGame()), viewer: TestData.organizer)
        return viewModel
    }

    private func responseSheet(_ viewModel: GameDetailsViewModel) -> ResponseSheetUiState? {
        ((viewModel.uiState as? GameDetailsUiStateContent)?.game as? GameUiStateOrganizer)?.responseSheet
    }

    func testTappingAnInviteeOpensTheirResponseSheetAndIsLogged() {
        let analytics = RecordingEventManager()
        let viewModel = organizerViewModel(FakeGamesRepository(games: [organizedGame()]), eventManager: analytics.eventManager)
        XCTAssertNil(responseSheet(viewModel))

        viewModel.onInviteeClick(inviteePhone: mattPhone)

        XCTAssertEqual(responseSheet(viewModel)?.inviteePhone, mattPhone)
        XCTAssertEqual(analytics.events, [AnalyticsEvent.InviteeRowClick.shared])
    }

    func testTheSheetClosesOnItsOwnOnceTheInviteeHasAnswered() {
        let viewModel = organizerViewModel(FakeGamesRepository(games: [organizedGame()]))
        viewModel.onInviteeClick(inviteePhone: mattPhone)

        viewModel.receive(TtcResultSuccess(data: organizedGame(mattStatus: .declined)), viewer: TestData.organizer)

        XCTAssertNil(responseSheet(viewModel))
    }

    func testRecordingAnAnswerClosesTheSheetSendsItAndConfirmsIt() async {
        let analytics = RecordingEventManager()
        let repository = FakeGamesRepository(games: [organizedGame()])
        let toastPresenter = TtcToastPresenter()
        let viewModel = organizerViewModel(repository, toastPresenter, eventManager: analytics.eventManager)
        viewModel.onInviteeClick(inviteePhone: mattPhone)

        await viewModel.onRecordResponse(accept: true)

        XCTAssertNil(responseSheet(viewModel))
        XCTAssertEqual(repository.recordRequests.map(\.gameId), ["game-1"])
        XCTAssertEqual(repository.recordRequests.map(\.inviteePhone), [mattPhone])
        XCTAssertEqual(repository.recordRequests.map(\.accept), [true])
        XCTAssertEqual(toastPresenter.message, "Matt Chen marked as Yes")
        XCTAssertEqual(analytics.events.last, AnalyticsEvent.OrganizerResponseClick(response: .yes))
    }

    func testARecordedYesThatLosesTheLastSlotSaysTheGameJustFilledUp() async {
        let repository = FakeGamesRepository(games: [organizedGame()])
        repository.recordFailure = .gameFull
        let toastPresenter = TtcToastPresenter()
        let viewModel = organizerViewModel(repository, toastPresenter)
        viewModel.onInviteeClick(inviteePhone: mattPhone)

        await viewModel.onRecordResponse(accept: true)

        XCTAssertEqual(toastPresenter.message, "This game just filled up")
    }

    func testDismissingTheSheetClosesItAndIsLoggedOnlyWhenNothingWasPicked() async {
        let analytics = RecordingEventManager()
        let viewModel = organizerViewModel(FakeGamesRepository(games: [organizedGame()]), eventManager: analytics.eventManager)

        viewModel.onInviteeClick(inviteePhone: mattPhone)
        viewModel.onResponseSheetDismiss()
        XCTAssertNil(responseSheet(viewModel))

        // Picking an answer closes the sheet too; a dismiss that follows it isn't another.
        viewModel.onInviteeClick(inviteePhone: mattPhone)
        await viewModel.onRecordResponse(accept: false)
        viewModel.onResponseSheetDismiss()

        XCTAssertEqual(
            analytics.events.filter { $0 is AnalyticsEvent.OrganizerResponseSheetDismiss },
            [AnalyticsEvent.OrganizerResponseSheetDismiss.shared]
        )
    }

    func testNoAnswerIsSentWhileNoSheetIsOpen() async {
        let repository = FakeGamesRepository(games: [organizedGame()])
        let viewModel = organizerViewModel(repository)

        await viewModel.onRecordResponse(accept: true)

        XCTAssertTrue(repository.recordRequests.isEmpty)
    }
}
