package net.bradball.teetimecaddie.features.games

import dev.icerock.moko.resources.StringResource
import net.bradball.teetimecaddie.core.models.GR

/**
 * The ways publishing, reading or answering a game, or reading contacts to invite, can fail.
 */
enum class GameErrors(
    val title: StringResource,
    val message: StringResource,
    val recovery: StringResource? = null
) {
    /** The course name was empty or only spaces. */
    NO_COURSE(
        GMR.strings.game_error_title,
        GMR.strings.game_error_no_course_message
    ),

    /** The slot count was outside 1–4. */
    INVALID_SLOTS(
        GMR.strings.game_error_title,
        GMR.strings.game_error_invalid_slots_message
    ),

    /** An invitee had no name, or a number too short or long to be a phone number. */
    INVALID_INVITEE(
        GMR.strings.game_error_title,
        GMR.strings.game_error_invalid_invitee_message
    ),

    /** The write failed. Nothing was saved — the transaction is all or nothing — so a retry is clean. */
    PUBLISH_FAILED(
        GMR.strings.game_error_title,
        GMR.strings.game_error_publish_failed_message,
        GMR.strings.game_error_publish_failed_recovery
    ),

    /** No game has the requested id. */
    NOT_FOUND(
        GMR.strings.game_error_not_found_title,
        GMR.strings.game_error_not_found_message
    ),

    /** Reading a game failed. */
    LOAD_FAILED(
        GMR.strings.game_error_load_failed_title,
        GMR.strings.game_error_load_failed_message,
        GMR.strings.game_error_load_failed_recovery
    ),

    /**
     * A yes arrived after every slot in the tee time was taken. The invitation is still waiting
     * for an answer, so the invitee can still say no.
     */
    GAME_FULL(
        GMR.strings.game_error_game_full_title,
        GMR.strings.game_error_game_full_message
    ),

    /** Saving an answer failed. Nothing was saved, so a retry is clean. */
    RESPOND_FAILED(
        GMR.strings.game_error_respond_failed_title,
        GMR.strings.game_error_respond_failed_message,
        GMR.strings.game_error_respond_failed_recovery
    ),

    /**
     * The device's contacts couldn't be read. Shown with the general error copy; the story
     * proposes none of its own.
     */
    CONTACTS_LOAD_FAILED(
        GR.strings.error_default_title,
        GR.strings.error_default_message,
        GR.strings.error_default_recovery
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
