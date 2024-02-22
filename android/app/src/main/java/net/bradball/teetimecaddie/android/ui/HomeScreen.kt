package net.bradball.teetimecaddie.android.ui
//
//import android.util.Log
//import androidx.compose.foundation.layout.Column
//import androidx.compose.material3.Button
//import androidx.compose.material3.CenterAlignedTopAppBar
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.navigation.NavGraphBuilder
//import androidx.navigation.NavHostController
//import androidx.navigation.NavOptions
//import androidx.navigation.compose.composable
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.ktx.auth
//import com.google.firebase.installations.remote.TokenResult
//import com.google.firebase.internal.InternalTokenResult
//import com.google.firebase.ktx.Firebase
//import kotlinx.coroutines.delay
//import net.bradball.teetimecaddie.android.ui.common.Screen
//import net.bradball.teetimecaddie.android.ui.navigation.clearBackStack
//import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen
//
//
//const val homeRoute = "home"
//private const val screenName = "Home"
//
//fun NavHostController.navigateToHome(navOptions: NavOptions? = clearBackStack(this)) {
//    this.navigate(homeRoute, navOptions)
//}
//
//fun NavGraphBuilder.homeScreen() {
//    composable(homeRoute) {
//        HomeRoute()
//    }
//}
//
//@Composable
//fun HomeRoute() {
//    HomeScreen()
//}
//
//@Composable
//private fun HomeScreen(onLoginClick: ()->Unit = { /* navigate to login screen */ }) {
//
//    var isLoggedIn by remember {
//        val loginState = !(Firebase.auth.currentUser?.isAnonymous ?: true)
//        Log.d("LOGIN", "Is logged in: $loginState")
//        mutableStateOf(loginState)
//    }
//
//    LaunchedEffect(Unit) {
//        delay(3_000)
//        val loginState = !(Firebase.auth.currentUser?.isAnonymous ?: true)
//        Log.d("LOGIN", "Is logged in after delay: $loginState")
//        isLoggedIn = loginState
//    }
//
//    LaunchedEffect(Unit) {
//        Firebase.auth.addAuthStateListener { auth ->
//            Log.d("LOGIN", "Current User: ${auth.currentUser}")
//        }
//
//        Firebase.auth.addIdTokenListener { auth: FirebaseAuth ->
//            Log.d("LOGIN", "Token Changed for user: ${auth.currentUser}")
//        }
//    }
//
//    Screen(AnalyticsScreen.None) {
//        Column {
//            CenterAlignedTopAppBar(
//                title = { Text("Home") }
//            )
//
//            if (isLoggedIn) {
//                Text("Welcome, Authenticated User!")
//                Button(onClick = { Firebase.auth.signOut() }) { Text("Log out") }
//            } else {
//                Button(onClick = {
//                    val task = Firebase.auth.signInWithEmailAndPassword("bradleycorn@gmail.com", "cr9pt1c4l")
//                    task.addOnCompleteListener { Log.d("LOGIN", "Login SUCCESS!") }
//                    task.addOnFailureListener { ex ->
//                        throw ex
//                    }
//                }) {
//                    Text("Log In")
//                }
//            }
//        }
//    }
//}
