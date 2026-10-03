package net.bradball.teetimecaddie.core.extensions

val String.Companion.empty: String
    get() = ""

/**
 * Matches text that at least *looks* like an email address: something, an `@`, something, a dot,
 * something — with no whitespace anywhere.
 *
 * Deliberately loose. This gates whether the credentials screen's buttons are enabled, so its job
 * is only to keep a half-typed address from enabling them. Whether the address actually exists is
 * the auth service's answer, not a regex's.
 */
private val EMAIL_REGEX = Regex("""\S+@\S+\.\S+""")

/** True when this string, trimmed, looks like an email address. See [EMAIL_REGEX]. */
val String.isValidEmail: Boolean
    get() = EMAIL_REGEX.matches(trim())
