package net.bradball.teetimecaddie.android.ui.common.modifiers

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme

/**
 * Conditionally applies a blur effect to the composable it's attached to.
 *
 * This modifier allows for conditional application of the built-in Compose blur modifier.
 * When enabled, it applies a blur effect using the specified [radius] and [edgeTreatment].
 * When disabled, it returns the original Modifier without any changes.
 *
 * @param enabled Boolean flag to determine whether the blur effect should be applied.
 *                If true, the blur is applied; if false, the original Modifier is returned unchanged.
 * @param radius The radius of the blur effect, specified as a [Dp] value.
 *               This determines the extent of the blur. Larger values create a more pronounced blur effect.
 *               Defaults to 5.dp.
 * @param edgeTreatment Specifies how the edges of the blurred area should be treated.
 *                      This is of type [BlurredEdgeTreatment] which typically includes options like
 *                      Rectangle (sharp edges) or Circular (rounded edges).
 *                      Defaults to [BlurredEdgeTreatment.Rectangle].
 *
 * @return A [Modifier] with the blur effect applied if [enabled] is true,
 *         otherwise returns the original Modifier unchanged.
 */
fun Modifier.blur(enabled: Boolean, radius: Dp = 5.dp, edgeTreatment: BlurredEdgeTreatment = BlurredEdgeTreatment.Rectangle): Modifier {
    return if (enabled) this.blur(radius, edgeTreatment) else this
}

@Preview(showBackground = true)
@Composable
fun PreviewBlurModifier() {
    MyApplicationTheme {
        Button({}, modifier = Modifier.blur(true)) {
            Text("Click Me")
        }
    }
}