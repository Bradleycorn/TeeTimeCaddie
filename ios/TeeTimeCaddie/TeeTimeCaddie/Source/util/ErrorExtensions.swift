//
//  ErrorExtensions.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 8/8/23.
//

import Foundation
import SwiftUI
import TeeTimeCaddieKit

class UnknownException: TeeTimeCaddieException {
    let title: StringResource
    let displayMessage: StringResource
    let recoverySuggestion: StringResource?
    let error: Error
    let logInfo: KotlinMutableDictionary<NSString, AnyObject>
    let loggableType: LoggableExceptionTypes = .interop
        
    init(error: Error,
         title: StringResource,
         displayMessage: StringResource,
         recoverySuggestion: StringResource?,
         logInfo: [String:Any] = [:]) {
                
        self.error = error
        self.title = title
        self.displayMessage = displayMessage
        self.recoverySuggestion = recoverySuggestion

        let info = KotlinMutableDictionary<NSString, AnyObject>()
        info.addEntries(from: logInfo)
        self.logInfo = info
    }

    func getLogData() -> KotlinMutableDictionary<NSString, AnyObject> {
        logInfo
    }
}

extension Error {
    func asTeeTimeCaddieException(
        defaultTitle: StringResource = GR.strings().error_default_title,
        defaultMessage: StringResource = GR.strings().error_default_message,
        defaultRecoveryMessage: StringResource? = GR.strings().error_default_recovery
    ) -> TeeTimeCaddieException {
        
        guard let ex = (self as NSError).userInfo["KotlinException"] as? TeeTimeCaddieException else {
            AppModule.shared.eventManager().logError(error: self, errorType: .interop, data: ["reason": "Error from Kotlin was not a TeeTimeCaddieException"])
            return UnknownException(error: self, title: defaultTitle,  displayMessage: defaultMessage, recoverySuggestion: defaultRecoveryMessage)
        }
        print("ERROR MESSAGE - \(ex.displayMessage.desc().localized())")
        return ex
    }
    
    func asTeeTimeCaddieError(
        defaultTitle: StringResource = GR.strings().error_default_title,
        defaultMessage: StringResource = GR.strings().error_default_message,
        defaultRecoveryMessage: StringResource? = GR.strings().error_default_recovery
    ) -> TeeTimeCaddieError {
        let ex = self.asTeeTimeCaddieException(defaultTitle: defaultTitle, defaultMessage: defaultMessage, defaultRecoveryMessage: defaultRecoveryMessage)
        return TeeTimeCaddieError(exception: ex)
    }
}

 
struct TeeTimeCaddieError: LocalizedError {
    let exception: TeeTimeCaddieException

    var errorDescription: String? {
        exception.title.desc().localized()
    }
    
    var failureReason: String? {
        exception.displayMessage.desc().localized()
    }
    
    var recoverySuggestion: String? {
        exception.recoverySuggestion?.desc().localized()
    }
    
    var logMessage: String {
        guard let title = errorDescription, let message = failureReason else {
            return "An Error Occured. No title or message provided."
        }
        return "\(title) - \(message)"
    }
}

extension View {
    func errorAlert<E: LocalizedError>(error: Binding<E?>, buttonTitle: String = "OK") -> some View {
        
        
        
        
        return alert(
            isPresented: .constant(error.wrappedValue != nil),
            error: error.wrappedValue,
            actions: { _ in
                Button(buttonTitle) {
                    error.wrappedValue = nil
                }
            },
            message: { error in
                Text(error.displayMessage)
            })
    }
}

extension LocalizedError {
    var displayMessage: String {
        var message = failureReason ?? GR.strings().error_default_message.desc().localized()
        if let recovery = recoverySuggestion {
            message.append("\n\n\(recovery)")
        }
        return message
    }
}
