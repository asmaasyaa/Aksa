package com.aksa.app.data.model

import com.aksa.app.domain.model.ChildProfile

data class ChildProfileDto(
    val childId: String = "",
    val guardianUid: String = "",
    val displayName: String = "",
    val age: Int = 8,
    val avatarId: String = "mascot_classic",
    val createdAt: Long = 0L
) {
    fun toDomain() = ChildProfile(
        childId = childId,
        guardianUid = guardianUid,
        displayName = displayName,
        age = age,
        avatarId = avatarId,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(domain: ChildProfile) = ChildProfileDto(
            childId = domain.childId,
            guardianUid = domain.guardianUid,
            displayName = domain.displayName,
            age = domain.age,
            avatarId = domain.avatarId,
            createdAt = domain.createdAt
        )
    }
}
