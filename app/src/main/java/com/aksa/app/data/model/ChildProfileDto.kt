package com.aksa.app.data.model

import com.aksa.app.domain.model.Profile

/**
 * Firestore DTO for Profile.
 * Stores profileId, userId, name, avatarId, age, and createdAt.
 * Retains default age = 0 for backward compatibility with documents missing this field.
 */
data class ProfileDto(
    val profileId: String = "",
    val userId: String = "",
    val name: String = "",
    val avatarId: String = "mascot_classic",
    val age: Int = 0,
    val createdAt: Long = 0L
) {
    fun toDomain() = Profile(
        profileId = profileId,
        userId = userId,
        name = name,
        avatarId = avatarId,
        age = age,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(domain: Profile) = ProfileDto(
            profileId = domain.profileId,
            userId = domain.userId,
            name = domain.name,
            avatarId = domain.avatarId,
            age = domain.age,
            createdAt = domain.createdAt
        )
    }
}

typealias ChildProfileDto = ProfileDto
