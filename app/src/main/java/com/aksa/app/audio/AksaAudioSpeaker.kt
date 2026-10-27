package com.aksa.app.audio

import kotlinx.coroutines.flow.StateFlow

interface AksaAudioSpeaker {
    val isReady: StateFlow<Boolean>
    fun speak(text: String)
    fun stop()
    fun release()
}
