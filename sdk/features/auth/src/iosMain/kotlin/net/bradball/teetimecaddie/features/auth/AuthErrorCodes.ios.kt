package net.bradball.teetimecaddie.features.auth

/**
 * On iOS, GitLive discards the underlying `NSError` and keeps only its string form, so the code
 * has to be read back out of the message — see [authErrorCodeFromNSErrorDescription]. Fragile-
 * looking, but it is the only channel the code survives on, and [AuthErrors.fromSignInErrorCode] degrades to
 * [AuthErrors.UNKNOWN] if the shape ever changes, rather than mis-reporting.
 */
internal actual fun Throwable.firebaseAuthErrorCode(): String? =
    authErrorCodeFromNSErrorDescription(message) ?: authErrorCodeFromNSErrorDescription(cause?.message)
