package com.aksa.app

import com.aksa.app.data.repository.demo.DemoAuthRepository
import com.aksa.app.data.repository.demo.DemoProfileRepository
import com.aksa.app.domain.model.Profile
import com.aksa.app.ui.state.AksaUserState
import com.aksa.app.ui.viewmodel.ProfileManagementViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChildProfileManagementTest {

    private lateinit var authRepository: DemoAuthRepository
    private lateinit var profileRepository: DemoProfileRepository

    @Before
    fun setUp() {
        authRepository = DemoAuthRepository()
        profileRepository = DemoProfileRepository()
        AksaUserState.clearSession()
    }

    @Test
    fun createFirstProfile_withAgeAndAvatar_persistsSuccessfully() = runTest {
        val parentUid = "parent_123"
        val result = profileRepository.createProfile(
            userId = parentUid,
            name = "Deva",
            avatarId = "mascot_champion",
            age = 8
        )

        assertTrue(result.isSuccess)
        val profile = result.getOrThrow()
        assertEquals("Deva", profile.name)
        assertEquals(8, profile.age)
        assertEquals("mascot_champion", profile.avatarId)
        assertEquals(parentUid, profile.userId)

        // Verify retrieval
        val profiles = profileRepository.getProfiles(parentUid)
        assertEquals(1, profiles.size)
        assertEquals("Deva", profiles[0].name)
        assertEquals(8, profiles[0].age)
    }

    @Test
    fun createMultipleProfiles_persistsDistinctProfilesUnderSameParent() = runTest {
        val parentUid = "parent_multi"

        val p1 = profileRepository.createProfile(parentUid, "Kakak", "cat_orange", age = 10).getOrThrow()
        val p2 = profileRepository.createProfile(parentUid, "Adik", "bunny_blue", age = 6).getOrThrow()

        val allProfiles = profileRepository.getProfiles(parentUid)
        assertEquals(2, allProfiles.size)

        val retrievedP1 = allProfiles.find { it.profileId == p1.profileId }
        val retrievedP2 = allProfiles.find { it.profileId == p2.profileId }

        assertNotNull(retrievedP1)
        assertNotNull(retrievedP2)
        assertEquals("Kakak", retrievedP1?.name)
        assertEquals(10, retrievedP1?.age)
        assertEquals("cat_orange", retrievedP1?.avatarId)

        assertEquals("Adik", retrievedP2?.name)
        assertEquals(6, retrievedP2?.age)
        assertEquals("bunny_blue", retrievedP2?.avatarId)
    }

    @Test
    fun switchProfile_updatesUserStateAndActiveIdCorrectly() = runTest {
        val parentUid = "parent_switch"
        val p1 = profileRepository.createProfile(parentUid, "Anak 1", "mascot_classic", age = 7).getOrThrow()
        val p2 = profileRepository.createProfile(parentUid, "Anak 2", "dino_lime", age = 9).getOrThrow()

        // Sync to Profile 1
        AksaUserState.syncFromProfile(p1)
        assertEquals(p1.profileId, AksaUserState.currentProfileId)
        assertEquals("Anak 1", AksaUserState.username)
        assertEquals(7, AksaUserState.age)
        assertEquals("mascot_classic", AksaUserState.avatarId)

        // Switch to Profile 2
        AksaUserState.syncFromProfile(p2)
        assertEquals(p2.profileId, AksaUserState.currentProfileId)
        assertEquals("Anak 2", AksaUserState.username)
        assertEquals(9, AksaUserState.age)
        assertEquals("dino_lime", AksaUserState.avatarId)
    }

    @Test
    fun updateProfile_modifiesOnlyTargetProfileWithoutAffectingOthers() = runTest {
        val parentUid = "parent_update"
        val p1 = profileRepository.createProfile(parentUid, "Original 1", "mascot_classic", age = 7).getOrThrow()
        val p2 = profileRepository.createProfile(parentUid, "Original 2", "cat_orange", age = 9).getOrThrow()

        // Update profile 1 only
        val updateResult = profileRepository.updateProfile(
            userId = parentUid,
            profileId = p1.profileId,
            name = "Updated 1",
            avatarId = "mascot_happy",
            age = 8
        )

        assertTrue(updateResult.isSuccess)
        val updatedP1 = updateResult.getOrThrow()
        assertEquals("Updated 1", updatedP1.name)
        assertEquals(8, updatedP1.age)
        assertEquals("mascot_happy", updatedP1.avatarId)

        // Verify profile 2 is completely unchanged
        val fetchedP2 = profileRepository.getProfile(parentUid, p2.profileId)
        assertNotNull(fetchedP2)
        assertEquals("Original 2", fetchedP2?.name)
        assertEquals(9, fetchedP2?.age)
        assertEquals("cat_orange", fetchedP2?.avatarId)
    }

    @Test
    fun logout_clearsActiveProfileContext() = runTest {
        val profile = Profile(
            profileId = "profile_active_test",
            userId = "parent_test",
            name = "Test Anak",
            avatarId = "cat_orange",
            age = 8
        )

        AksaUserState.syncFromProfile(profile)
        assertEquals("profile_active_test", AksaUserState.currentProfileId)
        assertEquals("Test Anak", AksaUserState.username)

        // Perform clearSession
        AksaUserState.clearSession()
        assertTrue(AksaUserState.currentProfileId.isBlank())
        assertTrue(AksaUserState.username.isBlank())
    }

    @Test
    fun getActiveChildProfile_respectsActiveProfileIdIfProvided() = runTest {
        val parentUid = "parent_active_query"
        val p1 = profileRepository.createProfile(parentUid, "Anak Pertama", "mascot_classic", age = 8).getOrThrow()
        val p2 = profileRepository.createProfile(parentUid, "Anak Kedua", "bunny_blue", age = 6).getOrThrow()

        // Without active ID, defaults to first profile
        val defaultActive = profileRepository.getActiveChildProfile(parentUid)
        assertEquals(p1.profileId, defaultActive?.profileId)

        // With active ID pointing to p2, returns p2
        val specificActive = profileRepository.getActiveChildProfile(parentUid, p2.profileId)
        assertEquals(p2.profileId, specificActive?.profileId)
        assertEquals("Anak Kedua", specificActive?.name)
    }
}

