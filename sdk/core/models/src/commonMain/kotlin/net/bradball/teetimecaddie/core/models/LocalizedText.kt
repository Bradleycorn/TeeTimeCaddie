package net.bradball.teetimecaddie.core.models

import dev.icerock.moko.resources.StringResource
import net.bradball.teetimecaddie.core.models.exceptions.TeeTimeCaddieException

/**
 * Text for an app to show: a string resource, and the values for its `%s` placeholders.
 *
 * The SDK hands text over unformatted, as [TeeTimeCaddieException.messageArgs] does, rather than as
 * moko's `StringDesc`, so each app resolves it natively in the viewer's locale. Both apps call it
 * `localized()`: a composable on Android, a method on iOS.
 *
 * @property resource The string, with `%s` placeholders where [args] go.
 * @property args Values for [resource]'s placeholders, in order.
 */
data class LocalizedText(
    val resource: StringResource,
    val args: List<String> = emptyList()
)
