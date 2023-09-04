package net.bradball.teetimecaddie.features.auth

import dev.icerock.moko.resources.StringResource
import net.bradball.teetimecaddie.core.models.MR

enum class AuthErrors(val title: StringResource, val message: StringResource, val recovery: StringResource?) {
    USER_EXISTS(AR.strings.reg_error_default_title, AR.strings.reg_error_default_message, AR.strings.reg_error_default_recovery),
    WEAK_PASSWORD(AR.strings.reg_error_default_title, AR.strings.reg_error_weak_password_message, AR.strings.reg_error_weak_password_recovery),
    INVALID_EMAIL(AR.strings.reg_error_default_title, AR.strings.reg_error_invalid_email_message, AR.strings.reg_error_invalid_email_recovery),
    NO_NAME(AR.strings.reg_error_default_title, AR.strings.reg_error_no_name_message, AR.strings.reg_error_no_name_recovery),
    REG_DEFAULT(AR.strings.reg_error_default_title, AR.strings.reg_error_default_message, AR.strings.reg_error_default_recovery),
    UNKNOWN(MR.strings.error_default_title, MR.strings.error_default_message, MR.strings.error_default_recovery);

    companion object {
        val default = UNKNOWN
    }
}