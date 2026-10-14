package net.bradball.teetimecaddie.features.auth

/** Matches the `Code=17011` fragment NSError puts in its description. */
private val NS_ERROR_CODE = Regex("""Code=(\d+)""")

/**
 * On iOS, GitLive discards the underlying `NSError` and keeps only its string form, so the numeric
 * code has to be read back out of the message. Fragile-looking, but it is the only channel the
 * code survives on — and [signInErrorFor] degrades to [AuthErrors.UNKNOWN] if the shape ever
 * changes, rather than mis-reporting.
 */
internal actual fun Throwable.firebaseAuthErrorCode(): String? =
    message?.let { NS_ERROR_CODE.find(it)?.groupValues?.getOrNull(1) }
        ?: cause?.message?.let { NS_ERROR_CODE.find(it)?.groupValues?.getOrNull(1) }
