package com.aksa.app.data.model

import com.aksa.app.domain.model.Profile

/**
 * Firestore DTO for Profile.
 * Stores only profileId, userId, name, avatarId, and createdAt.
 * Explicitly excludes age and other unneeded fields as specified in mini-PRD.
 */
data class ProfileDto(
    val profileId: String = "",
    val userId: String = "",
    val name: String = "",
    val avatarId: String = "mascot_classic",
    val createdAt: Long = 0L
) {
    fun toDomain() = Profile(
        profileId = profileId,
        userId = userId,
        name = name,
        avatarId = avatarId,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(domain: Profile) = ProfileDto(
            profileId = domain.profileId,
            userId = domain.userId,
            name = domain.name,
            avatarId = domain.avatarId,
            createdAt = domain.createdAt
        )
    }
}

typealias ChildProfileDto = ProfileDto
