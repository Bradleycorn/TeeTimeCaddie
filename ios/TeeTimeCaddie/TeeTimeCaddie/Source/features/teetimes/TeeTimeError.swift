import Foundation
import TeeTimeCaddieKit

enum TeeTimeError {
    case dateError(_ date: Date)
}

extension TeeTimeError: LocalizedError {
    var errorDescription: String? {
        switch self {
            case .dateError:
                return "Invalid Date"
        }
    }
    
    var failureReason: String? {
        switch self {
            case .dateError:
                return "Could not create a tee time with the date provided."
        }
    }
}

extension TeeTimeError: LoggableError {
    var type: LoggableExceptionTypes { .teetimes }
    
    var cause: Error? { nil }
    
    var logInfo: [String : Any] {
        switch self {
            case let .dateError(date):
                return ["date" : date.description]
        }
    }
}
