//
//  StringExtensions.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 3/25/24.
//

import Foundation
import TeeTimeCaddieKit

extension String {
    /// True when this string, trimmed, looks like an email address.
    ///
    /// Delegates to the SDK rather than restating the pattern here. "What counts as an email" is a
    /// validation rule both apps have to agree on, and a second regex in Swift is a second thing to
    /// keep in step. The Kotlin twin is `String.isValidEmail`.
    var isValidEmail: Bool {
        StringExtensionsKt.isValidEmail(self)
    }

    func insert(_ text: String, at index: Int) -> String {
        var newString = self
        newString.insert(contentsOf: text, at: newString.index(startIndex, offsetBy: index))
        return newString
    }
}

extension StringResource {
    /// This resource, resolved for the current locale.
    ///
    /// `desc().localized()` is the moko incantation; wrapping it keeps that out of call sites, which
    /// otherwise read as plumbing rather than as the string they are fetching.
    func localized() -> String {
        desc().localized()
    }

    /// This resource with its `%s` placeholders filled, resolved for the current locale.
    ///
    /// The SDK hands placeholders over unformatted so the apps can resolve them in the viewer's
    /// locale — see `AuthException.messageArgs`.
    func localized(_ args: Any...) -> String {
        localized(args: args)
    }

    /// As ``localized(_:)``, for arguments already in an array — `messageArgs`, typically.
    func localized(args: [Any]) -> String {
        args.isEmpty ? localized() : format(args: args).localized()
    }
}
