package net.bradball.teetimecaddie.features.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Reading error codes back out of iOS `NSError` descriptions, the only form GitLive keeps. */
class AuthErrorCodesTest {

    @Test
    fun readsTheNumericCode() {
        val description = """Error Domain=FIRAuthErrorDomain Code=17011 "There is no user record corresponding to this identifier." UserInfo={FIRAuthErrorUserInfoNameKey=ERROR_USER_NOT_FOUND}"""

        assertEquals("17011", authErrorCodeFromNSErrorDescription(description))
    }

    // Captured from a token refresh for a deleted user against the Auth emulator (TTC-127). The
    // iOS SDK has no code for INVALID_REFRESH_TOKEN, so it reports 17999 and keeps the response.
    @Test
    fun readsTheServerMessageFromAnInternalError() {
        val description = """Error Domain=FIRAuthErrorDomain Code=17999 "An internal error has occurred, print and inspect the error details for more information." UserInfo={NSUnderlyingError=0x600000c5f810 {Error Domain=FIRAuthInternalErrorDomain Code=3 "(null)" UserInfo={FIRAuthErrorUserInfoDeserializedResponseKey={
    code = 400;
    errors =     (
                {
            domain = global;
            message = "INVALID_REFRESH_TOKEN";
            reason = invalid;
        }
    );
    message = "INVALID_REFRESH_TOKEN";
}}}, FIRAuthErrorUserInfoNameKey=ERROR_INTERNAL_ERROR}"""

        val code = authErrorCodeFromNSErrorDescription(description)

        assertEquals("INVALID_REFRESH_TOKEN", code)
        assertTrue(AuthErrors.rejectsSession(code))
    }

    // An internal error with no server response behind it says nothing about the account.
    @Test
    fun keepsTheInternalCodeWhenThereIsNoServerMessage() {
        val description = """Error Domain=FIRAuthErrorDomain Code=17999 "An internal error has occurred." UserInfo={FIRAuthErrorUserInfoNameKey=ERROR_INTERNAL_ERROR}"""

        assertEquals("17999", authErrorCodeFromNSErrorDescription(description))
    }

    @Test
    fun returnsNullForAnythingWithoutACode() {
        assertNull(authErrorCodeFromNSErrorDescription(null))
        assertNull(authErrorCodeFromNSErrorDescription("Something unrelated went wrong"))
    }
}
