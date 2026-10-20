package net.bradball.teetimecaddie.android.ui.common.appBars

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.core.models.GR

/**
 * A top app bar with a centred title, and an optional back button.
 *
 * The centred twin of [TtcTopAppBar]: pass [onBack] to show the standard back arrow, so every
 * screen that leaves the same way shows the same icon.
 *
 * @param title The centred title.
 * @param onBack When non-null, shows a back arrow that calls this when tapped.
 * @param modifier Modifier for the app bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TtcCenteredTopAppBar(
    title: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = { Text(title) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = TtcIcons.ARROW_BACK.painter,
                        contentDescription = stringResource(GR.strings.back.resourceId),
                    )
                }
            }
        },
    )
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun TtcCenteredTopAppBarPreview() {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            TtcCenteredTopAppBar(title = "Game details", onBack = {})
        }
    }
}

@Preview(name = "No back button", showBackground = true)
@Composable
private fun TtcCenteredTopAppBarTitleOnlyPreview() {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            TtcCenteredTopAppBar(title = "Profile")
        }
    }
}
