package net.bradball.teetimecaddie.android.feature.auth.common

import dev.icerock.moko.resources.StringResource
import net.bradball.teetimecaddie.android.theme.TtcColorRole
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.features.auth.AR
import net.bradball.teetimecaddie.features.players.PR

/**
 * A message block shown on an auth screen — the designed alternative to a dialog or a toast for
 * something the person has to read and act on.
 *
 * Deliberately a **closed** set rather than a mapper over every `AuthErrors` / `PlayerErrors` case.
 * These three are the ones the design draws a block for; everything else is a transient failure that
 * belongs in a snackbar, and listing them here would invite a block the design has no treatment for.
 *
 * Each case carries its own presentation, so [AuthMessageBlock] renders any message without
 * knowing which one it has.
 */
sealed interface AuthMessage {

    /** The tone the block is drawn in. */
    val role: TtcColorRole

    /** The glyph shown beside the title. */
    val icon: TtcIcons

    val title: StringResource

    val body: StringResource

    /** Format arguments for [body]. */
    val bodyArgs: List<String> get() = emptyList()

    /**
     * The block's action, or null for a block that is purely informational.
     *
     * Nullable because [SignInFailed] has nothing to offer: there is no recovery beyond correcting
     * what was typed, and a button that re-submits the same credentials would be noise.
     */
    val actionText: StringResource? get() = null

    /**
     * Sign-in was rejected.
     *
     * Says nothing about *which* half was wrong. With Firebase email enumeration protection on,
     * "no such account" and "wrong password" are indistinguishable to us — and disclosing either
     * would be exactly the leak the protection exists to prevent.
     */
    data object SignInFailed : AuthMessage {
        override val role = TtcColorRole.Error
        override val icon = TtcIcons.LOCK_PERSON
        override val title = AR.strings.login_error_invalid_credentials_title
        override val body = AR.strings.login_error_invalid_credentials_message
    }

    /**
     * Creating an account was rejected because the address already has one.
     *
     * Not an error in the person's eyes — they have an account and typed the right address — so it
     * is drawn in [TtcColorRole.Secondary] rather than [TtcColorRole.Error], and its action takes
     * them to signing in.
     */
    data class EmailInUse(val email: String) : AuthMessage {
        override val role = TtcColorRole.Secondary
        override val icon = TtcIcons.EMAIL_ERROR
        override val title = AR.strings.reg_error_email_in_use_title
        override val body = AR.strings.reg_error_email_in_use_message
        override val bodyArgs = listOf(email)
        override val actionText = AR.strings.reg_error_email_in_use_recovery
    }

    /**
     * The mobile number already belongs to another player.
     *
     * Shown **together with** an inline error on the field itself: the block explains and offers a
     * way out, the field marks what to change.
     */
    data object PhoneInUse : AuthMessage {
        override val role = TtcColorRole.Secondary
        override val icon = TtcIcons.PHONE_TALK
        override val title = PR.strings.player_error_phone_in_use_title
        override val body = PR.strings.player_error_phone_in_use_message
        override val actionText = PR.strings.player_error_phone_in_use_recovery
    }
}
