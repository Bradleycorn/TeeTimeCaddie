//
//  Debug.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 8/26/23.
//

import Foundation

var IS_DEBUG_BUILD: Bool {
    #if DEBUG
        return true
    #else
        return false
    #endif
}

var IS_PREVIEW: Bool {
    return ProcessInfo.processInfo.environment["XCODE_RUNNING_FOR_PREVIEWS"] == "1" || ProcessInfo.processInfo.environment["XCODE_RUNNING_FOR_PLAYGROUNDS"] == "1"
}
