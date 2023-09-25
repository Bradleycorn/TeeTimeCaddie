package net.bradball.teetimecaddie.android.feature.auth.common

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.features.auth.AR

@Composable
fun AuthFooter(text: String, actionText: String? = null, onActionClick: ()->Unit = {}) {
    Row {
        Text(
            text,
            modifier = Modifier.alignByBaseline()
        )

        actionText?.let { buttonText ->
            TextButton(onClick = { onActionClick() }, modifier = Modifier.alignByBaseline()) {
                Text(buttonText)
            }
        }
    }
}


@Preview
@Composable
fun PreviewAuthFooter() {
    MyApplicationTheme {
        Surface {
            AuthFooter("Do Something else?", "Click Here")
        }
    }
}