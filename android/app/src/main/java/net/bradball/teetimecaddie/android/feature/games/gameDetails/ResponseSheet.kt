package net.bradball.teetimecaddie.android.feature.games.gameDetails

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.android.ui.common.sheets.TtcBottomSheet
import net.bradball.teetimecaddie.android.ui.common.text.stringResource
import net.bradball.teetimecaddie.core.models.GR
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.ResponseSheetUiState
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.previewFullResponseSheetUiState
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.previewResponseSheetUiState

/**
 * The sheet the organizer records an invitee's Yes or No in.
 *
 * Picking an answer, like dismissing the sheet, slides it away first; [onRecordResponse] or
 * [onDismiss] is called once it has closed, so the caller stops showing it then.
 *
 * @param onRecordResponse Called with the answer picked: true for Yes.
 * @param onDismiss Called when the sheet closes without an answer: dragged down or tapped outside.
 */
@Composable
fun ResponseSheet(
    uiState: ResponseSheetUiState,
    onRecordResponse: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    // The answer picked, held until the sheet has slid away.
    var picked by remember { mutableStateOf<Boolean?>(null) }

    TtcBottomSheet(
        title = stringResource(uiState.title),
        onDismissRequest = { picked?.let(onRecordResponse) ?: onDismiss() },
        // The design's response sheet has no ✕; it closes by dragging down, tapping outside, or
        // picking an answer. The spacer keeps the header the height it has with one.
        action = { Spacer(Modifier.height(ResponseSheetDefaults.HeaderActionHeight)) },
    ) { dismiss ->
        ResponseSheetBody(
            uiState = uiState,
            onPick = { accept ->
                picked = accept
                dismiss()
            },
        )
    }
}

/** The sheet's body under its title: the prompt, then Yes and No. */
@Composable
private fun ColumnScope.ResponseSheetBody(uiState: ResponseSheetUiState, onPick: (Boolean) -> Unit) {
    Text(
        text = stringResource(uiState.prompt),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(ResponseSheetDefaults.PromptPadding),
    )
    ResponseOption(
        icon = TtcIcons.CHECK_CIRCLE,
        iconColor = MaterialTheme.colorScheme.primary,
        label = stringResource(GR.strings.yes.resourceId),
        detail = uiState.yesDetail,
        enabled = uiState.canAccept,
        onClick = { onPick(true) },
    )
    ResponseOption(
        icon = TtcIcons.CANCEL,
        iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        label = stringResource(GR.strings.no.resourceId),
        detail = uiState.noDetail,
        onClick = { onPick(false) },
    )
}

/** One answer in the sheet: its icon, the answer, and a line saying what it means. */
@Composable
private fun ResponseOption(
    icon: TtcIcons,
    iconColor: Color,
    label: String,
    detail: LocalizedText,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ResponseSheetDefaults.OptionMinHeight)
            .clip(MaterialTheme.shapes.medium)
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else ResponseSheetDefaults.DisabledAlpha)
            .padding(ResponseSheetDefaults.OptionPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ResponseSheetDefaults.OptionSpacing),
    ) {
        Icon(
            painter = icon.painter,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(ResponseSheetDefaults.OptionIconSize),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(detail),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private object ResponseSheetDefaults {
    val HeaderActionHeight = 48.dp
    val PromptPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 8.dp)
    val OptionMinHeight = 64.dp
    val OptionPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    val OptionSpacing = 16.dp
    val OptionIconSize = 24.dp
    // The design's dimming for an answer that can't be picked.
    const val DisabledAlpha = 0.45f
}

// `ModalBottomSheet` draws in its own window, which the preview host never rasterizes (see
// `TtcBottomSheet`), so these preview the body on the sheet's own surface color.

@Composable
private fun ResponseSheetPreview(uiState: ResponseSheetUiState) {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(uiState.title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 12.dp, bottom = 8.dp),
                )
                ResponseSheetBody(uiState = uiState, onPick = {})
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ResponseSheetOpenPreview() {
    ResponseSheetPreview(previewResponseSheetUiState)
}

@Preview(name = "Tee time full", showBackground = true)
@Composable
private fun ResponseSheetFullPreview() {
    ResponseSheetPreview(previewFullResponseSheetUiState)
}
