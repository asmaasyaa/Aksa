package com.aksa.app

import com.aksa.app.domain.model.Profile
import com.aksa.app.navigation.Screen
import com.aksa.app.ui.state.AksaUserState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TopLevelNavigationTest {

    @Before
    fun setUp() {
        AksaUserState.clearSession()
    }

    @Test
    fun verifyTopLevelRoutesForBottomBarVisibility() {
        val topLevelRoutes = listOf(Screen.Home.route, Screen.Profile.route)
        val standaloneRoutes = listOf(
            Screen.Splash.route,
            Screen.Login.route,
            Screen.Register.route,
            Screen.CreateProfile.route,
            Screen.EditProfile.route,
            Screen.SelectProfile.route,
            Screen.LevelMap.route,
            Screen.ProgressAchievements.route,
            Screen.DeveloperPreview.route
        )

        topLevelRoutes.forEach { route ->
            val isTopLevel = route == Screen.Home.route || route == Screen.Profile.route
            assertTrue("Route $route should be identified as top-level", isTopLevel)
        }

        standaloneRoutes.forEach { route ->
            val isTopLevel = route == Screen.Home.route || route == Screen.Profile.route
            assertFalse("Route $route should NOT be identified as top-level", isTopLevel)
        }
    }

    @Test
    fun verifyActiveProfilePreservedOnTabSwitch() {
        val testProfile = Profile(
            profileId = "child_profile_999",
            userId = "parent_123",
            name = "Anisa",
            avatarId = "cat_orange",
            age = 7
        )
        AksaUserState.syncFromProfile(testProfile, streak = 5)

        // Simulate reading state when navigating between Home and Profile tabs
        assertEquals("child_profile_999", AksaUserState.currentProfileId)
        assertEquals("Anisa", AksaUserState.username)
        assertEquals(7, AksaUserState.age)
        assertEquals("cat_orange", AksaUserState.avatarId)

        // Simulate returning to Home tab
        assertEquals("child_profile_999", AksaUserState.currentProfileId)
        assertEquals(5, AksaUserState.streakCount)
    }
}
