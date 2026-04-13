package com.chirag.mello.ui.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.*

data class SpeechState(
    val isListening: Boolean,
    val startListening: () -> Unit,
    val stopListening: () -> Unit
)

@Composable
fun rememberSpeechRecognizer(
    context: Context,
    onResult: (String) -> Unit,
    onError: () -> Unit = {}
): SpeechState {
    var isListening by remember { mutableStateOf(false) }
    val recognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }

    val intent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
    }

    DisposableEffect(Unit) {
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onError(error: Int) {
                isListening = false
                onError()
            }
            override fun onResults(results: Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) onResult(matches[0])
                isListening = false
            }
        })
        onDispose { recognizer.destroy() }
    }

    return SpeechState(
        isListening = isListening,
        startListening = { recognizer.startListening(intent); isListening = true },
        stopListening  = { recognizer.stopListening(); isListening = false }
    )
}
