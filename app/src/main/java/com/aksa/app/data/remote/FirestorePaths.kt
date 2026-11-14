package com.aksa.app.data.remote

/**
 * Standard Firestore paths for Aksa multi-profile architecture.
 *
 * Hierarchy:
 * users/{parentUid}/profiles/{profileId}
 * users/{parentUid}/profiles/{profileId}/answerLogs/{logId}
 * users/{parentUid}/profiles/{profileId}/sessionResults/{sessionId}
 * users/{parentUid}/profiles/{profileId}/badges/{badgeId}
 */
object FirestorePaths {
    const val USERS = "users"
    const val PROFILES = "profiles"
    const val ANSWER_LOGS = "answerLogs"
    const val SESSION_RESULTS = "sessionResults"
    const val BADGES = "badges"

    fun userDoc(parentUid: String) = "$USERS/$parentUid"

    fun profilesCollection(parentUid: String) = "${userDoc(parentUid)}/$PROFILES"
    fun profileDoc(parentUid: String, profileId: String) = "${profilesCollection(parentUid)}/$profileId"

    fun answerLogsCollection(parentUid: String, profileId: String) =
        "${profileDoc(parentUid, profileId)}/$ANSWER_LOGS"
    fun answerLogDoc(parentUid: String, profileId: String, logId: String) =
        "${answerLogsCollection(parentUid, profileId)}/$logId"

    fun sessionResultsCollection(parentUid: String, profileId: String) =
        "${profileDoc(parentUid, profileId)}/$SESSION_RESULTS"
    fun sessionResultDoc(parentUid: String, profileId: String, sessionId: String) =
        "${sessionResultsCollection(parentUid, profileId)}/$sessionId"

    fun badgesCollection(parentUid: String, profileId: String) =
        "${profileDoc(parentUid, profileId)}/$BADGES"
    fun badgeDoc(parentUid: String, profileId: String, badgeId: String) =
        "${badgesCollection(parentUid, profileId)}/$badgeId"
}
