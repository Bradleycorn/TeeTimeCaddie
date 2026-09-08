//
//  AddTeeTimeViewModel.swift
//  TeeTimeCaddie
//
//  Created by Bradley Ball on 1/10/26.
//

import Foundation
import TeeTimeCaddieKit
import Factory

@Observable
class AddTeeTimeViewModel {
    private let teeTimesRepo: TeeTimesRepository
    private let authRepo: AuthRepository
    private let eventManager: EventManager


    init(
        teeTimesRepo: TeeTimesRepository = TeeTimesModule.shared.teeTimesRepository(),
        authRepo: AuthRepository = AuthModule.shared.authRepository(),
        eventManager: EventManager = AppModule.shared.eventManager()
    ) {
        self.teeTimesRepo = teeTimesRepo
        self.authRepo = authRepo
        self.eventManager = eventManager
    }
}
