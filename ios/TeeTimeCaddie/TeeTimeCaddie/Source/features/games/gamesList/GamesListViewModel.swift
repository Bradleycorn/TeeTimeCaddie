//
//  GamesListViewModel.swift
//  TeeTimeCaddie
//

import Foundation
import SwiftUI
import TeeTimeCaddieKit

/// The Swift twin of `GamesListViewModel.kt`.
@MainActor
@Observable
final class GamesListViewModel {

    private(set) var uiState: any GamesListUiState = GamesListUiStateLoading.shared

    private let gamesRepository: GamesRepository
    private let currentPlayer: () -> Player?
    private let eventManager: EventManager
    private let toastPresenter: TtcToastPresenter

    // Games whose answer is on its way, so a second tap doesn't send another.
    private var answering: Set<String> = []

    init(
        gamesRepository: GamesRepository = GamesModule.shared.gamesRepository(),
        currentPlayer: @escaping () -> Player? = GamesModule.shared.currentPlayer(),
        eventManager: EventManager = AppModule.shared.eventManager(),
        toastPresenter: TtcToastPresenter = AppModule.shared.toastPresenter()
    ) {
        self.gamesRepository = gamesRepository
        self.currentPlayer = currentPlayer
        self.eventManager = eventManager
        self.toastPresenter = toastPresenter
    }

    /// Follows every game the signed-in player is in or invited to, for as long as the screen is up.
    func observeGames() async {
        guard let player = currentPlayer() else { return }
        for await games in gamesRepository.observeGames(player: player) {
            uiState = GamesListUiStateKt.toGamesListUiState(games, viewer: player)
        }
    }

    /// Records a tap on an add control (the + or "Create a game"). Navigating is the screen's job.
    func onAddGameClick(from source: AnalyticsEvent.AddGameClick.AddGameClickSource) {
        eventManager.logEvent(event: AnalyticsEvent.AddGameClick(source: source))
    }

    /// Records a tap on a game. Navigating to it is the screen's job.
    func onGameClick() {
        eventManager.logEvent(event: AnalyticsEvent.GameListItemClick.shared)
    }

    /// Answers `invite` at once, with no confirmation step. The invitation leaves the card when the
    /// games update; a toast confirms the answer, or explains why it didn't go through.
    func onAnswerInvite(_ invite: InviteRowUiState, accept: Bool) async {
        eventManager.logEvent(event: AnalyticsEvent.AnswerInviteClick(accepted: accept, source: .gamesList))
        guard let player = currentPlayer(), !answering.contains(invite.id) else { return }
        answering.insert(invite.id)
        defer { answering.remove(invite.id) }

        do {
            let result = try await gamesRepository.respondToInvite(gameId: invite.id, player: player, accept: accept)
            toastPresenter.showInviteAnswer(result, accepted: accept)
        } catch {
            toastPresenter.showInviteAnswerFailure(error.asTeeTimeCaddieException())
        }
    }
}
