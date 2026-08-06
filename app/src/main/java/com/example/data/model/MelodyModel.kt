package com.example.data.model

/**
 * 8-bit chip-tune note representation for ESP32 tone generator.
 * Note frequencies: C4 (262Hz), D4 (294Hz), E4 (330Hz), F4 (349Hz), G4 (392Hz), A4 (440Hz), B4 (494Hz), C5 (523Hz)
 */
data class NoteStep(
    val stepIndex: Int, // 0..15
    val noteName: String = "REST", // C4, D4, E4, F4, G4, A4, B4, C5, REST
    val frequencyHz: Int = 0,
    val durationMs: Int = 200
)

data class AlarmModel(
    val id: Int = 0,
    val hour: Int = 7,
    val minute: Int = 30,
    val label: String = "Cyber Wake",
    val isEnabled: Boolean = true,
    val melodyName: String = "Cyber Retro Synth",
    val repeatDays: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
)

data class NotificationAppFilter(
    val packageName: String,
    val appName: String,
    val iconCategory: String,
    val isMuted: Boolean = false
)
