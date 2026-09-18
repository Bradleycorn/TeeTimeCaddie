package net.bradball.teetimecaddie.core.extensions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PhoneExtensionsTest {

    @Test
    fun digitsOnly_stripsEverythingThatIsNotADigit() {
        assertEquals("5025551234", "(502) 555-1234".digitsOnly)
        assertEquals("5025551234", "+1 502.555.1234".digitsOnly.removePrefix("1"))
        assertEquals("", "no digits here".digitsOnly)
    }

    // Every way the same number can be written must normalize identically, or an invitation sent
    // to a contact's "+1 415 555 0101" will not match the "(415) 555-0101" they typed at sign-up.
    @Test
    fun toPhoneDigits_normalizesEveryWritingOfTheSameNumber() {
        val expected = "5025551234"
        listOf(
            "5025551234",
            "(502) 555-1234",
            "502-555-1234",
            "502.555.1234",
            "+1 502-555-1234",
            "1 502 555 1234",
            "  (502) 555 1234  ",
        ).forEach { written ->
            assertEquals(expected, written.toPhoneDigits(), "failed for: $written")
        }
    }

    @Test
    fun toPhoneDigits_onlyStripsALeadingOneWhenItIsACountryCode() {
        // 11 digits starting with 1 -> country code, dropped.
        assertEquals("5025551234", "15025551234".toPhoneDigits())
        // 10 digits starting with 1 -> a real area code, kept.
        assertEquals("1025551234", "1025551234".toPhoneDigits())
        // 11 digits not starting with 1 -> not a country code, left alone.
        assertEquals("25025551234", "25025551234".toPhoneDigits())
    }

    @Test
    fun isValidPhoneNumber_requiresExactlyTenDigits() {
        assertTrue("5025551234".isValidPhoneNumber)
        assertTrue("(502) 555-1234".isValidPhoneNumber)
        assertTrue("+1 502 555 1234".isValidPhoneNumber)

        // Nine digits keeps the Create account button off (AC).
        assertFalse("502555123".isValidPhoneNumber)
        assertFalse("".isValidPhoneNumber)
        assertFalse("502555123456".isValidPhoneNumber)
    }

    // The as-you-type contract. The important case is the absence of a trailing separator: if
    // "502" formatted to "(502) ", backspacing would re-format to the same string and the field
    // would appear frozen.
    @Test
    fun formattedPhoneNumber_formatsProgressivelyWithNoTrailingSeparator() {
        assertEquals("", "".formattedPhoneNumber)
        assertEquals("5", "5".formattedPhoneNumber)
        assertEquals("50", "50".formattedPhoneNumber)
        assertEquals("502", "502".formattedPhoneNumber)
        assertEquals("(502) 5", "5025".formattedPhoneNumber)
        assertEquals("(502) 555", "502555".formattedPhoneNumber)
        assertEquals("(502) 555-1", "5025551".formattedPhoneNumber)
        assertEquals("(502) 555-1234", "5025551234".formattedPhoneNumber)
    }

    @Test
    fun formattedPhoneNumber_stopsAtTenDigits() {
        assertEquals("(502) 555-1234", "50255512349999".formattedPhoneNumber)
    }

    @Test
    fun formattedPhoneNumber_isIdempotentSoReformattingItsOwnOutputIsSafe() {
        val once = "5025551234".formattedPhoneNumber
        assertEquals(once, once.formattedPhoneNumber)

        val partial = "5025".formattedPhoneNumber
        assertEquals(partial, partial.formattedPhoneNumber)
    }

    @Test
    fun formattedPhoneNumber_roundTripsBackToTheStoredDigits() {
        val digits = "5025551234"
        assertEquals(digits, digits.formattedPhoneNumber.toPhoneDigits())
    }
}
