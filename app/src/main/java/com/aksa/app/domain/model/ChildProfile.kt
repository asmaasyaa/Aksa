package com.aksa.app.domain.model

object ProfileConfig {
    var maxProfilesPerAccount: Int? = null
}

data class Profile(
    val profileId: String,
    val userId: String,
    val name: String,
    val avatarId: String,
    val age: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val childId: String get() = profileId
    val guardianUid: String get() = userId
    val displayName: String get() = name

    init {
        require(name.isNotBlank()) { "Profile name cannot be blank" }
        require(avatarId.isNotBlank()) { "Profile avatarId cannot be blank" }
    }
}

typealias ChildProfile = Profile

