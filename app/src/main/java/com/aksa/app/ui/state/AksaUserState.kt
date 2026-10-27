package com.aksa.app.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aksa.app.domain.model.ChildProfile

data class AksaAvatarItem(
    val id: String,
    val name: String
)

/**
 * Global UI helper retaining avatar assets and bridging legacy prototype screens
 * to real repository-backed profile state. Plaintext passwords have been removed completely.
 */
object AksaUserState {
    // Active child profile state (synced with repository/ViewModel)
    var username by mutableStateOf("Marvin")
    var avatarId by mutableStateOf("mascot_classic")
    var age by mutableIntStateOf(8)

    // Learning metrics driven by real session history
    var level by mutableStateOf("Modul 1: Huruf")
    var streakCount by mutableIntStateOf(0)
    var hurufMiripCompleted by mutableStateOf(false)

    // Child-friendly avatars matching AKSA flat aesthetic
    val availableAvatars = listOf(
        AksaAvatarItem("mascot_classic", "Aksa"),
        AksaAvatarItem("mascot_champion", "Juara"),
        AksaAvatarItem("mascot_happy", "Bintang"),
        AksaAvatarItem("cat_orange", "Si Oren"),
        AksaAvatarItem("bunny_blue", "Si Biru"),
        AksaAvatarItem("dino_lime", "Si Dino")
    )

    fun syncFromProfile(profile: ChildProfile, streak: Int = 0) {
        username = profile.displayName
        avatarId = profile.avatarId
        age = profile.age
        streakCount = streak
    }

    fun updateProfile(newUsername: String, newAvatarId: String, newAge: Int) {
        username = newUsername
        avatarId = newAvatarId
        age = newAge
    }
}
