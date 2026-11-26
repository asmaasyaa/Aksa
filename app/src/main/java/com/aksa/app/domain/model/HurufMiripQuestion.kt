package com.aksa.app.domain.model

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
