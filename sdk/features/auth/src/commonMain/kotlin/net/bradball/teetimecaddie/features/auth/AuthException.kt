package net.bradball.teetimecaddie.features.auth

import dev.icerock.moko.resources.StringResource
import net.bradball.teetimecaddie.core.analytics.LoggableExceptionTypes
import net.bradball.teetimecaddie.core.models.exceptions.TeeTimeCaddieException

/**
 * An authentication failure, carrying the [AuthErrors] case the UI renders.
 *
 * @property messageArgs Values to substitute into [displayMessage]'s `%s` placeholders — currently
 *   only the email address for [AuthErrors.EMAIL_IN_USE]. The SDK deliberately does not format the
 *   string itself: moko's `StringResource` takes no arguments, and reaching for `StringDesc` would
 *   put a moko-desc type in the SDK's public API and an unverified assumption into the Swift
 *   interop. Each app formats natively instead — `stringResource(id, *args)` on Android,
 *   `String(format:)` on iOS.
 */
class AuthException(
    val error: AuthErrors,
    val messageArgs: List<String> = emptyList(),
    cause: Exception? = null
): Exception(cause), TeeTimeCaddieException {
    override val title: StringResource = error.title
    override val displayMessage: StringResource = error.message
    override val recoverySuggestion: StringResource? = error.recovery
    override val loggableType: LoggableExceptionTypes = LoggableExceptionTypes.AUTHENTICATION
    override val logInfo: HashMap<String, Any?> = hashMapOf("error" to error.name)
}
