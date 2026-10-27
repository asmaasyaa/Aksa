package com.aksa.app.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Text-to-Speech manager for Indonesian audio instruction replay in learning activities.
 * Handles initialization failure gracefully, prevents audio overlap, and safely releases resources.
 */
class AndroidTextToSpeechHelper(
    context: Context
) : AksaAudioSpeaker, TextToSpeech.OnInitListener {

    private val tag = "AksaTTS"
    private var tts: TextToSpeech? = null

    private val _isReady = MutableStateFlow(false)
    override val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(tag, "Failed to instantiate TextToSpeech", e)
            _isReady.value = false
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val ttsInstance = tts
            if (ttsInstance == null) {
                _isReady.value = false
                return
            }

            // Attempt Indonesian locale
            val indonesianLocale = try {
                Locale.Builder().setLanguage("id").setRegion("ID").build()
            } catch (e: Exception) {
                Locale("id", "ID")
            }
            val langResult = ttsInstance.isLanguageAvailable(indonesianLocale)

            if (langResult == TextToSpeech.LANG_AVAILABLE ||
                langResult == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
                langResult == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
            ) {
                ttsInstance.language = indonesianLocale
            } else {
                Log.w(tag, "Indonesian TTS language not available, using default locale")
                ttsInstance.language = Locale.getDefault()
            }

            ttsInstance.setSpeechRate(0.85f) // Slightly slower for child clarity
            ttsInstance.setPitch(1.05f)     // Friendly, clear pitch
            _isReady.value = true
        } else {
            Log.w(tag, "TTS initialization failed with status: $status")
            _isReady.value = false
        }
    }

    override fun speak(text: String) {
        if (!_isReady.value) {
            Log.w(tag, "speak() called but TTS is not ready: '$text'")
            return
        }

        try {
            // Stop prior speech to avoid speaking over itself
            tts?.stop()
            val utteranceId = "aksa_utterance_${System.currentTimeMillis()}"
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e(tag, "Error during TTS speak()", e)
        }
    }

    override fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e(tag, "Error stopping TTS", e)
        }
    }

    override fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            _isReady.value = false
        } catch (e: Exception) {
            Log.e(tag, "Error releasing TTS", e)
        }
    }
}
