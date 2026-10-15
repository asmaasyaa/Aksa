package com.aksa.app.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class AksaAvatarItem(
    val id: String,
    val name: String
)

object AksaUserState {
    // Current child profile state
    var username by mutableStateOf("Marvin")
    var password by mutableStateOf("123456")
    var avatarId by mutableStateOf("mascot_classic")
    var age by mutableIntStateOf(8)

    // Gamification state
    var level by mutableStateOf("LV 3 Master")
    var streakCount by mutableIntStateOf(9)
    var hurufMiripCompleted by mutableStateOf(false)

    // Available child-friendly avatars matching AKSA flat aesthetic
    val availableAvatars = listOf(
        AksaAvatarItem("mascot_classic", "Aksa"),
        AksaAvatarItem("mascot_champion", "Juara"),
        AksaAvatarItem("mascot_happy", "Bintang"),
        AksaAvatarItem("cat_orange", "Si Oren"),
        AksaAvatarItem("bunny_blue", "Si Biru"),
        AksaAvatarItem("dino_lime", "Si Dino")
    )

    fun updateProfile(newUsername: String, newPassword: String, newAvatarId: String, newAge: Int) {
        username = newUsername
        password = newPassword
        avatarId = newAvatarId
        age = newAge
    }
}
