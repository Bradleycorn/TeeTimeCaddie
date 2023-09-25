package net.bradball.teetimecaddie.android.feature.auth.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.R
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.appBars.TtcCenteredTopAppBar

@Composable
fun AuthHeader(title: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        
        TtcCenteredTopAppBar(title)
    
        Icon(
            painterResource(id = R.drawable.app_logo),
            contentDescription = "TeeTimeCaddie",
            modifier = Modifier.size(128.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.size(32.dp))
    }
}

@Preview
@Composable
fun PreviewAuthHeader() {
    MyApplicationTheme {
        Surface {
            AuthHeader(title = "Authentication")
        }
    }
}