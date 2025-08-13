package net.bradball.teetimecaddie.android.ui.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.initializers.InitializationState
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons

@Composable
fun AppErrorScreen(failedState: InitializationState.Failed) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.size(128.dp))
            Icon(
                TtcIcons.TeeBalStrikethrough,
                contentDescription = "TeeTimeCaddie",
                modifier = Modifier.size(64.dp)
            )
            Text( "We Shanked One Off the First Tee!", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.size(64.dp))
            failedState.error.message?.let { error ->
                Text(error)
            }
        }
    }
}

@Preview
@Composable
fun AppErrorScreenPreview() {
    MyApplicationTheme {
        AppErrorScreen(InitializationState.Failed(Exception("A startup error occurred.")))
    }
}