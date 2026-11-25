package net.bradball.teetimecaddie.android.feature.auth.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import net.bradball.teetimecaddie.android.ui.navigation.BasicNavigator
import net.bradball.teetimecaddie.android.ui.navigation.Navigator
import net.bradball.teetimecaddie.android.ui.navigation.TtcNavKey
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthNavigationTest {

    // The profile step left on screen after the session ended was TTC-127.
    @Test
    fun returnToLogin_dropsTheProfileStep() {
        val navigator = BasicNavigator(LoginDestination, CreateAccountDestination("new@golf.app"))

        navigator.returnToLogin()

        assertEquals(listOf(LoginDestination), navigator.backStack.toList())
    }

    @Test
    fun returnToLogin_replacesAStackWithoutLoginAtTheBottom() {
        val navigator = BasicNavigator(CreateAccountDestination("new@golf.app"))

        navigator.returnToLogin()

        assertEquals(listOf(LoginDestination), navigator.backStack.toList())
    }

    // Going back from the profile step pops to Login and then signs out; the reset that follows
    // must leave that entry alone, or the email typed on it would be lost.
    @Test
    fun returnToLogin_leavesALoneLoginEntryUntouched() {
        val navigator = RecordingNavigator(LoginDestination)

        navigator.returnToLogin()

        assertEquals(0, navigator.navigations)
        assertEquals(listOf(LoginDestination), navigator.backStack.toList())
    }

    private class RecordingNavigator(vararg start: NavKey) : Navigator {
        override val backStack: SnapshotStateList<NavKey> = mutableStateListOf(*start)
        override val currentDestination: NavKey? get() = backStack.lastOrNull()
        var navigations = 0
            private set

        override fun navigate(destination: TtcNavKey, clearBackStack: Boolean) {
            navigations++
            if (clearBackStack) backStack.clear()
            backStack.add(destination)
        }

        override fun goBack() {
            if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
        }
    }
}
