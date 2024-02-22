package net.bradball.teetimecaddie.android.ui.common.icons.material


import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val Icons.Outlined.CalendarClock: ImageVector
    get() {
        if (_calendarClock != null) {
            return _calendarClock!!
        }
        _calendarClock = Builder(name = "Icons.Outlined.CalendarClock", defaultWidth = 24.0.dp, defaultHeight =
        24.0.dp, viewportWidth = 960.0f, viewportHeight = 960.0f).apply {
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero) {
                moveTo(200.0f, 320.0f)
                horizontalLineToRelative(560.0f)
                verticalLineToRelative(-80.0f)
                lineTo(200.0f, 240.0f)
                verticalLineToRelative(80.0f)
                close()
                moveTo(200.0f, 320.0f)
                verticalLineToRelative(-80.0f)
                verticalLineToRelative(80.0f)
                close()
                moveTo(200.0f, 880.0f)
                quadToRelative(-33.0f, 0.0f, -56.5f, -23.5f)
                reflectiveQuadTo(120.0f, 800.0f)
                verticalLineToRelative(-560.0f)
                quadToRelative(0.0f, -33.0f, 23.5f, -56.5f)
                reflectiveQuadTo(200.0f, 160.0f)
                horizontalLineToRelative(40.0f)
                verticalLineToRelative(-80.0f)
                horizontalLineToRelative(80.0f)
                verticalLineToRelative(80.0f)
                horizontalLineToRelative(320.0f)
                verticalLineToRelative(-80.0f)
                horizontalLineToRelative(80.0f)
                verticalLineToRelative(80.0f)
                horizontalLineToRelative(40.0f)
                quadToRelative(33.0f, 0.0f, 56.5f, 23.5f)
                reflectiveQuadTo(840.0f, 240.0f)
                verticalLineToRelative(227.0f)
                quadToRelative(-19.0f, -9.0f, -39.0f, -15.0f)
                reflectiveQuadToRelative(-41.0f, -9.0f)
                verticalLineToRelative(-43.0f)
                lineTo(200.0f, 400.0f)
                verticalLineToRelative(400.0f)
                horizontalLineToRelative(252.0f)
                quadToRelative(7.0f, 22.0f, 16.5f, 42.0f)
                reflectiveQuadTo(491.0f, 880.0f)
                lineTo(200.0f, 880.0f)
                close()
                moveTo(720.0f, 920.0f)
                quadToRelative(-83.0f, 0.0f, -141.5f, -58.5f)
                reflectiveQuadTo(520.0f, 720.0f)
                quadToRelative(0.0f, -83.0f, 58.5f, -141.5f)
                reflectiveQuadTo(720.0f, 520.0f)
                quadToRelative(83.0f, 0.0f, 141.5f, 58.5f)
                reflectiveQuadTo(920.0f, 720.0f)
                quadToRelative(0.0f, 83.0f, -58.5f, 141.5f)
                reflectiveQuadTo(720.0f, 920.0f)
                close()
                moveTo(787.0f, 815.0f)
                lineTo(815.0f, 787.0f)
                lineTo(740.0f, 712.0f)
                verticalLineToRelative(-112.0f)
                horizontalLineToRelative(-40.0f)
                verticalLineToRelative(128.0f)
                lineToRelative(87.0f, 87.0f)
                close()
            }
        }
            .build()
        return _calendarClock!!
    }

private var _calendarClock: ImageVector? = null
