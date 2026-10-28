package net.bradball.teetimecaddie.android.ui.common.sheets

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Shared color, shape and spacing values for [TtcBottomSheet], derived from the bottom sheets in
 * the Tee Time Caddie design (the contacts explanation sheet and picker). Colors always resolve to
 * semantic Material 3 roles.
 *
 * Mirrors the structure of `TtcCardDefaults`.
 */
internal object TtcBottomSheetDefaults {

    /**
     * Sheet shape — Fairway Morning `radius-xl`, the theme's [Shapes.extraLarge] (28dp), on the top
     * corners only. The sheet sits on the screen's bottom edge, so its bottom corners stay square.
     */
    val Shape: Shape
        @Composable get() = MaterialTheme.shapes.extraLarge.copy(
            bottomStart = ZeroCornerSize,
            bottomEnd = ZeroCornerSize,
        )

    /** Sheet container — `surfaceContainerLow`, one step above the screen it covers. */
    val ContainerColor: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainerLow

    /** The drag handle's color — `outlineVariant`, the design's quiet 32×4 grip. */
    val DragHandleColor: Color
        @Composable get() = MaterialTheme.colorScheme.outlineVariant

    /**
     * Padding around the header row: the design's `4/8/8/20`. The title starts 20dp in, and the
     * trailing action or ✕ sits 8dp from the edge, where its own touch target supplies the rest.
     */
    val HeaderPadding = PaddingValues(start = 20.dp, top = 4.dp, end = 8.dp, bottom = 8.dp)

    /**
     * Padding around the sheet's content: the design's `4/8/24`. Content that runs edge to edge,
     * such as a list whose rows pad themselves, passes its own value.
     */
    val ContentPadding = PaddingValues(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 24.dp)
}
