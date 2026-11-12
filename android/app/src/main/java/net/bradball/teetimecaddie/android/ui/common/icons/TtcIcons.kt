package net.bradball.teetimecaddie.android.ui.common.icons

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import net.bradball.teetimecaddie.android.R

enum class TtcIcons(val resource: Int) {
    ADD(R.drawable.icon_add),
    ADD_A_PHOTO(R.drawable.icon_add_a_photo),
    ARROW_BACK(R.drawable.icon_arrow_back),
    ARROW_DROP_DOWN(R.drawable.icon_arrow_drop_down),
    ARROW_FORWARD(R.drawable.icon_arrow_forward),
    CALENDAR(R.drawable.icon_calendar),
    CALENDAR_CLOCK(R.drawable.icon_calendar_clock),
    CANCEL(R.drawable.icon_cancel),
    CHECK_CIRCLE(R.drawable.icon_check_circle),
    CLOSE(R.drawable.icon_close),
    EDIT(R.drawable.icon_edit),
    EMAIL(R.drawable.icon_email),
    EMAIL_ERROR(R.drawable.icon_email_error),
    ERROR(R.drawable.icon_error),
    GOLF_COURSE(R.drawable.icon_golf_course),
    GROUP(R.drawable.icon_group),
    HOURGLASS_EMPTY(R.drawable.icon_hourglass_empty),
    LOCK(R.drawable.icon_lock),
    LOCK_PERSON(R.drawable.icon_lock_person),
    LOGOUT(R.drawable.icon_logout),
    MARK_EMAIL_UNREAD(R.drawable.icon_mark_email_unread),
    MORE_VERT(R.drawable.icon_more_vert),
    PERSON_ADD(R.drawable.icon_person_add),
    PERSON_FILL(R.drawable.icon_person_fill),
    PERSON_OFF(R.drawable.icon_person_off),
    PERSON_OUTLINE(R.drawable.icon_person_outline),
    PHONE(R.drawable.icon_phone),
    PHONE_TALK(R.drawable.icon_phone_in_talk),
    SCHEDULE(R.drawable.icon_schedule),
    STAR_FILLED(R.drawable.icon_star_filled),
    TEE(R.drawable.icon_tee),
    TEE_CLOCK(R.drawable.icon_tee_clock),
    TEE_STRIKE(R.drawable.icon_strikethrough),
    TRASH(R.drawable.icon_trash),
    VISIBILITY(R.drawable.icon_visibility),
    VISIBILITY_OFF(R.drawable.icon_visibility_off);

    val painter: Painter
        @Composable get() = painterResource(this.resource)
}