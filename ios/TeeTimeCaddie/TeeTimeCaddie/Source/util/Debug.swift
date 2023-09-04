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
