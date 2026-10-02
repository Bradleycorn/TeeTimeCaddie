//
//  PhoneNumberTests.swift
//  TeeTimeCaddieTests
//

import SwiftUI
import XCTest
@testable import TeeTimeCaddie

/// The Swift half of a shared contract.
///
/// `PhoneExtensionsTest.kt` asserts the same cases against the Kotlin twin. The two must agree:
/// a number typed on one platform has to read the same on the other, and reach storage identically.
final class PhoneNumberTests: XCTestCase {

    // MARK: - digits

    func testDigitsStripsEverythingButDigits() {
        XCTAssertEqual(PhoneNumber.digits("(502) 555-1234"), "5025551234")
        XCTAssertEqual(PhoneNumber.digits("502.555.1234"), "5025551234")
        XCTAssertEqual(PhoneNumber.digits("502 555 1234"), "5025551234")
    }

    /// A US area code cannot begin with 1, so a leading 1 on eleven digits is the country code.
    func testDigitsDropsALeadingCountryCode() {
        XCTAssertEqual(PhoneNumber.digits("+1 502-555-1234"), "5025551234")
        XCTAssertEqual(PhoneNumber.digits("15025551234"), "5025551234")
    }

    /// Ten digits beginning with 1 are a real number, not a country code plus nine.
    func testDigitsKeepsALeadingOneOnACompleteNumber() {
        XCTAssertEqual(PhoneNumber.digits("1025551234"), "1025551234")
    }

    func testDigitsStopsAtAFullNumber() {
        XCTAssertEqual(PhoneNumber.digits("50255512349999"), "5025551234")
    }

    func testDigitsOfEmptyIsEmpty() {
        XCTAssertEqual(PhoneNumber.digits(""), "")
        XCTAssertEqual(PhoneNumber.digits("abc"), "")
    }

    // MARK: - formatted

    /// The rule that makes this usable mid-typing: punctuation never lands ahead of the caret.
    func testFormattedNeverEndsWithASeparator() {
        XCTAssertEqual(PhoneNumber.formatted("5"), "(5")
        XCTAssertEqual(PhoneNumber.formatted("555"), "(555")
        XCTAssertEqual(PhoneNumber.formatted("5551"), "(555) 1")
        XCTAssertEqual(PhoneNumber.formatted("555123"), "(555) 123")
        XCTAssertEqual(PhoneNumber.formatted("5551234"), "(555) 123-4")
    }

    func testFormattedCompleteNumber() {
        XCTAssertEqual(PhoneNumber.formatted("5025551234"), "(502) 555-1234")
    }

    func testFormattedIsIdempotent() {
        let once = PhoneNumber.formatted("5025551234")
        XCTAssertEqual(PhoneNumber.formatted(once), once)
    }

    func testFormattedOfEmptyIsEmpty() {
        XCTAssertEqual(PhoneNumber.formatted(""), "")
    }

    // MARK: - binding

    /// Typing left to right must land the digits in the order they were typed — the bug Android hit
    /// by rewriting the field's value instead of only its presentation.
    func testBindingKeepsDigitsInTypedOrder() {
        var stored = ""
        let binding = PhoneNumber.binding(
            to: Binding(get: { stored }, set: { stored = $0 })
        )

        for digit in "5025551234" {
            binding.wrappedValue = binding.wrappedValue + String(digit)
        }

        XCTAssertEqual(stored, "5025551234")
        XCTAssertEqual(binding.wrappedValue, "(502) 555-1234")
    }

    func testBindingStoresDigitsAndShowsFormatted() {
        var stored = "5025551234"
        let binding = PhoneNumber.binding(
            to: Binding(get: { stored }, set: { stored = $0 })
        )

        XCTAssertEqual(binding.wrappedValue, "(502) 555-1234")

        binding.wrappedValue = "(502) 555-12"
        XCTAssertEqual(stored, "50255512")
    }

    func testBindingRefusesMoreThanAFullNumber() {
        var stored = ""
        let binding = PhoneNumber.binding(
            to: Binding(get: { stored }, set: { stored = $0 })
        )

        binding.wrappedValue = "502555123499"
        XCTAssertEqual(stored, "5025551234")
    }
}
