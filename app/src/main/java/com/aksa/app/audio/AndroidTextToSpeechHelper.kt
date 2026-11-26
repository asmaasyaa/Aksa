package com.aksa.app.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class AndroidTextToSpeechHelper(context: Context) : AksaAudioSpeaker {

    private val tag = "AksaTTS"
    private var tts: TextToSpeech? = null
    private val _isReady = MutableStateFlow(false)
    override val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    // Menyimpan teks yang dipanggil saat TTS engine belum selesai inisialisasi
    private val pendingUtterances = mutableListOf<Pair<String, Int>>()
    private var utteranceCounter = 0L

    init {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val indonesianLocale = Locale.Builder()
                        .setLanguage("id")
                        .setRegion("ID")
                        .build()
                    val result = tts?.setLanguage(indonesianLocale)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.w(tag, "Indonesian TTS language not supported, falling back to default")
                        tts?.setLanguage(Locale.getDefault())
                    }
                    tts?.setSpeechRate(0.85f)
                    tts?.setPitch(1.05f)
                    _isReady.value = true

                    // Putar semua teks yang sempat tertunda saat engine sedang loading
                    synchronized(pendingUtterances) {
                        for ((text, queueMode) in pendingUtterances) {
                            dispatchSpeak(text, queueMode)
                        }
                        pendingUtterances.clear()
                    }
                } else {
                    Log.e(tag, "TextToSpeech initialization failed with status: $status")
                    _isReady.value = false
                    synchronized(pendingUtterances) {
                        pendingUtterances.clear()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to construct TextToSpeech", e)
            _isReady.value = false
        }
    }

    override fun speak(text: String) {
        val cleanText = sanitizeForSpeech(text)
        if (cleanText.isBlank()) return

        if (!_isReady.value) {
            synchronized(pendingUtterances) {
                // Ganti antrean lama karena mode QUEUE_FLUSH menimpa ucapan sebelumnya
                pendingUtterances.clear()
                pendingUtterances.add(cleanText to TextToSpeech.QUEUE_FLUSH)
            }
            return
        }

        dispatchSpeak(cleanText, TextToSpeech.QUEUE_FLUSH)
    }

    override fun speakQueued(text: String) {
        val cleanText = sanitizeForSpeech(text)
        if (cleanText.isBlank()) return

        if (!_isReady.value) {
            synchronized(pendingUtterances) {
                pendingUtterances.add(cleanText to TextToSpeech.QUEUE_ADD)
            }
            return
        }

        dispatchSpeak(cleanText, TextToSpeech.QUEUE_ADD)
    }

    private fun dispatchSpeak(cleanText: String, queueMode: Int) {
        try {
            utteranceCounter++
            val utteranceId = "aksa_utterance_${System.currentTimeMillis()}_$utteranceCounter"
            // Gunakan QUEUE_FLUSH langsung tanpa tts?.stop() agar tidak membatalkan utterance baru
            tts?.speak(cleanText, queueMode, null, utteranceId)
        } catch (e: Exception) {
            Log.e(tag, "Error during TTS dispatchSpeak()", e)
        }
    }

    override fun stop() {
        synchronized(pendingUtterances) {
            pendingUtterances.clear()
        }
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e(tag, "Error during TTS stop()", e)
        }
    }

    override fun release() {
        synchronized(pendingUtterances) {
            pendingUtterances.clear()
        }
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            _isReady.value = false
        } catch (e: Exception) {
            Log.e(tag, "Error during TTS release()", e)
        }
    }

    companion object {
        fun sanitizeForSpeech(rawText: String): String {
            return rawText
                .replace(Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+"), "")
                .replace(Regex("[\\u2600-\\u27BF]+"), "")
                .replace("✓", "")
                .replace("✗", "")
                .replace("★", "")
                .replace("☆", "")
                .replace(Regex("\\s+"), " ")
                .trim()
        }
    }
}