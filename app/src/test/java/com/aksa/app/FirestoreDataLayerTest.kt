package com.aksa.app

import com.aksa.app.data.repository.demo.DemoAchievementRepository
import com.aksa.app.data.repository.demo.DemoProfileRepository
import com.aksa.app.data.repository.demo.DemoSessionRepository
import com.aksa.app.domain.model.AnswerLog
import com.aksa.app.domain.model.Profile
import com.aksa.app.domain.model.ProfileConfig
import com.aksa.app.domain.model.SessionResult
import com.aksa.app.domain.model.SessionStatus
import com.aksa.app.ui.state.AksaUserState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class FirestoreDataLayerTest {

    private lateinit var profileRepository: DemoProfileRepository
    private lateinit var sessionRepository: DemoSessionRepository
    private lateinit var achievementRepository: DemoAchievementRepository

    @Before
    fun setUp() {
        profileRepository = DemoProfileRepository()
        sessionRepository = DemoSessionRepository()
        achievementRepository = DemoAchievementRepository()
        ProfileConfig.maxProfilesPerAccount = null
    }

    @Test
    fun multiProfileCreationAndRealtimeFlowObservation() = runTest {
        val parentUid = "parent_test_${UUID.randomUUID()}"

        // Initial state: 0 profiles
        val initialList = profileRepository.observeProfiles(parentUid).first()
        assertTrue(initialList.isEmpty())

        // Create profile 1
        val p1 = profileRepository.createProfile(parentUid, "Kakak Budi", "mascot_classic").getOrThrow()
        assertEquals(parentUid, p1.userId)
        assertEquals("Kakak Budi", p1.name)
        assertEquals("mascot_classic", p1.avatarId)

        // Create profile 2
        val p2 = profileRepository.createProfile(parentUid, "Adik Siti", "cat_orange").getOrThrow()
        assertEquals(parentUid, p2.userId)
        assertEquals("Adik Siti", p2.name)
        assertEquals("cat_orange", p2.avatarId)

        // Observe real-time list
        val updatedList = profileRepository.observeProfiles(parentUid).first()
        assertEquals(2, updatedList.size)
        assertEquals("Kakak Budi", updatedList[0].name)
        assertEquals("Adik Siti", updatedList[1].name)
    }

    @Test
    fun profileUpdateVerifiesOwnershipAndPersists() = runTest {
        val parentUid = "parent_owner_1"
        val attackerUid = "parent_attacker_2"

        val profile = profileRepository.createProfile(parentUid, "Nama Awal", "mascot_classic").getOrThrow()

        // 1. Legitimate owner updates
        val updateResult = profileRepository.updateProfile(
            userId = parentUid,
            profileId = profile.profileId,
            name = "Nama Baru",
            avatarId = "dino_lime"
        )
        assertTrue(updateResult.isSuccess)
        val updatedProfile = updateResult.getOrThrow()
        assertEquals("Nama Baru", updatedProfile.name)
        assertEquals("dino_lime", updatedProfile.avatarId)

        // Verify query reflects update
        val fetched = profileRepository.getProfile(parentUid, profile.profileId)
        assertNotNull(fetched)
        assertEquals("Nama Baru", fetched?.name)
        assertEquals("dino_lime", fetched?.avatarId)

        // 2. Attacker attempts to update someone else's profile
        val attackerResult = profileRepository.updateProfile(
            userId = attackerUid,
            profileId = profile.profileId,
            name = "Nama Bajakan",
            avatarId = "bunny_blue"
        )
        assertFalse("Attacker update must fail", attackerResult.isSuccess)
    }

    @Test
    fun parentDataIsolationPreventsCrossAccountAccess() = runTest {
        val parentA = "parent_A"
        val parentB = "parent_B"

        profileRepository.createProfile(parentA, "Anak Parent A", "mascot_champion").getOrThrow()
        profileRepository.createProfile(parentB, "Anak Parent B", "bunny_blue").getOrThrow()

        val profilesA = profileRepository.getProfiles(parentA)
        val profilesB = profileRepository.getProfiles(parentB)

        assertEquals(1, profilesA.size)
        assertEquals("Anak Parent A", profilesA[0].name)

        assertEquals(1, profilesB.size)
        assertEquals("Anak Parent B", profilesB[0].name)
    }

    @Test
    fun configurableProfileLimitEnforced() = runTest {
        val parentUid = "parent_limit_test"
        ProfileConfig.maxProfilesPerAccount = 2

        val r1 = profileRepository.createProfile(parentUid, "Child 1", "mascot_classic")
        assertTrue(r1.isSuccess)

        val r2 = profileRepository.createProfile(parentUid, "Child 2", "mascot_classic")
        assertTrue(r2.isSuccess)

        // Exceeding limit
        val r3 = profileRepository.createProfile(parentUid, "Child 3", "mascot_classic")
        assertFalse(r3.isSuccess)
        assertTrue(r3.exceptionOrNull()?.message?.contains("Batas maksimum") == true)
    }

    @Test
    fun learningProgressDataIsIsolatedPerProfile() = runTest {
        val profile1Id = "p_child_1"
        val profile2Id = "p_child_2"

        // Record session for child 1
        val session1 = SessionResult(
            sessionId = "session_1",
            childId = profile1Id,
            moduleId = "modul_1",
            activityId = "tangkap_huruf",
            startedAt = 1000L,
            completedAt = 2000L,
            totalQuestions = 15,
            correctAnswers = 14,
            incorrectAnswers = 1,
            accuracy = 0.93f,
            status = SessionStatus.COMPLETED
        )
        sessionRepository.saveSessionResult(session1)

        // Record answer for child 1
        val answer1 = AnswerLog(
            answerId = "ans_1",
            sessionId = "session_1",
            childId = profile1Id,
            moduleId = "modul_1",
            activityId = "tangkap_huruf",
            questionId = "q_1",
            targetChar = "A",
            chosenChar = "A",
            isCorrect = true,
            responseTimeMs = 800L
        )
        sessionRepository.recordAnswer(answer1)

        // Child 1 has 1 completed session
        val sessionsChild1 = sessionRepository.getCompletedSessions(profile1Id)
        assertEquals(1, sessionsChild1.size)
        assertEquals("session_1", sessionsChild1[0].sessionId)

        // Child 2 has 0 completed sessions (isolated)
        val sessionsChild2 = sessionRepository.getCompletedSessions(profile2Id)
        assertTrue("Child 2 must have no completed sessions", sessionsChild2.isEmpty())
    }

    @Test
    fun badgeAwardingIsIsolatedPerProfile() = runTest {
        val profile1Id = "child_1"
        val profile2Id = "child_2"
        val week = "2026-11-09"

        // Award badge to child 1
        val awarded = achievementRepository.awardMilestoneIfEligible(profile1Id, week, 2)
        assertTrue(awarded)

        // Attempt duplicate award for child 1 same week -> false (idempotent)
        val duplicate = achievementRepository.awardMilestoneIfEligible(profile1Id, week, 2)
        assertFalse(duplicate)

        // Child 1 has 1 badge
        val badgesChild1 = achievementRepository.getAchievements(profile1Id)
        assertEquals(1, badgesChild1.size)

        // Child 2 has 0 badges (isolated)
        val badgesChild2 = achievementRepository.getAchievements(profile2Id)
        assertTrue(badgesChild2.isEmpty())
    }

    @Test
    fun allAvailableAvatarsAreConfiguredAndPersistCorrectly() = runTest {
        val parentUid = "parent_avatar_test"
        val avatars = AksaUserState.availableAvatars
        assertEquals(6, avatars.size)

        for (avatar in avatars) {
            assertTrue("Avatar ID must not be blank", avatar.id.isNotBlank())
            assertTrue("Avatar name must not be blank", avatar.name.isNotBlank())

            val result = profileRepository.createProfile(
                userId = parentUid,
                name = "Anak ${avatar.name}",
                avatarId = avatar.id
            )
            assertTrue(result.isSuccess)
            val created = result.getOrThrow()
            assertEquals(avatar.id, created.avatarId)
        }
    }
}

