package net.bradball.teetimecaddie.android.feature.auth.welcome

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons

@Composable
fun WelcomeRoute(onClose: () -> Unit) {
    BackHandler {
        onClose()
    }

    WelcomeScreen(onClose = onClose)
}

@Composable
fun WelcomeScreen(onClose: ()->Unit = {}) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primary
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.size(128.dp))
            Icon(
                TtcIcons.TeeBallClock,
                contentDescription = "TeeTimeCaddie",
                modifier = Modifier.size(64.dp)
            )
            Text("Welcome to Tee Time Caddie!", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.size(64.dp))
            OutlinedButton(
                onClick = onClose,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = LocalContentColor.current,
                ),
                border = BorderStroke(1.5.dp, LocalContentColor.current)
            ) {
                Text("Get Started")
                Icon(TtcIcons.ArrowForward, contentDescription = "Get Started Arrow")
            }
        }
    }
}

@Preview
@Composable
fun WelcomeScreenPreview() {
    MyApplicationTheme {
        WelcomeScreen()
    }
}