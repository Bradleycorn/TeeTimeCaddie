package net.bradball.teetimecaddie.android.ui.navigation

import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.navOptions

fun clearBackStack(navHostController: NavHostController): NavOptions = navOptions {
    popUpTo(navHostController.graph.id) { inclusive = true }
}
