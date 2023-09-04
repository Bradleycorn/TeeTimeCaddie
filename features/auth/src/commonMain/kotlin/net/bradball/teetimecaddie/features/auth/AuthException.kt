package net.bradball.teetimecaddie.features.auth

import dev.icerock.moko.resources.StringResource
import net.bradball.teetimecaddie.core.analytics.LoggableExceptionTypes
import net.bradball.teetimecaddie.core.models.exceptions.TeeTimeCaddieException

class AuthException(
    val error: AuthErrors,
    cause: Exception? = null
): Exception(cause), TeeTimeCaddieException {
    override val title: StringResource = error.title
    override val displayMessage: StringResource = error.message
    override val recoverySuggestion: StringResource? = error.recovery
    override val loggableType: LoggableExceptionTypes = LoggableExceptionTypes.AUTHENTICATION
    override val logInfo: HashMap<String, Any?> = hashMapOf()
}
