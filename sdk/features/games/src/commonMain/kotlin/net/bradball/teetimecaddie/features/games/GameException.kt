package net.bradball.teetimecaddie.features.games

import dev.icerock.moko.resources.StringResource
import net.bradball.teetimecaddie.core.analytics.LoggableExceptionTypes
import net.bradball.teetimecaddie.core.models.exceptions.TeeTimeCaddieException

/**
 * A game failure, carrying the [GameErrors] case the UI renders.
 *
 * @property messageArgs Values for [displayMessage]'s `%s` placeholders. See `AuthException` for
 *   why the SDK hands these over unformatted rather than resolving them itself.
 */
class GameException(
    val error: GameErrors,
    override val messageArgs: List<String> = emptyList(),
    cause: Exception? = null
): Exception(cause), TeeTimeCaddieException {
    override val title: StringResource = error.title
    override val displayMessage: StringResource = error.message
    override val recoverySuggestion: StringResource? = error.recovery

    override val loggableType: LoggableExceptionTypes = LoggableExceptionTypes.GAMES
    override val logInfo: HashMap<String, Any?> = hashMapOf("error" to error.name)
}
