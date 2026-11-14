package com.aksa.app.domain.model

/**
 * Global configuration for profile management.
 * As per mini-PRD: The maximum number of profiles per parent account is not permanently decided yet.
 * Configurable dynamically without hardcoding policies.
 */
object ProfileConfig {
    /**
     * Max allowed profiles per parent account.
     * null indicates unlimited until a permanent business decision is made.
     */
    var maxProfilesPerAccount: Int? = null
}

/**
 * Child learning profile associated with a parent/guardian account.
 * As per mini-PRD: Contains only nickname and avatar information (no age).
 */
data class Profile(
    val profileId: String,
    val userId: String,
    val name: String,
    val avatarId: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    // Backwards-compatible aliases for existing callers
    val childId: String get() = profileId
    val guardianUid: String get() = userId
    val displayName: String get() = name
    val age: Int get() = 0 // Deprecated / unused as per mini-PRD

    init {
        require(name.isNotBlank()) { "Profile name cannot be blank" }
        require(avatarId.isNotBlank()) { "Profile avatarId cannot be blank" }
    }
}

/**
 * Typealias for backward compatibility with existing codebase references.
 */
typealias ChildProfile = Profile
