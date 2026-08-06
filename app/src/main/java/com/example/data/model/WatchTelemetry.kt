package com.example.data.model

import androidx.compose.runtime.Immutable

enum class BleConnectionState {
    DISCONNECTED,
    SCANNING,
    PAIRED,
    SYNCING,
    ERROR
}

/**
 * Live hardware telemetry coming from the ESP32 smartwatch over BLE JSON.
 * Example payload: {"batt": 85, "volt": 3.92, "gx": 1.2, "gy": -0.5, "gz": 9.8, "btn": 0, "steps": 4200}
 */
@Immutable
data class WatchTelemetry(
    val connectionState: BleConnectionState = BleConnectionState.DISCONNECTED,
    val batteryPercent: Int = 88,
    val batteryVoltage: Float = 3.92f,
    val isCharging: Boolean = false, // TP4056 status
    val gyroX: Float = 0f,
    val gyroY: Float = 0f,
    val gyroZ: Float = 9.8f,
    val accelX: Float = 0.1f,
    val accelY: Float = 0.2f,
    val accelZ: Float = 9.8f,
    val buttonPressed: Boolean = false,
    val steps: Int = 4250,
    val calories: Int = 185,
    val activeMinutes: Int = 38,
    val deviceName: String = "ESP32-CyberWatch-BLE",
    val rssi: Int = -58,
    val isMockMode: Boolean = true,
    val rawJsonIncoming: String = "{\"batt\": 88, \"gx\": 0.0, \"gy\": 0.0, \"gz\": 9.8, \"btn\": 0, \"steps\": 4250}",
    val rawJsonOutgoing: String = "{\"cmd\": \"PING\"}",
    val isFallDetected: Boolean = false
)

data class DiscoveredDevice(
    val name: String,
    val address: String,
    val rssi: Int
)
