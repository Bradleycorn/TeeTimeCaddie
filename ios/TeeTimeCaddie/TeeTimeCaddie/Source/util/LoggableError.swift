import Foundation
import TeeTimeCaddieKit


protocol LoggableError: Error {
    var logInfo: [String:Any] { get }
    var type: LoggableExceptionTypes { get }
    var cause: Error? { get }
}

extension LoggableError {
    var logInfo: Dictionary<String, AnyObject?> {
        Dictionary()
    }
    
    func getLogData() -> [String: Any] {
        
        var data: [String: Any] = [:]
        data.merge(logInfo, uniquingKeysWith: {_, new in new})
        

        var child: Error? = cause
        while let loggable = child as? LoggableError {
            data.merge(loggable.logInfo, uniquingKeysWith: {_, new in new})
            child = loggable.cause
        }
        
        return data
    }
}

extension EventManager {
    func logError(_ error: LoggableError) {
        self.logError(error: error, errorType: error.type, data: error.getLogData())
    }
}
