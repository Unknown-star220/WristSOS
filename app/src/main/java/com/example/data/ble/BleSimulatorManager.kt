package com.example.data.ble

import com.example.data.model.BleConnectionState
import com.example.data.model.DiscoveredDevice
import com.example.data.model.WatchTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Manages Bluetooth LE connection and high-frequency ESP32 telemetry JSON data stream.
 * Includes a full Mock Data Simulator for testing without physical hardware.
 */
class BleSimulatorManager(
    private val scope: CoroutineScope
) {
    private val _telemetry = MutableStateFlow(WatchTelemetry())
    val telemetry: StateFlow<WatchTelemetry> = _telemetry.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices.asStateFlow()

    private var simulatorJob: Job? = null
    private var scanJob: Job? = null
    private var simTimeSeconds = 0f
    private var walkingSimEnabled = true

    init {
        startMockSimulator()
    }

    fun startScan() {
        scope.launch {
            _telemetry.update { it.copy(connectionState = BleConnectionState.SCANNING) }
            _discoveredDevices.value = emptyList()
            delay(1000)
            val mockList = listOf(
                DiscoveredDevice("ESP32-CyberWatch-BLE", "24:6F:28:B1:00:1E", -48),
                DiscoveredDevice("ESP32-Smartwatch-MPU6050", "30:AE:A4:CC:11:82", -65),
                DiscoveredDevice("DIY-OLED-Watch-Node", "AC:67:B2:77:43:9A", -78)
            )
            _discoveredDevices.value = mockList
        }
    }

    fun connectDevice(device: DiscoveredDevice) {
        scope.launch {
            _telemetry.update { it.copy(connectionState = BleConnectionState.SYNCING, deviceName = device.name) }
            delay(1200)
            _telemetry.update { it.copy(connectionState = BleConnectionState.PAIRED) }
        }
    }

    fun disconnect() {
        _telemetry.update { it.copy(connectionState = BleConnectionState.DISCONNECTED) }
    }

    fun toggleMockMode(enabled: Boolean) {
        _telemetry.update { it.copy(isMockMode = enabled) }
        if (enabled && simulatorJob?.isActive != true) {
            startMockSimulator()
        }
    }

    fun toggleCharging(charging: Boolean) {
        _telemetry.update {
            it.copy(
                isCharging = charging,
                batteryPercent = if (charging) (it.batteryPercent + 5).coerceAtMost(100) else it.batteryPercent
            )
        }
    }

    fun toggleWalkingSim(walking: Boolean) {
        walkingSimEnabled = walking
    }

    fun simulateButtonPress() {
        scope.launch {
            _telemetry.update {
                val updatedJson = "{\"batt\": ${it.batteryPercent}, \"gx\": ${String.format("%.1f", it.gyroX)}, \"btn\": 1, \"steps\": ${it.steps}}"
                it.copy(buttonPressed = true, rawJsonIncoming = updatedJson)
            }
            delay(500)
            _telemetry.update { it.copy(buttonPressed = false) }
        }
    }

    fun triggerFallSimulation() {
        scope.launch {
            _telemetry.update {
                it.copy(
                    accelX = 9.2f,
                    accelY = 8.5f,
                    accelZ = 12.0f,
                    isFallDetected = true,
                    rawJsonIncoming = "{\"batt\": ${it.batteryPercent}, \"gx\": 9.2, \"gz\": 12.0, \"fall\": 1}"
                )
            }
            delay(4000)
            _telemetry.update { it.copy(isFallDetected = false) }
        }
    }

    fun sendCommandJson(cmdJson: String) {
        _telemetry.update { it.copy(rawJsonOutgoing = cmdJson) }
    }

    fun resetFallAlert() {
        _telemetry.update { it.copy(isFallDetected = false) }
    }

    private fun startMockSimulator() {
        simulatorJob?.cancel()
        simulatorJob = scope.launch(Dispatchers.Default) {
            while (true) {
                delay(100) // ~10 FPS telemetry loop for smooth visual updates without main thread lag
                simTimeSeconds += 0.1f

                val current = _telemetry.value
                if (!current.isMockMode) continue

                // Gyro & Accel sine wave physics
                val gx = sin(simTimeSeconds * 1.5f) * 25f
                val gy = cos(simTimeSeconds * 1.2f) * 30f
                val gz = 9.8f + sin(simTimeSeconds * 0.8f) * 1.5f

                val ax = sin(simTimeSeconds * 2.0f) * 1.8f
                val ay = cos(simTimeSeconds * 2.0f) * 1.8f
                val az = 9.81f

                // Step calculation from accelerometer movement spikes
                var steps = current.steps
                if (walkingSimEnabled && (simTimeSeconds.toInt() % 2 == 0)) {
                    steps += (1..3).random()
                }

                val calories = (steps * 0.043f).toInt()
                val activeMins = (steps / 100).coerceAtLeast(1)
                val volt = 3.3f + (current.batteryPercent / 100f) * 0.9f // 3.3V to 4.2V scale for 3.7V LiPo

                val jsonStr = "{\"batt\": ${current.batteryPercent}, \"volt\": ${String.format("%.2f", volt)}, " +
                        "\"gx\": ${String.format("%.1f", gx)}, \"gy\": ${String.format("%.1f", gy)}, " +
                        "\"btn\": ${if (current.buttonPressed) 1 else 0}, \"steps\": $steps}"

                _telemetry.update {
                    it.copy(
                        gyroX = gx,
                        gyroY = gy,
                        gyroZ = gz,
                        accelX = ax,
                        accelY = ay,
                        accelZ = az,
                        batteryVoltage = volt,
                        steps = steps,
                        calories = calories,
                        activeMinutes = activeMins,
                        rawJsonIncoming = jsonStr
                    )
                }
            }
        }
    }
}
