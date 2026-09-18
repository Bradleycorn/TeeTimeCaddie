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
 * a wrong password, so [INVALID_CREDENTIALS] covers both. Profile-side failures (a phone number
 * already in use, a name that is blank) are not authentication failures and live in the players
 * feature's own error type.
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
    }
}
