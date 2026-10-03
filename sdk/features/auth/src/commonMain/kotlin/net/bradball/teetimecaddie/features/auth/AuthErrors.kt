package net.bradball.teetimecaddie.features.auth

import dev.icerock.moko.resources.StringResource
import net.bradball.teetimecaddie.core.models.GR

/**
 * The ways authentication can fail, as the app needs to talk about them.
 *
 * Replaces a taxonomy that collapsed every sign-in failure into one case, so a dropped connection
 * and a disabled account both read to the user as "wrong password".
 *
 * Note what is *absent*: there is no "no account for that email" case. With Firebase email
 * enumeration protection enabled, the sign-in endpoint cannot distinguish an unknown address from
 * a wrong password, so [INVALID_CREDENTIALS] covers both. Profile-side failures — a phone number
 * already in use, a blank name — are not authentication failures and live in the players feature's
 * own error type.
 *
 * @property title A short headline for the failure, suitable for the first line of a message block
 *   or the title of an alert. Never a sentence.
 * @property message The explanation shown under [title]. May contain a `%s` placeholder, in which
 *   case `AuthException.messageArgs` carries the value — the SDK hands these over unformatted so
 *   each platform can format natively. Currently only [EMAIL_IN_USE] uses one, for the address.
 * @property recovery The label for the action that gets the person out of this state, or null when
 *   there is nothing to offer beyond trying again. This is button text, not prose.
 */
enum class AuthErrors(
    val title: StringResource,
    val message: StringResource,
    val recovery: StringResource? = null
) {
    /** Sign-in failed. Which half was wrong is not knowable — see the note above. */
    INVALID_CREDENTIALS(
        AR.strings.login_error_invalid_credentials_title,
        AR.strings.login_error_invalid_credentials_message
    ),

    /** Sign-up rejected: the address already has an account. Carries the email as a message arg. */
    EMAIL_IN_USE(
        AR.strings.reg_error_email_in_use_title,
        AR.strings.reg_error_email_in_use_message,
        AR.strings.reg_error_email_in_use_recovery
    ),

    INVALID_EMAIL(
        AR.strings.auth_error_title,
        AR.strings.reg_error_invalid_email_message,
        AR.strings.reg_error_invalid_email_recovery
    ),

    WEAK_PASSWORD(
        AR.strings.auth_error_title,
        AR.strings.reg_error_weak_password_message,
        AR.strings.reg_error_weak_password_recovery
    ),

    ACCOUNT_DISABLED(
        AR.strings.auth_error_account_disabled_title,
        AR.strings.auth_error_account_disabled_message
    ),

    TOO_MANY_ATTEMPTS(
        AR.strings.auth_error_too_many_attempts_title,
        AR.strings.auth_error_too_many_attempts_message
    ),

    NETWORK(
        AR.strings.auth_error_network_title,
        AR.strings.auth_error_network_message
    ),

    /** The Firebase session went away underneath an operation that needed it. */
    SESSION_EXPIRED(
        AR.strings.auth_error_session_expired_title,
        AR.strings.auth_error_session_expired_message
    ),

    UNKNOWN(
        GR.strings.error_default_title,
        GR.strings.error_default_message,
        GR.strings.error_default_recovery
    );

    companion object {
        val default = UNKNOWN

        /**
         * The error a sign-in failure's code means.
         *
         * Pure, and takes a plain `String?`, so it is unit-testable without a Firebase runtime —
         * which matters, because this is the one place the two platforms' error vocabularies have
         * to agree. Android surfaces `ERROR_*` strings; iOS surfaces bare NSError numbers, because
         * GitLive discards the error object and keeps only its description.
         */
        internal fun fromSignInErrorCode(code: String?): AuthErrors = when (code) {
            // Email enumeration protection collapses "no such user" and "wrong password" into one
            // code. The older, more specific codes still arrive from projects that have it off,
            // and all three land on the same case — the UI must not be able to tell them apart.
            "ERROR_INVALID_LOGIN_CREDENTIALS", "ERROR_INVALID_CREDENTIAL", "17004" -> INVALID_CREDENTIALS
            "ERROR_USER_NOT_FOUND", "17011" -> INVALID_CREDENTIALS
            "ERROR_WRONG_PASSWORD", "17009" -> INVALID_CREDENTIALS

            "ERROR_INVALID_EMAIL", "17008" -> INVALID_EMAIL
            "ERROR_USER_DISABLED", "17005" -> ACCOUNT_DISABLED
            "ERROR_TOO_MANY_REQUESTS", "17010", "17052" -> TOO_MANY_ATTEMPTS
            "ERROR_NETWORK_REQUEST_FAILED", "17020" -> NETWORK
            "ERROR_USER_TOKEN_EXPIRED", "ERROR_INVALID_USER_TOKEN", "17021", "17017" -> SESSION_EXPIRED

            else -> UNKNOWN
        }

        /**
         * The error an account-creation failure's code means.
         *
         * Note `EMAIL_EXISTS` survives email enumeration protection — Google documents sign-up as
         * the one endpoint that still reports it. That is what lets the credentials screen reject
         * an in-use address before the profile step rather than after it.
         */
        internal fun fromCreateAccountErrorCode(code: String?): AuthErrors = when (code) {
            "ERROR_EMAIL_ALREADY_IN_USE", "17007" -> EMAIL_IN_USE
            "ERROR_WEAK_PASSWORD", "17026" -> WEAK_PASSWORD
            "ERROR_INVALID_EMAIL", "17008" -> INVALID_EMAIL
            "ERROR_TOO_MANY_REQUESTS", "17010", "17052" -> TOO_MANY_ATTEMPTS
            "ERROR_NETWORK_REQUEST_FAILED", "17020" -> NETWORK
            else -> UNKNOWN
        }
    }
}
