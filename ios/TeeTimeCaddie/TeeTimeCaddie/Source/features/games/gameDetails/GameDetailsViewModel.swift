//
//  GameDetailsViewModel.swift
//  TeeTimeCaddie
//

import Foundation
import SwiftUI
import TeeTimeCaddieKit

/// The Swift twin of `GameDetailsViewModel.kt`.
@MainActor
@Observable
final class GameDetailsViewModel {

    private(set) var uiState: any GameDetailsUiState = GameDetailsUiStateLoading.shared

    private let gameId: String
    private let gamesRepository: GamesRepository
    private let currentPlayer: () -> Player?
    private let eventManager: EventManager
    private let toastPresenter: TtcToastPresenter

    // Whether an answer is on its way, so a second tap doesn't send another.
    private var answering = false

    // The game as last read, and who it was read for, so the state can be rebuilt when the
    // response sheet opens or closes without waiting for the game to change.
    private var latestGame: Game?
    private var viewer: Player?

    // The number of the invitee whose response sheet the organizer has open, or nil while it's
    // closed. Ephemeral: it lives as long as this screen.
    private var respondingFor: String? {
        didSet { refreshUiState() }
    }

    // Whether the organizer's answer for an invitee is on its way, so a second pick doesn't send
    // another.
    private var recordingResponse = false

    init(
        gameId: String,
        gamesRepository: GamesRepository = GamesModule.shared.gamesRepository(),
        currentPlayer: @escaping () -> Player? = GamesModule.shared.currentPlayer(),
        eventManager: EventManager = AppModule.shared.eventManager(),
        toastPresenter: TtcToastPresenter = AppModule.shared.toastPresenter()
    ) {
        self.gameId = gameId
        self.gamesRepository = gamesRepository
        self.currentPlayer = currentPlayer
        self.eventManager = eventManager
        self.toastPresenter = toastPresenter
    }

    /// Follows the game for as long as the screen is up, as the signed-in player sees it: someone
    /// answering, or the game filling, shows here without a refresh. The organizer's response
    /// sheet is part of it while it's open, and keeps up the same way.
    ///
    /// With nobody signed in there is no view of the game to show. Signing out tears this screen
    /// down, so until it does the screen keeps what it had.
    func observeGame() async {
        guard let viewer = currentPlayer() else { return }
        for await result in gamesRepository.observeGame(gameId: gameId) {
            receive(result, viewer: viewer)
        }
    }

    /// Takes in one reading of the game for [viewer]. The game's stream feeds this; tests, which
    /// can't build a Kotlin `Flow` in Swift, call it directly.
    func receive(_ result: TtcResult<Game>, viewer: Player) {
        self.viewer = viewer
        switch onEnum(of: result) {
        case .success(let success):
            latestGame = success.data
            refreshUiState()
        case .failure(let failure):
            latestGame = nil
            uiState = GameDetailsUiStateError(error: failure.error)
        }
    }

    private func refreshUiState() {
        guard let latestGame, let viewer else { return }
        uiState = latestGame.toDetailsUiState(viewer: viewer, respondingFor: respondingFor)
    }

    /// Answers the signed-in player's invitation, staying on this screen. The RSVP card collapses
    /// when the game updates; a toast confirms the answer, or explains why it didn't go through.
    func onAnswer(accept: Bool) async {
        eventManager.logEvent(event: AnalyticsEvent.AnswerInviteClick(accepted: accept, source: .gameDetails))
        guard let player = currentPlayer(), !answering else { return }
        answering = true
        defer { answering = false }

        do {
            let result = try await gamesRepository.respondToInvite(gameId: gameId, player: player, accept: accept)
            toastPresenter.showInviteAnswer(result, accepted: accept)
        } catch {
            toastPresenter.showInviteAnswerFailure(error.asTeeTimeCaddieException())
        }
    }

    /// The organizer tapped an invitee who hasn't answered, or their "more" button: opens the sheet
    /// for recording their answer.
    ///
    /// - Parameter inviteePhone: The row's `inviteePhone`.
    func onInviteeClick(inviteePhone: String) {
        eventManager.logEvent(event: AnalyticsEvent.InviteeRowClick.shared)
        respondingFor = inviteePhone
    }

    /// The response sheet closed without Yes or No being picked.
    func onResponseSheetDismiss() {
        guard respondingFor != nil else { return }
        eventManager.logEvent(event: AnalyticsEvent.OrganizerResponseSheetDismiss.shared)
        respondingFor = nil
    }

    /// Records the organizer's Yes (`accept`) or No for the invitee whose response sheet is open,
    /// and closes it. The roster moves when the game updates; a toast confirms the answer, or
    /// explains why it didn't go through.
    func onRecordResponse(accept: Bool) async {
        eventManager.logEvent(event: AnalyticsEvent.OrganizerResponseClick(accepted: accept))
        guard let responseSheetUiState = ((uiState as? GameDetailsUiStateContent)?.game as? GameUiStateOrganizer)?
            .responseSheet else { return }
        respondingFor = nil
        guard let organizer = currentPlayer(), !recordingResponse else { return }
        recordingResponse = true
        defer { recordingResponse = false }

        do {
            let result = try await gamesRepository.recordResponse(
                gameId: gameId,
                organizer: organizer,
                inviteePhone: responseSheetUiState.inviteePhone,
                accept: accept
            )
            switch onEnum(of: result) {
            case .success:
                toastPresenter.show(responseSheetUiState.recordedMessage(accepted: accept).localized())
            case .failure(let failure):
                toastPresenter.showInviteAnswerFailure(failure.error)
            }
        } catch {
            toastPresenter.showInviteAnswerFailure(error.asTeeTimeCaddieException())
        }
    }
}
