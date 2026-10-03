package net.bradball.teetimecaddie.features.players

import dev.icerock.moko.resources.StringResource
import net.bradball.teetimecaddie.core.models.GR

/**
 * The ways saving or reading a player profile can fail.
 *
 * Separate from `AuthErrors` on purpose: a phone number that is already registered is a profile
 * constraint, not an authentication failure, and the screens render the two differently.
 *
 * @property inlineMessage Set only for errors the design shows *on a field* as well as in a
 *   message block. [PHONE_IN_USE] is the one case that appears in both places at once, so putting
 *   it here stops each platform re-deriving that exception from the error name.
 */
enum class PlayerErrors(
    val title: StringResource,
    val message: StringResource,
    val recovery: StringResource? = null,
    val inlineMessage: StringResource? = null
) {
    PHONE_IN_USE(
        PR.strings.player_error_phone_in_use_title,
        PR.strings.player_error_phone_in_use_message,
        PR.strings.player_error_phone_in_use_recovery,
        PR.strings.player_error_phone_in_use_inline
    ),

    INVALID_PHONE(
        PR.strings.player_error_title,
        PR.strings.player_error_invalid_phone_message,
        inlineMessage = PR.strings.player_error_invalid_phone_inline
    ),

    NO_NAME(
        PR.strings.player_error_title,
        PR.strings.player_error_no_name_message
    ),

    /** The document write failed. Retryable — the auth account survives. */
    SAVE_FAILED(
        PR.strings.player_error_save_failed_title,
        PR.strings.player_error_save_failed_message,
        PR.strings.player_error_save_failed_recovery
    ),

    /** The avatar upload failed. Side-effect free: nothing was written, so a retry is clean. */
    PHOTO_UPLOAD_FAILED(
        PR.strings.player_error_photo_upload_failed_title,
        PR.strings.player_error_photo_upload_failed_message,
        PR.strings.player_error_photo_upload_failed_recovery
    ),

    UNKNOWN(
        GR.strings.error_default_title,
        GR.strings.error_default_message,
        GR.strings.error_default_recovery
    );

    companion object {
        val default = UNKNOWN
    }
}
