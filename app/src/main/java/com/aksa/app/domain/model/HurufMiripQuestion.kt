package com.aksa.app.domain.model

/**
 * Question model for Huruf Mirip (b/d and p/q visual comparison activity).
 */
data class HurufMiripQuestion(
    val id: String,
    val targetLetter: String,
    val pairLetter: String,
    val choices: List<String>,
    val promptText: String,
    val audioPrompt: String,
    val hintTitle: String,
    val leftHint: Pair<String, String>,
    val rightHint: Pair<String, String>
)
