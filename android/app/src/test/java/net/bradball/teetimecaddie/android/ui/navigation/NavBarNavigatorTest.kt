package net.bradball.teetimecaddie.android.ui.navigation

import net.bradball.teetimecaddie.android.feature.games.navigation.GameDetailsDestination
import net.bradball.teetimecaddie.android.feature.games.navigation.NewGameDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavBarNavigatorTest {

    @Test
    fun isAtTopLevel_onEachTabsRoot() {
        val navigator = NavBarNavigator(TopLevelDestination.GAMES)
        assertTrue(navigator.isAtTopLevel)

        navigator.navigate(TopLevelDestination.PROFILE)
        assertTrue(navigator.isAtTopLevel)
    }

    @Test
    fun isNotAtTopLevel_afterPushingNewGame() {
        val navigator = NavBarNavigator(TopLevelDestination.GAMES)

        navigator.navigate(NewGameDestination)

        assertFalse(navigator.isAtTopLevel)
    }

    @Test
    fun isNotAtTopLevel_afterPushingGameDetails() {
        val navigator = NavBarNavigator(TopLevelDestination.GAMES)

        navigator.navigate(GameDetailsDestination("game-1"))

        assertFalse(navigator.isAtTopLevel)
    }

    @Test
    fun isAtTopLevel_againAfterGoingBack() {
        val navigator = NavBarNavigator(TopLevelDestination.GAMES)
        navigator.navigate(NewGameDestination)

        navigator.goBack()

        assertTrue(navigator.isAtTopLevel)
    }

    @Test
    fun goBack_twiceFromPushedScreen_staysOnTabRoot() {
        val navigator = NavBarNavigator(TopLevelDestination.GAMES)
        navigator.navigate(NewGameDestination)

        navigator.goBack()
        navigator.goBack()

        assertEquals(listOf(TopLevelDestination.GAMES.destination), navigator.backStack.toList())
    }

    @Test
    fun goBack_onTabRoot_isNoOp() {
        val navigator = NavBarNavigator(TopLevelDestination.GAMES)
        navigator.navigate(TopLevelDestination.PROFILE)

        navigator.goBack()

        assertEquals(TopLevelDestination.PROFILE, navigator.selectedTopLevelDestination)
        assertEquals(listOf(TopLevelDestination.PROFILE.destination), navigator.backStack.toList())
    }
}
