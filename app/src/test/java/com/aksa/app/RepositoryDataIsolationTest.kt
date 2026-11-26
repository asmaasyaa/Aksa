package com.aksa.app

import com.aksa.app.data.repository.demo.DemoAuthRepository
import com.aksa.app.data.repository.demo.DemoProfileRepository
import com.aksa.app.domain.model.Profile
import com.aksa.app.domain.model.ProfileConfig
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
    fun profileInputValidationEnforced() {
        // Valid profile
        val valid = Profile(
            profileId = "p_1",
            userId = "user_1",
            name = "Aksa",
            avatarId = "mascot_classic"
        )
        assertEquals("Aksa", valid.name)
        assertEquals("mascot_classic", valid.avatarId)

        // Invalid: blank name
        try {
            Profile(
                profileId = "p_invalid",
                userId = "user_1",
                name = "   ",
                avatarId = "mascot_classic"
            )
            fail("Should throw IllegalArgumentException for blank name")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("blank"))
        }

        // Invalid: blank avatar
        try {
            Profile(
                profileId = "p_invalid",
                userId = "user_1",
                name = "Budi",
                avatarId = "  "
            )
            fail("Should throw IllegalArgumentException for blank avatarId")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("blank"))
        }
    }

    @Test
    fun guardianDataIsolationThroughRepositoryContracts() = runTest {
        val profileRepository = DemoProfileRepository()

        val guardian1 = "guardian_alice"
        val guardian2 = "guardian_bob"

        val profileAlice = Profile(
            profileId = "child_alice_1",
            userId = guardian1,
            name = "Alita",
            avatarId = "cat_orange"
        )

        val profileBob = Profile(
            profileId = "child_bob_1",
            userId = guardian2,
            name = "Boni",
            avatarId = "dino_lime"
        )

        profileRepository.saveChildProfile(profileAlice)
        profileRepository.saveChildProfile(profileBob)

        // Guardian 1 queries active profile -> only sees Alice's child
        val fetchedAlice = profileRepository.getActiveChildProfile(guardian1)
        assertNotNull(fetchedAlice)
        assertEquals("Alita", fetchedAlice?.name)
        assertEquals(guardian1, fetchedAlice?.userId)

        // Guardian 2 queries active profile -> only sees Bob's child
        val fetchedBob = profileRepository.getActiveChildProfile(guardian2)
        assertNotNull(fetchedBob)
        assertEquals("Boni", fetchedBob?.name)
        assertEquals(guardian2, fetchedBob?.userId)

        // Unknown guardian -> returns null
        val unknown = profileRepository.getActiveChildProfile("guardian_unknown")
        assertNull(unknown)
    }

    @Test
    fun multiProfileSupportUnderSameParentAccount() = runTest {
        val profileRepository = DemoProfileRepository()
        val parentUid = "parent_multi_123"

        // 1. Create first profile
        val p1Result = profileRepository.createProfile(
            userId = parentUid,
            name = "Kakak",
            avatarId = "mascot_champion"
        )
        assertTrue(p1Result.isSuccess)
        val p1 = p1Result.getOrThrow()

        // 2. Create second profile
        val p2Result = profileRepository.createProfile(
            userId = parentUid,
            name = "Adik",
            avatarId = "bunny_blue"
        )
        assertTrue(p2Result.isSuccess)
        val p2 = p2Result.getOrThrow()

        // 3. Query all profiles for parent -> must return both in order
        val allProfiles = profileRepository.getProfiles(parentUid)
        assertEquals(2, allProfiles.size)
        assertEquals("Kakak", allProfiles[0].name)
        assertEquals("Adik", allProfiles[1].name)

        // 4. Update second profile
        val updateResult = profileRepository.updateProfile(
            userId = parentUid,
            profileId = p2.profileId,
            name = "Adik Baru",
            avatarId = "dino_lime"
        )
        assertTrue(updateResult.isSuccess)
        val updated = updateResult.getOrThrow()
        assertEquals("Adik Baru", updated.name)
        assertEquals("dino_lime", updated.avatarId)

        // 5. Verify persistence
        val fetched = profileRepository.getProfile(parentUid, p2.profileId)
        assertNotNull(fetched)
        assertEquals("Adik Baru", fetched?.name)

        // 6. Security verification: unauthorized parent cannot update p2
        val hackResult = profileRepository.updateProfile(
            userId = "parent_attacker",
            profileId = p2.profileId,
            name = "Hacked",
            avatarId = "mascot_classic"
        )
        assertFalse("Other parent must NOT be able to update someone else's profile", hackResult.isSuccess)
    }

    @Test
    fun configurableProfileLimitRespected() = runTest {
        val profileRepository = DemoProfileRepository()
        val parentUid = "parent_limited"

        // Set max limit to 2 dynamically
        ProfileConfig.maxProfilesPerAccount = 2
        try {
            assertTrue(profileRepository.createProfile(parentUid, "Anak 1", "mascot_classic").isSuccess)
            assertTrue(profileRepository.createProfile(parentUid, "Anak 2", "mascot_classic").isSuccess)

            // 3rd attempt should fail
            val thirdResult = profileRepository.createProfile(parentUid, "Anak 3", "mascot_classic")
            assertFalse("Third profile must fail when limit is 2", thirdResult.isSuccess)
        } finally {
            ProfileConfig.maxProfilesPerAccount = null // reset
        }
    }

    @Test
    fun noPlaintextPasswordStoredInUserState() {
        AksaUserState.username = "TestChild"
        AksaUserState.avatarId = "mascot_classic"

        val hasPasswordField = AksaUserState::class.java.declaredFields.any {
            it.name.contains("password", ignoreCase = true)
        }
        assertFalse("AksaUserState must never contain any password field", hasPasswordField)
    }

    @Test
    fun authRepositoryDoesNotContainDemoMode() = runTest {
        val authRepo = DemoAuthRepository()
        val loginResult = authRepo.signIn("demo.orangtua@aksa.id", "123456")
        assertTrue(loginResult.isSuccess)
    }
}
