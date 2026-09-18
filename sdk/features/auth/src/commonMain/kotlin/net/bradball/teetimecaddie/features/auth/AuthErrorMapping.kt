package net.bradball.teetimecaddie.features.auth

/**
 * Map a sign-in failure's error code to the case the UI renders.
 *
 * Pure, and takes a plain `String?`, so it is unit-testable without a Firebase runtime — which
 * matters, because this is the one place where the two platforms' error vocabularies have to agree.
 */
internal fun signInErrorFor(code: String?): AuthErrors = when (code) {
    // Email enumeration protection collapses "no such user" and "wrong password" into one code.
    // The older, more specific codes are still mapped for projects that have it disabled, and all
    // three land on the same case — the UI must not distinguish them.
    "ERROR_INVALID_LOGIN_CREDENTIALS", "ERROR_INVALID_CREDENTIAL", "17004" -> AuthErrors.INVALID_CREDENTIALS
    "ERROR_USER_NOT_FOUND", "17011" -> AuthErrors.INVALID_CREDENTIALS
    "ERROR_WRONG_PASSWORD", "17009" -> AuthErrors.INVALID_CREDENTIALS

    "ERROR_INVALID_EMAIL", "17008" -> AuthErrors.INVALID_EMAIL
    "ERROR_USER_DISABLED", "17005" -> AuthErrors.ACCOUNT_DISABLED
    "ERROR_TOO_MANY_REQUESTS", "17010", "17052" -> AuthErrors.TOO_MANY_ATTEMPTS
    "ERROR_NETWORK_REQUEST_FAILED", "17020" -> AuthErrors.NETWORK
    "ERROR_USER_TOKEN_EXPIRED", "ERROR_INVALID_USER_TOKEN", "17021", "17017" -> AuthErrors.SESSION_EXPIRED

    else -> AuthErrors.UNKNOWN
}

/**
 * Map an account-creation failure's error code to the case the UI renders.
 *
 * Note that `EMAIL_EXISTS` survives email enumeration protection — Google documents sign-up as the
 * one endpoint that still reports it. That is what lets the credentials screen reject an in-use
 * address before the profile step, rather than after it.
 */
internal fun createAccountErrorFor(code: String?): AuthErrors = when (code) {
    "ERROR_EMAIL_ALREADY_IN_USE", "17007" -> AuthErrors.EMAIL_IN_USE
    "ERROR_WEAK_PASSWORD", "17026" -> AuthErrors.WEAK_PASSWORD
    "ERROR_INVALID_EMAIL", "17008" -> AuthErrors.INVALID_EMAIL
    "ERROR_TOO_MANY_REQUESTS", "17010", "17052" -> AuthErrors.TOO_MANY_ATTEMPTS
    "ERROR_NETWORK_REQUEST_FAILED", "17020" -> AuthErrors.NETWORK
    else -> AuthErrors.UNKNOWN
}
