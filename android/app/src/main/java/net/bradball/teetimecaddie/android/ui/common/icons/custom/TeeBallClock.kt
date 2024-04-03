package net.bradball.teetimecaddie.android.ui.common.icons.custom

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val CustomIcons.TeeBallClock: ImageVector
    get() {
        if (_teeBallClock != null) {
            return _teeBallClock!!
        }
        _teeBallClock = Builder(name = "CustomIcons.TeeBallClock", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
            viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFF1A1919)), stroke = SolidColor(Color(0x00000000)),
                strokeLineWidth = 1.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                strokeLineMiter = 4.0f, pathFillType = EvenOdd) {
                moveTo(14.7583f, 17.3274f)
                lineTo(9.2413f, 17.3274f)
                lineTo(9.2413f, 18.1361f)
                curveTo(9.2414f, 18.1361f, 9.6427f, 18.3117f, 10.0909f, 18.7392f)
                curveTo(10.2208f, 18.863f, 10.3545f, 19.008f, 10.4836f, 19.176f)
                curveTo(10.9185f, 19.7418f, 11.3005f, 20.5686f, 11.3005f, 21.7273f)
                curveTo(11.3005f, 21.9099f, 11.3005f, 22.1205f, 11.3005f, 22.3533f)
                lineTo(12.6781f, 22.3533f)
                curveTo(12.6781f, 22.1205f, 12.6781f, 21.9099f, 12.6781f, 21.7273f)
                curveTo(12.6781f, 20.5686f, 13.0578f, 19.6314f, 13.4927f, 19.0656f)
                curveTo(13.6218f, 18.8976f, 13.7556f, 18.7527f, 13.8855f, 18.6288f)
                curveTo(14.3336f, 18.2014f, 14.7371f, 18.1361f, 14.7373f, 18.1361f)
                lineTo(14.7583f, 18.1361f)
                lineTo(14.7583f, 17.3274f)
            }
            path(fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFF1A1919)),
                strokeLineWidth = 1.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                strokeLineMiter = 4.0f, pathFillType = EvenOdd) {
                moveTo(18.5277f, 8.8993f)
                curveTo(18.5277f, 12.5045f, 15.605f, 15.4272f, 11.9998f, 15.4272f)
                curveTo(8.3945f, 15.4272f, 5.4718f, 12.5045f, 5.4718f, 8.8993f)
                curveTo(5.4718f, 5.2939f, 8.3945f, 2.3713f, 11.9998f, 2.3713f)
                curveTo(15.605f, 2.3713f, 18.5277f, 5.2939f, 18.5277f, 8.8993f)
                close()
            }
            path(fill = SolidColor(Color(0xFF1A1919)), stroke = SolidColor(Color(0x00000000)),
                strokeLineWidth = 1.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                strokeLineMiter = 4.0f, pathFillType = EvenOdd) {
                moveTo(11.981f, 9.3536f)
                lineTo(11.8802f, 9.3536f)
                curveTo(11.6396f, 9.3536f, 11.4429f, 9.1568f, 11.4429f, 8.9163f)
                lineTo(11.4429f, 4.5753f)
                curveTo(11.4429f, 4.3347f, 11.6396f, 4.1379f, 11.8802f, 4.1379f)
                lineTo(11.981f, 4.1379f)
                curveTo(12.2215f, 4.1379f, 12.4183f, 4.3347f, 12.4183f, 4.5753f)
                lineTo(12.4183f, 8.9163f)
                curveTo(12.4183f, 9.1568f, 12.2215f, 9.3536f, 11.981f, 9.3536f)
            }
            path(fill = SolidColor(Color(0xFF1A1919)), stroke = SolidColor(Color(0x00000000)),
                strokeLineWidth = 1.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                strokeLineMiter = 4.0f, pathFillType = EvenOdd) {
                moveTo(9.3148f, 11.6031f)
                lineTo(9.2436f, 11.5318f)
                curveTo(9.0735f, 11.3618f, 9.0735f, 11.0834f, 9.2436f, 10.9134f)
                lineTo(11.5862f, 8.5706f)
                curveTo(11.7564f, 8.4006f, 12.0347f, 8.4006f, 12.2048f, 8.5706f)
                lineTo(12.276f, 8.6419f)
                curveTo(12.4461f, 8.812f, 12.4461f, 9.0903f, 12.276f, 9.2604f)
                lineTo(9.9333f, 11.6031f)
                curveTo(9.7632f, 11.7732f, 9.4849f, 11.7732f, 9.3148f, 11.6031f)
            }
        }
            .build()
        return _teeBallClock!!
    }

private var _teeBallClock: ImageVector? = null