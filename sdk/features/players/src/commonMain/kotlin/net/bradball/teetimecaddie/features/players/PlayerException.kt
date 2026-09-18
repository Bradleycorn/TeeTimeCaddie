package net.bradball.teetimecaddie.features.players

import dev.icerock.moko.resources.StringResource
import net.bradball.teetimecaddie.core.analytics.LoggableExceptionTypes
import net.bradball.teetimecaddie.core.models.exceptions.TeeTimeCaddieException

/**
 * A player-profile failure, carrying the [PlayerErrors] case the UI renders.
 *
 * @property inlineMessage Non-null when this error should also appear on the offending field.
 * @property messageArgs Values for [displayMessage]'s `%s` placeholders. See `AuthException` for
 *   why the SDK hands these over unformatted rather than resolving them itself.
 */
class PlayerException(
    val error: PlayerErrors,
    val messageArgs: List<String> = emptyList(),
    cause: Exception? = null
): Exception(cause), TeeTimeCaddieException {
    override val title: StringResource = error.title
    override val displayMessage: StringResource = error.message
    override val recoverySuggestion: StringResource? = error.recovery
    val inlineMessage: StringResource? = error.inlineMessage
    override val loggableType: LoggableExceptionTypes = LoggableExceptionTypes.PLAYERS
    override val logInfo: HashMap<String, Any?> = hashMapOf("error" to error.name)
}
