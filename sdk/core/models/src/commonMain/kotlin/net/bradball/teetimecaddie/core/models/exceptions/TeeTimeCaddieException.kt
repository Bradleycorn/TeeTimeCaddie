package net.bradball.teetimecaddie.core.models.exceptions

import dev.icerock.moko.resources.StringResource
import net.bradball.teetimecaddie.core.analytics.LoggableException

interface TeeTimeCaddieException: LoggableException {
    val title: StringResource
    val displayMessage: StringResource
    val recoverySuggestion: StringResource?
}
