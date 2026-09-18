package net.bradball.teetimecaddie.features.auth

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The one place the two platforms' error vocabularies have to agree.
 *
 * Android surfaces Firebase's `ERROR_*` strings; iOS surfaces bare NSError numbers, because
 * GitLive discards the error object and keeps only its description. Both have to land on the same
 * [AuthErrors].
 */
class AuthErrorMappingTest {

    @Test
    fun signIn_mapsBothPlatformsCodesIdentically() {
        val pairs = listOf(
            "ERROR_INVALID_EMAIL" to "17008",
            "ERROR_USER_DISABLED" to "17005",
            "ERROR_TOO_MANY_REQUESTS" to "17010",
            "ERROR_NETWORK_REQUEST_FAILED" to "17020",
        )
        pairs.forEach { (android, ios) ->
            assertEquals(
                signInErrorFor(android),
                signInErrorFor(ios),
                "$android and $ios should map to the same error"
            )
        }
    }

    // With email enumeration protection on, Firebase reports one code for both "no such account"
    // and "wrong password". The older, more specific codes still arrive from projects that have it
    // off — and all three must land on the same case, or the UI would disclose which half was
    // wrong depending on a project setting.
    @Test
    fun signIn_collapsesEveryCredentialFailureIntoOneCase() {
        listOf(
            "ERROR_INVALID_LOGIN_CREDENTIALS",
            "ERROR_INVALID_CREDENTIAL",
            "17004",
            "ERROR_USER_NOT_FOUND",
            "17011",
            "ERROR_WRONG_PASSWORD",
            "17009",
        ).forEach { code ->
            assertEquals(
                AuthErrors.INVALID_CREDENTIALS,
                signInErrorFor(code),
                "$code must not be distinguishable from any other credential failure"
            )
        }
    }

    @Test
    fun signIn_fallsBackToUnknownForAnythingUnrecognized() {
        assertEquals(AuthErrors.UNKNOWN, signInErrorFor(null))
        assertEquals(AuthErrors.UNKNOWN, signInErrorFor(""))
        assertEquals(AuthErrors.UNKNOWN, signInErrorFor("ERROR_SOMETHING_NEW"))
        assertEquals(AuthErrors.UNKNOWN, signInErrorFor("99999"))
    }

    // Sign-up is the one endpoint that still reports a taken address under email enumeration
    // protection. AC 3 — rejecting an in-use email before the profile step — depends on it.
    @Test
    fun createAccount_reportsAnEmailAlreadyInUse() {
        assertEquals(AuthErrors.EMAIL_IN_USE, createAccountErrorFor("ERROR_EMAIL_ALREADY_IN_USE"))
        assertEquals(AuthErrors.EMAIL_IN_USE, createAccountErrorFor("17007"))
    }

    @Test
    fun createAccount_mapsBothPlatformsCodesIdentically() {
        val pairs = listOf(
            "ERROR_EMAIL_ALREADY_IN_USE" to "17007",
            "ERROR_WEAK_PASSWORD" to "17026",
            "ERROR_INVALID_EMAIL" to "17008",
            "ERROR_NETWORK_REQUEST_FAILED" to "17020",
        )
        pairs.forEach { (android, ios) ->
            assertEquals(
                createAccountErrorFor(android),
                createAccountErrorFor(ios),
                "$android and $ios should map to the same error"
            )
        }
    }

    @Test
    fun createAccount_fallsBackToUnknownForAnythingUnrecognized() {
        assertEquals(AuthErrors.UNKNOWN, createAccountErrorFor(null))
        assertEquals(AuthErrors.UNKNOWN, createAccountErrorFor("ERROR_SOMETHING_NEW"))
    }
}
