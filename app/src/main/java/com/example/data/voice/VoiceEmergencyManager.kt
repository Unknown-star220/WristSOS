package com.example.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class VoiceEmergencyManager(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onHelpDetected: () -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _lastRecognizedText = MutableStateFlow("")
    val lastRecognizedText: StateFlow<String> = _lastRecognizedText.asStateFlow()

    private val _isVoiceAvailable = MutableStateFlow(SpeechRecognizer.isRecognitionAvailable(context))
    val isVoiceAvailable: StateFlow<Boolean> = _isVoiceAvailable.asStateFlow()

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _isListening.value = false
            return
        }

        scope.launch(Dispatchers.Main) {
            try {
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                }

                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}

                    override fun onError(error: Int) {
                        Log.w("VoiceEmergency", "Speech error code: $error")
                        // If continuous listening fails or times out, re-listen if enabled
                        if (_isListening.value) {
                            scope.launch(Dispatchers.Main) {
                                kotlinx.coroutines.delay(1000)
                                if (_isListening.value) {
                                    restartListeningInternal()
                                }
                            }
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val text = matches[0]
                            _lastRecognizedText.value = text
                            checkSpeechMatches(text)
                        }
                        if (_isListening.value) {
                            restartListeningInternal()
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val text = matches[0]
                            _lastRecognizedText.value = text
                            checkSpeechMatches(text)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                restartListeningInternal()
            } catch (e: Exception) {
                Log.e("VoiceEmergency", "Failed to init SpeechRecognizer", e)
                _isListening.value = false
            }
        }
    }

    private fun restartListeningInternal() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            Log.e("VoiceEmergency", "Error starting listening", e)
        }
    }

    fun stopListening() {
        _isListening.value = false
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e("VoiceEmergency", "Error stopping speech recognizer", e)
        }
    }

    fun simulateVoicePhrase(phrase: String) {
        _lastRecognizedText.value = phrase
        checkSpeechMatches(phrase)
    }

    private fun checkSpeechMatches(text: String) {
        val normalized = text.lowercase(Locale.ROOT)
        if (normalized.contains("help help") || 
            normalized.contains("help") || 
            normalized.contains("sos") || 
            normalized.contains("emergency")) {
            Log.i("VoiceEmergency", "Trigger 'HELP HELP' recognized: $text")
            onHelpDetected()
        }
    }
}
