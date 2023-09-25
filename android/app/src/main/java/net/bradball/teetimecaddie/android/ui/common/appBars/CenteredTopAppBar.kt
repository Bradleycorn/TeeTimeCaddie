package net.bradball.teetimecaddie.android.ui.common.appBars

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun TtcCenteredTopAppBar(title: String) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
    )
}
