package net.bradball.teetimecaddie.android.ui.common.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import net.bradball.teetimecaddie.android.ui.common.LoadingDots
import net.bradball.teetimecaddie.android.ui.common.LoadingIndicatorTypes


@Composable
fun LoadingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {

    val onButtonClicked = {
        if (!isLoading) {
            onClick()
        }
    }

    Button(onClick = onButtonClicked, modifier = modifier, enabled = enabled) {
        LoadingIndicator(isLoading = isLoading) {
            Text(text)
        }
    }
}

@Composable
fun LoadingIndicator(
    isLoading: Boolean,
    loadingIndicatorType: LoadingIndicatorTypes = LoadingIndicatorTypes.Flashing,
    content: @Composable RowScope.()->Unit
) {
    // Use a Custom Layout so that we can measure the width of both the
    // button text and the indicator and make sure that the resulting
    // layout is sized to fit either/both.
    // Then we can place either the text or the indicator based on the loading state.
    // This helps ensure that the button does not change size when switching the loading state.
    Layout(
        content = {
            // Content is the Text and the LoadingIndicator
            Row(modifier = Modifier.layoutId("buttonContent"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,) { content() }
            LoadingDots(type = loadingIndicatorType, modifier = Modifier.layoutId("loadingIndicator"))
        }) { measureables, constraints ->

        // First, measure both the text and the indicator, with no additional contraints on their size.
        val textPlaceable = measureables.first { it.layoutId == "buttonContent"}.measure(constraints)
        val indicatorPlaceable = measureables.first { it.layoutId == "loadingIndicator"}.measure(constraints)

        // Now calculate the layout width,
        // making sure it's big enough to fit the larger of the 2 placeables.
        val layoutWidth = textPlaceable.width.coerceAtLeast(indicatorPlaceable.width)
        val layoutHeight = textPlaceable.height.coerceAtLeast(indicatorPlaceable.height)

        // Now, create the layout at the appropriate size
        layout(layoutWidth,layoutHeight) {
            // Place EITHER the indicator or the text (but not both), based on the loading state
            if (isLoading) {
                // Calculate the X and Y position of the indicator so that it's centered in the layout.
                val indicatorX = (layoutWidth - indicatorPlaceable.width) / 2
                val indicatorY = (layoutHeight - indicatorPlaceable.height) / 2
                // Place the indicator
                indicatorPlaceable.placeRelative(x = indicatorX, y = indicatorY)
            } else {
                // Calculate the X and Y position of the text so that it's centered in the layout.
                val textX = (layoutWidth - textPlaceable.width) / 2
                val textY = (layoutHeight - textPlaceable.height) / 2
                //Place the text
                textPlaceable.placeRelative(x = textX, y = textY)
            }
        }
    }
}