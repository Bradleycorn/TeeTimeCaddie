package net.bradball.teetimecaddie.android.feature.auth.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.features.auth.AR

/**
 * The "By continuing you agree to…" line at the foot of the credentials screen.
 *
 * Terms and Privacy are accented but **inert** — not links, not clickable. That is deliberate and
 * not an omission: neither page exists yet, and a link that goes nowhere is worse than text that
 * never promised to. When those destinations land, this becomes the one place to add them.
 *
 * Built with [buildAnnotatedString] off the format string rather than three concatenated [Text]s,
 * so the sentence wraps as a sentence and stays translatable as one unit.
 */
@Composable
fun AuthLegalFooter(modifier: Modifier = Modifier) {
    val terms = stringResource(AR.strings.auth_legal_terms.resourceId)
    val privacy = stringResource(AR.strings.auth_legal_privacy.resourceId)
    val template = stringResource(AR.strings.auth_legal_footer.resourceId, terms, privacy)
    val accent = SpanStyle(color = MaterialTheme.colorScheme.tertiary)

    val text = buildAnnotatedString {
        append(template)
        // Find the substituted words in the formatted result rather than rebuilding the sentence
        // from pieces, so a translation is free to reorder %1$s and %2$s.
        listOf(terms, privacy).forEach { word ->
            val start = template.indexOf(word)
            if (start >= 0) addStyle(accent, start, start + word.length)
        }
    }

    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AuthLegalFooterPreview() {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            AuthLegalFooter(modifier = Modifier.padding(16.dp))
        }
    }
}
