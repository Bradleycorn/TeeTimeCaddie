package net.bradball.teetimecaddie.android.feature.auth.common

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.features.auth.AR

/**
 * The brand mark, name and tagline that head the credentials screen.
 *
 * The circle is `primaryContainer` rather than solid `primary`: it is an identity mark, not the
 * screen's call to action, and a solid green disc would out-shout the Sign in button directly
 * below it.
 */
@Composable
fun AuthBrandLockup(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AuthBrandLockupDefaults.Spacing),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(AuthBrandLockupDefaults.MarkSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
        ) {
            Icon(
                painter = TtcIcons.TEE.painter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(AuthBrandLockupDefaults.GlyphSize),
            )
        }

        Text(
            text = stringResource(AR.strings.auth_brand_name.resourceId),
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = stringResource(AR.strings.auth_tagline.resourceId),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private object AuthBrandLockupDefaults {
    /** Matches the iOS lockup's 64pt mark, so the two platforms read at the same weight. */
    val MarkSize = 64.dp
    val GlyphSize = 32.dp
    val Spacing = 8.dp
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AuthBrandLockupPreview() {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            AuthBrandLockup(modifier = Modifier.size(width = 320.dp, height = 200.dp))
        }
    }
}
