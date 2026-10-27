package com.aksa.app

import com.aksa.app.data.repository.demo.DemoAuthRepository
import com.aksa.app.data.repository.demo.DemoProfileRepository
import com.aksa.app.domain.model.ChildProfile
import com.aksa.app.ui.state.AksaUserState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class RepositoryDataIsolationTest {

    @Test
    fun childAgeValidationEnforced() {
        // Valid ages: 6 to 12
        for (age in 6..12) {
            val profile = ChildProfile(
                childId = "c_$age",
                guardianUid = "g_1",
                displayName = "Anak $age",
                age = age,
                avatarId = "mascot_classic"
            )
            assertEquals(age, profile.age)
        }

        // Invalid age < 6
        try {
            ChildProfile(
                childId = "c_invalid",
                guardianUid = "g_1",
                displayName = "Balita",
                age = 5,
                avatarId = "mascot_classic"
            )
            fail("Should throw IllegalArgumentException for age 5")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("6 and 12"))
        }

        // Invalid age > 12
        try {
            ChildProfile(
                childId = "c_invalid",
                guardianUid = "g_1",
                displayName = "Remaja",
                age = 13,
                avatarId = "mascot_classic"
            )
            fail("Should throw IllegalArgumentException for age 13")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("6 and 12"))
        }
    }

    @Test
    fun guardianDataIsolationThroughRepositoryContracts() = runTest {
        val profileRepository = DemoProfileRepository()

        val guardian1 = "guardian_alice"
        val guardian2 = "guardian_bob"

        val profileAlice = ChildProfile(
            childId = "child_alice_1",
            guardianUid = guardian1,
            displayName = "Alita",
            age = 7,
            avatarId = "cat_orange"
        )

        val profileBob = ChildProfile(
            childId = "child_bob_1",
            guardianUid = guardian2,
            displayName = "Boni",
            age = 9,
            avatarId = "dino_lime"
        )

        profileRepository.saveChildProfile(profileAlice)
        profileRepository.saveChildProfile(profileBob)

        // Guardian 1 queries active profile -> only sees Alice's child
        val fetchedAlice = profileRepository.getActiveChildProfile(guardian1)
        assertNotNull(fetchedAlice)
        assertEquals("Alita", fetchedAlice?.displayName)
        assertEquals(guardian1, fetchedAlice?.guardianUid)

        // Guardian 2 queries active profile -> only sees Bob's child
        val fetchedBob = profileRepository.getActiveChildProfile(guardian2)
        assertNotNull(fetchedBob)
        assertEquals("Boni", fetchedBob?.displayName)
        assertEquals(guardian2, fetchedBob?.guardianUid)

        // Unknown guardian -> returns null
        val unknown = profileRepository.getActiveChildProfile("guardian_unknown")
        assertNull(unknown)
    }

    @Test
    fun noPlaintextPasswordStoredInUserState() {
        // Verify AksaUserState fields
        AksaUserState.username = "TestChild"
        AksaUserState.age = 8
        AksaUserState.avatarId = "mascot_classic"

        // AksaUserState must have no password property
        val hasPasswordField = AksaUserState::class.java.declaredFields.any {
            it.name.contains("password", ignoreCase = true)
        }
        assertFalse("AksaUserState must never contain any password field", hasPasswordField)
    }

    @Test
    fun demoModeDoesNotClaimCrossDeviceSync() = runTest {
        val authRepo = DemoAuthRepository()
        assertTrue("Demo mode must be explicitly flagged", authRepo.isDemoMode)
        val loginResult = authRepo.signIn("demo.orangtua@aksa.id", "123456")
        assertTrue(loginResult.isSuccess)
        assertTrue(loginResult.getOrThrow().isDemo)
    }
}
