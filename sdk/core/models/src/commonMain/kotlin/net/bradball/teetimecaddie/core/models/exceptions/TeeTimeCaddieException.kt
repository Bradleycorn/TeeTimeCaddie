package net.bradball.teetimecaddie.core.models.exceptions

import dev.icerock.moko.resources.StringResource
import net.bradball.teetimecaddie.core.analytics.LoggableException

interface TeeTimeCaddieException: LoggableException {
    val title: StringResource
    val displayMessage: StringResource
    val recoverySuggestion: StringResource?

    /**
     * Values for [displayMessage]'s `%s` placeholders, in order.
     *
     * Declared here, with an empty default, so a caller can render *any* failure without knowing
     * which concrete exception it holds — the apps show unhandled failures generically, and
     * dropping arguments there would print a raw `%s` at someone.
     */
    val messageArgs: List<String> get() = emptyList()
}
