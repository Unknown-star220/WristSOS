package com.example.data.model

enum class OledElementType {
    TIME_DIGITAL,
    DATE_TEXT,
    BATTERY_BAR,
    STEP_COUNTER,
    CALORIE_COUNTER,
    HEART_GLYPH,
    CUSTOM_TEXT,
    HARDWARE_LOGO
}

data class OledElement(
    val id: String,
    val type: OledElementType,
    val label: String,
    val x: Int, // 0 to 128
    val y: Int, // 0 to 64
    val fontSize: Int = 12, // e.g. 10, 14, 20
    val text: String = "",
    val isSelected: Boolean = false
)

data class WatchFacePreset(
    val id: String,
    val name: String,
    val elements: List<OledElement>
)
