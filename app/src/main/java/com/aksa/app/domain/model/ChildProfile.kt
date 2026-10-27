package com.aksa.app.domain.model

/**
 * Learning profile for a child, associated with a guardian account.
 * Age must be between 6 and 12 years.
 */
data class ChildProfile(
    val childId: String,
    val guardianUid: String,
    val displayName: String,
    val age: Int,
    val avatarId: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    init {
        require(displayName.isNotBlank()) { "Child display name cannot be blank" }
        require(age in 6..12) { "Child age must be between 6 and 12 years (got $age)" }
    }
}
