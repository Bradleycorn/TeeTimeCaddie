package net.bradball.teetimecaddie.core.extensions

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringExtensionsTest {

    @Test
    fun isValidEmail_acceptsAddressesThatLookLikeAddresses() {
        assertTrue("dana@golf.app".isValidEmail)
        assertTrue("brad.ball+tag@example.co.uk".isValidEmail)
        assertTrue("  dana@golf.app  ".isValidEmail, "surrounding whitespace should be trimmed")
    }

    // Each of these is a state a person passes through while typing, and the credentials screen's
    // buttons must stay disabled for all of them (AC: "a half-typed address keeps both off").
    @Test
    fun isValidEmail_rejectsHalfTypedAddresses() {
        listOf(
            "",
            "d",
            "dana",
            "dana@",
            "dana@golf",      // no dot yet
            "@golf.app",      // nothing before the @
            "dana@.app",      // nothing between @ and dot
            "danagolf.app",   // no @
            "dana golf@a.app" // whitespace inside
        ).forEach { halfTyped ->
            assertFalse(halfTyped.isValidEmail, "should be invalid: '$halfTyped'")
        }
    }
}
