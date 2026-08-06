package com.example.data.voice

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class EmergencySirenManager {
    private var toneGenerator: ToneGenerator? = null
    private var sirenJob: Job? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        } catch (e: Exception) {
            Log.e("EmergencySiren", "ToneGenerator init failed", e)
        }
    }

    fun startSiren(scope: CoroutineScope) {
        stopSiren()
        sirenJob = scope.launch(Dispatchers.Default) {
            val generator = toneGenerator
            while (isActive) {
                try {
                    generator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 250)
                    delay(300)
                    if (!isActive) break
                    generator?.startTone(ToneGenerator.TONE_SUP_ERROR, 250)
                    delay(300)
                } catch (e: Exception) {
                    Log.e("EmergencySiren", "Error playing siren tone", e)
                }
            }
        }
    }

    fun stopSiren() {
        sirenJob?.cancel()
        sirenJob = null
        try {
            toneGenerator?.stopTone()
        } catch (_: Exception) {}
    }
}
