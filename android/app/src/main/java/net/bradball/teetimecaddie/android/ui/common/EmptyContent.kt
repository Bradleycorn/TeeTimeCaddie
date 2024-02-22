package net.bradball.teetimecaddie.android.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.R
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme



@Composable
fun EmptyContent(title: String, message: String, iconVector: ImageVector, modifier: Modifier = Modifier) {
    val painter = rememberVectorPainter(image = iconVector)
    EmptyContent(title, message, painter, modifier)
}

@Composable
fun EmptyContent(title: String = "Fore!", message: String, icon: Painter? = null, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 24.dp)) {
        icon?.let { painter ->
            Icon(
                painter = painter,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
        }
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EmptyContentPreview() {
    MyApplicationTheme {
        Surface {
            EmptyContent(
                title = "No Content",
                message = "There is no content. Add some content and it will show here.",
                icon = painterResource(id = R.drawable.app_logo),
                modifier = Modifier.padding(top = 64.dp)
            )
        }
    }
}
