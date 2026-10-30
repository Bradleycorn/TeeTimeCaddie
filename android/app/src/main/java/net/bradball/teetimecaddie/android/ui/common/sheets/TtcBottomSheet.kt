package net.bradball.teetimecaddie.android.ui.common.sheets

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.core.models.GR

/**
 * A modal bottom sheet from the Fairway Morning design system.
 *
 * Wraps Material 3's [ModalBottomSheet] in the design's sheet:
 * - a `surfaceContainerLow` container, rounded on top only
 * - an `outlineVariant` drag handle
 * - a header row with a [title] and, at its end, either the caller's [action] or a ✕ that closes
 *   the sheet
 *
 * The sheet always opens fully expanded and skips the half-open state, as the design does. A short
 * sheet sizes itself to its content; a long one, such as a list, fills the screen and scrolls.
 *
 * The caller owns visibility, as with `TtcDatePickerDialog`:
 * ```kotlin
 * var showSheet by remember { mutableStateOf(false) }
 * if (showSheet) {
 *     TtcBottomSheet(title = "Find your golf buddies", onDismissRequest = { showSheet = false }) { dismiss ->
 *         Text("…")
 *         TtcButton(onClick = dismiss) { Text("Not now") }
 *     }
 * }
 * ```
 *
 * [action] and [content] each receive a `dismiss` function. Call it to close the sheet from a
 * button: it slides the sheet away first and then calls [onDismissRequest], which is how dragging
 * down, tapping the scrim and the ✕ close it too. Calling [onDismissRequest] directly would remove
 * the sheet without its exit animation.
 *
 * @param title The sheet's title.
 * @param onDismissRequest Called once the sheet has closed, by any route. Stop showing it here.
 * @param modifier Modifier for the sheet.
 * @param sheetState The sheet's state. Defaults to one that skips the half-open state.
 * @param contentPadding Padding around [content]. Defaults to the design's sheet body inset.
 * @param action Replaces the ✕ at the end of the header, e.g. the picker's "Done (3)". It is placed
 *   in the header's row, after the title. A sheet with an action has no ✕; it closes by dragging
 *   down, tapping outside, or through [action] itself.
 * @param content The sheet's body, below the header.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TtcBottomSheet(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    contentPadding: PaddingValues = TtcBottomSheetDefaults.ContentPadding,
    action: (@Composable RowScope.(dismiss: () -> Unit) -> Unit)? = null,
    content: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val dismiss: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismissRequest()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = TtcBottomSheetDefaults.Shape,
        containerColor = TtcBottomSheetDefaults.ContainerColor,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TtcBottomSheetDefaults.DragHandleColor) },
    ) {
        TtcBottomSheetBody(
            title = title,
            contentPadding = contentPadding,
            trailingContent = {
                if (action != null) action(dismiss) else CloseButton(onClick = dismiss)
            },
            content = { content(dismiss) },
        )
    }
}

/**
 * Everything inside the sheet below the drag handle: the header row, then the content.
 *
 * @param trailingContent What ends the header row, after the title — already resolved to the
 *   caller's action or the ✕.
 */
@Composable
private fun TtcBottomSheetBody(
    title: String,
    contentPadding: PaddingValues,
    trailingContent: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TtcBottomSheetDefaults.HeaderPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            trailingContent()
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content,
        )
    }
}

@Composable
private fun CloseButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            painter = TtcIcons.CLOSE.painter,
            contentDescription = stringResource(GR.strings.close.resourceId),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────
// `ModalBottomSheet` draws in its own window, which Studio's preview host never rasterizes (the same
// limitation documented in `TtcDatePickerDialog`). These previews draw the real sheet body on a
// surface with the sheet's own shape, color and drag handle. They are not a second code path:
// `TtcBottomSheet` is what ships.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetPreviewScaffold(
    title: String,
    trailingContent: @Composable RowScope.() -> Unit = { CloseButton(onClick = {}) },
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
        Surface(shape = TtcBottomSheetDefaults.Shape, color = TtcBottomSheetDefaults.ContainerColor) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BottomSheetDefaults.DragHandle(color = TtcBottomSheetDefaults.DragHandleColor)
                TtcBottomSheetBody(
                    title = title,
                    contentPadding = TtcBottomSheetDefaults.ContentPadding,
                    trailingContent = trailingContent,
                    content = content,
                )
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TtcBottomSheetPreview() {
    MyApplicationTheme {
        BottomSheetPreviewScaffold(title = "Sheet title") {
            Text(
                text = "Sheet content sits below the header, inset by the design's sheet padding.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }
    }
}

@Preview(name = "With action", showBackground = true)
@Composable
private fun TtcBottomSheetActionPreview() {
    MyApplicationTheme {
        BottomSheetPreviewScaffold(
            title = "Sheet title",
            trailingContent = { TextButton(onClick = {}) { Text("Done (2)", fontWeight = FontWeight.SemiBold) } },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) {
                    Text("Row ${it + 1}", modifier = Modifier.padding(horizontal = 8.dp))
                }
            }
        }
    }
}
