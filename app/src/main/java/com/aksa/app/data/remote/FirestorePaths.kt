package com.aksa.app.data.remote

object FirestorePaths {
    const val GUARDIANS = "guardians"
    const val CHILDREN = "children"
    const val SESSIONS = "sessions"
    const val ANSWERS = "answers"
    const val ACHIEVEMENTS = "achievements"

    fun guardianDoc(guardianUid: String) = "$GUARDIANS/$guardianUid"
    fun childrenCollection(guardianUid: String) = "${guardianDoc(guardianUid)}/$CHILDREN"
    fun childDoc(guardianUid: String, childId: String) = "${childrenCollection(guardianUid)}/$childId"
    fun sessionsCollection(guardianUid: String, childId: String) = "${childDoc(guardianUid, childId)}/$SESSIONS"
    fun sessionDoc(guardianUid: String, childId: String, sessionId: String) = "${sessionsCollection(guardianUid, childId)}/$sessionId"
    fun answersCollection(guardianUid: String, childId: String, sessionId: String) = "${sessionDoc(guardianUid, childId, sessionId)}/$ANSWERS"
    fun achievementsCollection(guardianUid: String, childId: String) = "${childDoc(guardianUid, childId)}/$ACHIEVEMENTS"
    fun achievementDoc(guardianUid: String, childId: String, achievementId: String) = "${achievementsCollection(guardianUid, childId)}/$achievementId"
}
