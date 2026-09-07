package net.bradball.teetimecaddie.android.ui.common.icons

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import net.bradball.teetimecaddie.android.R

enum class TtcIcons(val resource: Int) {
    ADD(R.drawable.icon_add),
    ADD_A_PHOTO(R.drawable.icon_add_a_photo),
    ARROW_BACK(R.drawable.icon_arrow_back),
    ARROW_FORWARD(R.drawable.icon_arrow_forward),
    CALENDAR(R.drawable.icon_calendar),
    CALENDAR_CLOCK(R.drawable.icon_calendar_clock),
    EDIT(R.drawable.icon_edit),
    PERSON(R.drawable.icon_person),
    TEE(R.drawable.icon_tee),
    TEE_CLOCK(R.drawable.icon_tee_clock),
    TEE_STRIKE(R.drawable.icon_strikethrough),
    TRASH(R.drawable.icon_trash),
    VISIBILITY(R.drawable.icon_visibility),
    VISIBILITY_OFF(R.drawable.icon_visibility_off);

    val painter: Painter
        @Composable get() = painterResource(this.resource)
}