package net.bradball.teetimecaddie.core.extensions

/** The number of digits in a phone number the app considers complete. */
const val PHONE_NUMBER_LENGTH = 10

/**
 * This string with every non-digit character removed.
 *
 * `"(502) 555-1234".digitsOnly == "5025551234"`
 */
val String.digitsOnly: String
    get() = filter { it.isDigit() }

/**
 * This string reduced to the canonical digits-only form the app stores phone numbers in.
 *
 * Strips punctuation and a leading US country code, so every way a person might write or a
 * contact might store the same number normalizes to the same value:
 *
 * ```
 * "(502) 555-1234".toPhoneDigits()  == "5025551234"
 * "+1 502-555-1234".toPhoneDigits() == "5025551234"
 * "1 502 555 1234".toPhoneDigits()  == "5025551234"
 * ```
 *
 * This is what makes invitation matching work: a number typed at sign-up and the same number
 * pulled from the device's contacts must compare equal.
 */
fun String.toPhoneDigits(): String {
    val digits = digitsOnly
    return if (digits.length == PHONE_NUMBER_LENGTH + 1 && digits.startsWith("1")) {
        digits.substring(1)
    } else {
        digits
    }
}

/** True when this string normalizes to a complete [PHONE_NUMBER_LENGTH]-digit phone number. */
val String.isValidPhoneNumber: Boolean
    get() = toPhoneDigits().length == PHONE_NUMBER_LENGTH

/**
 * This string formatted for display as `(502) 555-1234`.
 *
 * Formats progressively so it can drive a text field as the person types, and **never emits a
 * trailing separator** — `"502"` formats to `"502"`, not `"(502) "`. That matters: a format that
 * appended the separator would round-trip to itself when the person pressed backspace, and the
 * field would appear frozen.
 *
 * Input beyond [PHONE_NUMBER_LENGTH] digits is dropped, which is what stops the field accepting
 * an eleventh digit.
 */
val String.formattedPhoneNumber: String
    get() {
        val digits = toPhoneDigits().take(PHONE_NUMBER_LENGTH)
        return when {
            digits.length < 4 -> digits
            digits.length < 7 -> "(${digits.substring(0, 3)}) ${digits.substring(3)}"
            else -> "(${digits.substring(0, 3)}) ${digits.substring(3, 6)}-${digits.substring(6)}"
        }
    }
