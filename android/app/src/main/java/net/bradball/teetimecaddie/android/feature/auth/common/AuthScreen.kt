package net.bradball.teetimecaddie.android.feature.auth.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.Screen
import net.bradball.teetimecaddie.android.ui.common.forms.InputSpacer
import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen
import net.bradball.teetimecaddie.features.auth.AR

@Composable
fun AuthScreen(
    analyticsScreen: AnalyticsScreen,
    title: String,
    footerText: String? = null,
    footerActionText: String? = null,
    onFooterActionClick: ()->Unit = {},
    content: @Composable ColumnScope.()->Unit) {

    val scrollState = rememberScrollState()

    Screen(analyticsScreen, modifier = Modifier.fillMaxSize().imePadding()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {

            AuthHeader(title)

            content()

            footerText?.let {
                Spacer(modifier = Modifier.weight(1f))

                AuthFooter(
                    text = it,
                    actionText = footerActionText,
                    onActionClick = onFooterActionClick
                )

                InputSpacer()
            }
        }
    }
}

@Preview
@Composable
fun PreviewAuthScreen() {
    MyApplicationTheme {
        AuthScreen(AnalyticsScreen.None, "Auth Screen", "Do Something Else?", "Click Here") {
            Text("This is an Auth Screen")
        }
    }
}