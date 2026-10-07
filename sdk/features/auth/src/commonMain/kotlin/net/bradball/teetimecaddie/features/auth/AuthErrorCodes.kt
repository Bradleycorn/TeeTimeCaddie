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
 * NSError codes); [signInErrorFor] and [createAccountErrorFor] accept both.
 */
internal expect fun Throwable.firebaseAuthErrorCode(): String?
