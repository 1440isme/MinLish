package com.minlish.core.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

sealed interface SpeechResult {
    data object Success : SpeechResult
    data object Loading : SpeechResult
    data class Error(val message: String) : SpeechResult
}

class TextToSpeechManager(
    context: Context,
) : TextToSpeech.OnInitListener {
    companion object {
        private const val DEFAULT_TTS_UTTERANCE_ID = "minlish_tts"
    }

    private sealed interface InitializationState {
        data object Initializing : InitializationState
        data object Ready : InitializationState
        data class Failed(val message: String) : InitializationState
    }

    private var initializationState: InitializationState = InitializationState.Initializing
    private var pendingSpeechText: String? = null
    private var textToSpeech: TextToSpeech? = TextToSpeech(context.applicationContext, this)

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            initializationState =
                InitializationState.Failed("Text to speech engine failed to initialize.")
            return
        }

        val configured = configureLanguage(Locale.US) || configureLanguage(Locale.ENGLISH)
        if (!configured) {
            initializationState =
                InitializationState.Failed("Text to speech language is not available on this device.")
            return
        }

        initializationState = InitializationState.Ready
        pendingSpeechText?.let { queuedText ->
            pendingSpeechText = null
            speak(queuedText)
        }
    }

    fun speak(text: String): SpeechResult {
        val trimmedText = text.trim()
        if (trimmedText.isEmpty()) {
            return SpeechResult.Success
        }

        return when (val state = initializationState) {
            InitializationState.Initializing -> {
                pendingSpeechText = trimmedText
                SpeechResult.Loading
            }

            is InitializationState.Failed -> SpeechResult.Error(state.message)

            InitializationState.Ready -> {
                val result = textToSpeech?.speak(
                    trimmedText,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    DEFAULT_TTS_UTTERANCE_ID,
                ) ?: TextToSpeech.ERROR

                if (result == TextToSpeech.ERROR) {
                    SpeechResult.Error("Failed to play text to speech audio.")
                } else {
                    SpeechResult.Success
                }
            }
        }
    }

    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }

    private fun configureLanguage(locale: Locale): Boolean {
        val result = textToSpeech?.setLanguage(locale) ?: TextToSpeech.ERROR
        return result != TextToSpeech.LANG_MISSING_DATA &&
            result != TextToSpeech.LANG_NOT_SUPPORTED
    }
}
