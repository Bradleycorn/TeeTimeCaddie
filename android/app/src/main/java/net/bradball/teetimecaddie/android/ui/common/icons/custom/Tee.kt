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

val CustomIcons.Tee: ImageVector
    get() {
        if (_tee != null) {
            return _tee!!
        }
        _tee = Builder(name = "CustomIcons.Tee", defaultWidth = 24.0.dp, defaultHeight
        = 24.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFF1A1919)), stroke = SolidColor(Color(0x00000000)),
                strokeLineWidth = 1.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                strokeLineMiter = 4.0f, pathFillType = EvenOdd) {
                moveTo(23.0f, 1.0f)
                lineTo(1.0f, 1.0f)
                lineTo(1.0f, 4.5401f)
                curveTo(1.0004f, 4.5401f, 2.6007f, 5.3085f, 4.3882f, 7.1801f)
                curveTo(4.9059f, 7.7221f, 5.4394f, 8.3568f, 5.9542f, 9.092f)
                curveTo(7.6885f, 11.5689f, 9.2117f, 15.1878f, 9.2117f, 20.2599f)
                curveTo(9.2117f, 21.059f, 9.2117f, 21.9808f, 9.2117f, 23.0f)
                lineTo(14.705f, 23.0f)
                curveTo(14.705f, 21.9808f, 14.705f, 21.059f, 14.705f, 20.2599f)
                curveTo(14.705f, 15.1878f, 16.2191f, 11.0855f, 17.9533f, 8.6089f)
                curveTo(18.4683f, 7.8735f, 19.0016f, 7.2389f, 19.5195f, 6.6969f)
                curveTo(21.3067f, 4.8258f, 22.9155f, 4.5401f, 22.9165f, 4.5401f)
                lineTo(23.0f, 4.5401f)
                lineTo(23.0f, 1.0f)
            }
        }
            .build()
        return _tee!!
    }

private var _tee: ImageVector? = null