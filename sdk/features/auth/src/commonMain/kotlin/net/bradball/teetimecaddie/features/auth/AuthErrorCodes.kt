package net.bradball.teetimecaddie.features.auth

/**
 * Extract a Firebase Auth error code from a thrown exception, or null if it carries none.
 *
 * Why code-based and not type-based: GitLive's exception mapping is asymmetric. On Android the
 * exception types are typealiases to the real Firebase Android ones, so `FirebaseAuthInvalidUser`
 * and friends arrive intact. On iOS, `appleMain` maps only a handful of NSError codes by hand and
 * never maps 17011 (user not found) or 17009 (wrong password) — both fall through to a generic
 * `FirebaseAuthException`. Catching by type would therefore behave differently on each platform.
 *
 * Codes come back in each platform's own vocabulary (Android's `ERROR_*` strings, iOS's numeric
 * NSError codes); [AuthErrors.fromSignInErrorCode] and [AuthErrors.fromCreateAccountErrorCode] accept both.
 */
internal expect fun Throwable.firebaseAuthErrorCode(): String?

/** Matches the `Code=17011` fragment NSError puts in its description. */
private val NS_ERROR_CODE = Regex("""Code=(\d+)""")

/** Matches the server's error message inside an NSError description: `message = "USER_NOT_FOUND";`. */
private val SERVER_MESSAGE = Regex("""message = "?([A-Z_]+)"?;""")

/** The iOS SDK's catch-all code, used for any server response it doesn't recognize. */
private const val IOS_INTERNAL_ERROR = "17999"

/**
 * The error code in an iOS `NSError` description, for platforms where only that string survives.
 *
 * Usually the numeric code (`17011`). The exception is the iOS SDK's catch-all, `17999`: it means
 * the server answered with a message the SDK has no code for, so the server's own message is
 * returned instead, from the response the SDK keeps in the description. A refresh of a deleted
 * user's token against the Auth emulator ends up here as `INVALID_REFRESH_TOKEN`, which Android
 * reports as `ERROR_INVALID_USER_TOKEN`.
 *
 * Pure, and in common code rather than `iosMain`, so it can be tested against real descriptions.
 */
internal fun authErrorCodeFromNSErrorDescription(description: String?): String? {
    val code = description?.let { NS_ERROR_CODE.find(it)?.groupValues?.getOrNull(1) } ?: return null
    if (code != IOS_INTERNAL_ERROR) return code
    return SERVER_MESSAGE.find(description)?.groupValues?.getOrNull(1) ?: code
}
