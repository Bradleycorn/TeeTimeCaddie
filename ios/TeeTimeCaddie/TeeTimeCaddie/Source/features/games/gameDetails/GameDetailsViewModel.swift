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
    /// answering, or the game filling, shows here without a refresh.
    ///
    /// With nobody signed in there is no view of the game to show. Signing out tears this screen
    /// down, so until it does the screen keeps what it had.
    func observeGame() async {
        guard let viewer = currentPlayer() else { return }
        for await result in gamesRepository.observeGame(gameId: gameId) {
            switch onEnum(of: result) {
            case .success(let success):
                uiState = success.data.toDetailsUiState(viewer: viewer, respondingFor: nil)
            case .failure(let failure):
                uiState = GameDetailsUiStateError(error: failure.error)
            }
        }
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
}
