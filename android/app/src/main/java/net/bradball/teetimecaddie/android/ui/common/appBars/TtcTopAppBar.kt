package net.bradball.teetimecaddie.android.ui.common.appBars

import android.content.res.Configuration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.core.models.GR

/**
 * A start-aligned app bar, in the two configurations the auth flow needs.
 *
 * This is a stock Material 3 [TopAppBar] — no re-skin, no colour overrides. It exists only so the
 * two shapes the design calls for are named in one place:
 *
 * - **No arguments** renders an empty strip. The credentials screen has no title, but Android
 *   still leaves the app-bar space (iOS has no header there at all — a deliberate divergence).
 * - **With [title] and [onBack]** renders a back arrow with the title beside it, both start
 *   aligned. iOS centres its title instead; also deliberate.
 *
 * For a centred title, use [TtcCenteredTopAppBar].
 *
 * @param title The title, or null for the empty strip.
 * @param onBack Invoked by the navigation icon. When null, no back arrow is shown.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TtcTopAppBar(
    title: String? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            if (title != null) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
            }
        },
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
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TtcTopAppBarPreview() {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            TtcTopAppBar(title = "Create account", onBack = {})
        }
    }
}

@Preview(name = "Empty strip - Light", showBackground = true)
@Preview(name = "Empty strip - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TtcTopAppBarEmptyPreview() {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            TtcTopAppBar()
        }
    }
}
