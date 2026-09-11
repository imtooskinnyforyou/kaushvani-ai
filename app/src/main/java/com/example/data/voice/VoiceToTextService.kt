package com.example.data.voice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Lifecycle state representation for SpeechRecognizer voice-to-text recording.
 */
sealed class SpeechState {
    object Idle : SpeechState()
    object Preparing : SpeechState()
    object Listening : SpeechState()
    object Recording : SpeechState()
    object Processing : SpeechState()
    data class Success(val transcript: String, val confidence: Float = 0.95f) : SpeechState()
    data class Error(val message: String, val errorCode: Int = -1) : SpeechState()
}

/**
 * Robust on-device Voice-to-Text service utilizing Android's device microphone and SpeechRecognizer.
 * Supports regional Indian languages (Hindi, Marathi, Bengali, Gujarati, Tamil, Telugu, English, etc.),
 * live decibel (RMS) level monitoring for UI waveforms, partial results streaming, and error handling.
 */
class VoiceToTextService(private val context: Context) {

    companion object {
        private const val TAG = "VoiceToTextService"
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    private val _speechState = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    private val _livePartialText = MutableStateFlow("")
    val livePartialText: StateFlow<String> = _livePartialText.asStateFlow()

    private val _liveRmsDb = MutableStateFlow(0f)
    val liveRmsDb: StateFlow<Float> = _liveRmsDb.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _lastTranscript = MutableStateFlow("")
    val lastTranscript: StateFlow<String> = _lastTranscript.asStateFlow()

    /**
     * Checks if speech recognition service is available on this Android device.
     */
    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    /**
     * Checks if audio recording permission is granted.
     */
    fun hasMicrophonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Starts listening to the device microphone in the specified locale.
     * Guaranteed to execute on the Android Main Looper.
     */
    fun startListening(
        localeTag: String = "hi-IN",
        prompt: String = "अपने शिल्प का विवरण बोलें (Describe your craft)..."
    ) {
        mainHandler.post {
            if (!hasMicrophonePermission()) {
                val errorMsg = "माइक्रोफ़ोन अनुमति आवश्यक है (Microphone permission required)"
                _speechState.value = SpeechState.Error(errorMsg, SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS)
                return@post
            }

            if (!isRecognitionAvailable()) {
                val errorMsg = "डिवाइस पर स्पीच पहचान सेवा उपलब्ध नहीं है (Speech recognition unavailable)"
                Log.w(TAG, errorMsg)
                _speechState.value = SpeechState.Error(errorMsg)
                return@post
            }

            try {
                // Safely clean up previous recognizer session
                destroyRecognizerInternal()

                _livePartialText.value = ""
                _liveRmsDb.value = 0f
                _speechState.value = SpeechState.Preparing
                _isListening.value = true

                val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer = recognizer

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeTag)
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, localeTag)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                }

                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Log.d(TAG, "onReadyForSpeech")
                        _speechState.value = SpeechState.Listening
                    }

                    override fun onBeginningOfSpeech() {
                        Log.d(TAG, "onBeginningOfSpeech")
                        _speechState.value = SpeechState.Recording
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        _liveRmsDb.value = rmsdB.coerceIn(0f, 10f)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        Log.d(TAG, "onEndOfSpeech")
                        _speechState.value = SpeechState.Processing
                        _isListening.value = false
                    }

                    override fun onError(error: Int) {
                        val errorMessage = getErrorMessage(error)
                        Log.e(TAG, "SpeechRecognizer error ($error): $errorMessage")
                        _isListening.value = false
                        _liveRmsDb.value = 0f
                        _speechState.value = SpeechState.Error(errorMessage, error)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val confScores = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                        val transcript = matches?.firstOrNull()?.trim().orEmpty()
                        val confidence = confScores?.firstOrNull() ?: 0.95f

                        Log.d(TAG, "Speech recognition result: '$transcript', confidence: $confidence")
                        _isListening.value = false
                        _liveRmsDb.value = 0f

                        if (transcript.isNotBlank()) {
                            _lastTranscript.value = transcript
                            _speechState.value = SpeechState.Success(transcript, confidence)
                        } else {
                            _speechState.value = SpeechState.Idle
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull()?.trim().orEmpty()
                        if (partial.isNotBlank()) {
                            _livePartialText.value = partial
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                recognizer.startListening(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start speech recognition: ${e.message}", e)
                _isListening.value = false
                _speechState.value = SpeechState.Error("रिकॉर्डिंग शुरू करने में त्रुटि: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Gracefully stops recording and prompts the engine to finalize the transcript.
     */
    fun stopListening() {
        mainHandler.post {
            try {
                _speechState.value = SpeechState.Processing
                _isListening.value = false
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop listening: ${e.message}", e)
            }
        }
    }

    /**
     * Cancels active recognition without processing.
     */
    fun cancel() {
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
                _isListening.value = false
                _liveRmsDb.value = 0f
                _speechState.value = SpeechState.Idle
            } catch (e: Exception) {
                Log.e(TAG, "Failed to cancel listening: ${e.message}", e)
            }
        }
    }

    /**
     * Destroys recognizer resources.
     */
    fun destroy() {
        mainHandler.post {
            destroyRecognizerInternal()
            _isListening.value = false
            _liveRmsDb.value = 0f
            _speechState.value = SpeechState.Idle
        }
    }

    private fun destroyRecognizerInternal() {
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.w(TAG, "Recognizer destruction ignored: ${e.message}")
        } finally {
            speechRecognizer = null
        }
    }

    private fun getErrorMessage(error: Int): String {
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "ऑडियो रिकॉर्डिंग में त्रुटि (Audio recording error)"
            SpeechRecognizer.ERROR_CLIENT -> "क्लाइंट त्रुटि (Client error, please retry)"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "माइक्रोफ़ोन अनुमति नहीं मिली (Microphone permission not granted)"
            SpeechRecognizer.ERROR_NETWORK -> "इंटरनेट कनेक्शन धीमा है (Network connection error)"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "नेटवर्क टाइमआउट (Network timeout)"
            SpeechRecognizer.ERROR_NO_MATCH -> "आवाज़ स्पष्ट नहीं सुनाई दी, कृपया दोबारा बोलें (No match, please speak again)"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "सिस्टम व्यस्त है, कृपया एक पल प्रतीक्षा करें (Recognizer busy)"
            SpeechRecognizer.ERROR_SERVER -> "सर्वर त्रुटि (Server error)"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "कोई आवाज़ नहीं मिली (No speech detected)"
            else -> "रिकॉर्डिंग त्रुटि #$error (Recording error)"
        }
    }
}
