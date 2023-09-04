package net.bradball.teetimecaddie.android.feature.auth.login

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginRoute(
    onRegisterClick: () -> Unit,
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()) {
    LoginScreen(onRegisterClick = onRegisterClick, onLoggedIn = onLoggedIn)
}

@Composable
private fun LoginScreen(onRegisterClick: ()->Unit = {}, onLoggedIn: ()->Unit = {}) {
    Column {
        CenterAlignedTopAppBar(title = {
            Text("Login")
        })

        Button(onClick = { onRegisterClick() }) {
            Text("Create Account")
        }

        Button(onClick = { onLoggedIn() }) {
            Text("Login")
        }
    }
}