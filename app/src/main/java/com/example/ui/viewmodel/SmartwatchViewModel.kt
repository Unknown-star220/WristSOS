package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ble.BleSimulatorManager
import com.example.data.local.WatchDatabase
import com.example.data.model.AlarmModel
import com.example.data.model.BadgeModel
import com.example.data.model.DailyStats
import com.example.data.model.DiscoveredDevice
import com.example.data.model.NoteStep
import com.example.data.model.NotificationAppFilter
import com.example.data.model.OledElement
import com.example.data.model.OledElementType
import com.example.data.model.WatchFacePreset
import com.example.data.model.WatchTelemetry
import com.example.data.repository.SmartwatchRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.data.model.EmergencyContact
import com.example.data.voice.EmergencySirenManager
import com.example.data.voice.VoiceEmergencyManager
import kotlinx.coroutines.Job

class SmartwatchViewModel(application: Application) : AndroidViewModel(application) {

    private val db = WatchDatabase.getDatabase(application)
    private val repository = SmartwatchRepository(db.watchDao())
    val bleManager = BleSimulatorManager(viewModelScope)
    val sirenManager = EmergencySirenManager()

    var voiceManager: VoiceEmergencyManager? = null

    val telemetry: StateFlow<WatchTelemetry> = bleManager.telemetry
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = bleManager.discoveredDevices

    val dailyStatsList: StateFlow<List<DailyStats>> = repository.allDailyStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alarms: StateFlow<List<AlarmModel>> = repository.allAlarms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val badges: StateFlow<List<BadgeModel>> = repository.allBadges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appFilters: StateFlow<List<NotificationAppFilter>> = repository.allAppFilters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Emergency Voice Alert States
    private val _isEmergencyActive = MutableStateFlow(false)
    val isEmergencyActive: StateFlow<Boolean> = _isEmergencyActive.asStateFlow()

    private val _emergencyCountdown = MutableStateFlow(5)
    val emergencyCountdown: StateFlow<Int> = _emergencyCountdown.asStateFlow()

    private val _isEmergencyDispatched = MutableStateFlow(false)
    val isEmergencyDispatched: StateFlow<Boolean> = _isEmergencyDispatched.asStateFlow()

    private val _lastRecognizedSpeech = MutableStateFlow("Listening for 'HELP HELP'...")
    val lastRecognizedSpeech: StateFlow<String> = _lastRecognizedSpeech.asStateFlow()

    private val _isVoiceListening = MutableStateFlow(true)
    val isVoiceListening: StateFlow<Boolean> = _isVoiceListening.asStateFlow()

    private val _emergencyLog = MutableStateFlow<List<String>>(
        listOf("System initialized. Monitoring for 'HELP HELP' voice trigger.")
    )
    val emergencyLog: StateFlow<List<String>> = _emergencyLog.asStateFlow()

    // Emergency Contact Mobile Numbers State
    private val _emergencyContacts = MutableStateFlow<List<EmergencyContact>>(
        listOf(
            EmergencyContact(
                name = "Mom / Primary Contact",
                phoneNumber = "+1 (555) 911-0123",
                relationship = "Family / Primary",
                isPrimary = true,
                isEnabled = true
            ),
            EmergencyContact(
                name = "Emergency Services (911)",
                phoneNumber = "911",
                relationship = "Emergency Dispatch",
                isPrimary = false,
                isEnabled = true
            ),
            EmergencyContact(
                name = "Dr. Sarah Miller",
                phoneNumber = "+1 (555) 345-6789",
                relationship = "Family Physician",
                isPrimary = false,
                isEnabled = true
            )
        )
    )
    val emergencyContacts: StateFlow<List<EmergencyContact>> = _emergencyContacts.asStateFlow()

    private var countdownJob: Job? = null

    // Feature 5: OLED Watch Face Designer Elements
    private val _watchFaceElements = MutableStateFlow<List<OledElement>>(
        listOf(
            OledElement("e1", OledElementType.TIME_DIGITAL, "Digital Clock", x = 18, y = 10, fontSize = 20, text = "12:45"),
            OledElement("e2", OledElementType.DATE_TEXT, "Date Text", x = 28, y = 35, fontSize = 10, text = "THU 06 AUG"),
            OledElement("e3", OledElementType.BATTERY_BAR, "Battery Bar", x = 5, y = 50, fontSize = 10, text = "88% BATT"),
            OledElement("e4", OledElementType.STEP_COUNTER, "Steps Counter", x = 75, y = 50, fontSize = 10, text = "4.2K STEPS")
        )
    )
    val watchFaceElements: StateFlow<List<OledElement>> = _watchFaceElements.asStateFlow()

    // Feature 6: Buzzer Melody Studio Notes
    private val defaultNotes = listOf("C4", "E4", "G4", "C5", "G4", "E4", "C4", "REST", "D4", "F4", "A4", "D5", "A4", "F4", "D4", "REST")
    private val _melodyGrid = MutableStateFlow(
        defaultNotes.mapIndexed { index, note ->
            NoteStep(stepIndex = index, noteName = note, frequencyHz = getFreqForNote(note), durationMs = 200)
        }
    )
    val melodyGrid: StateFlow<List<NoteStep>> = _melodyGrid.asStateFlow()

    // Feature 7: Gesture Controls & Safety Settings
    val raiseToWakeSensitivity = MutableStateFlow(75f) // 0 to 100%
    val pushButtonAction = MutableStateFlow("TAKE_PHOTO") // TAKE_PHOTO, FIND_PHONE, TOGGLE_MUTE, SOS_CALL

    // OLED Transmission State
    private val _oledPushProgress = MutableStateFlow<Float?>(null)
    val oledPushProgress: StateFlow<Float?> = _oledPushProgress.asStateFlow()

    // Status Message / Toast feedback
    private val _userFeedback = MutableStateFlow<String?>(null)
    val userFeedback: StateFlow<String?> = _userFeedback.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedDefaultsIfEmpty()
        }
        viewModelScope.launch {
            telemetry.collect { tele ->
                repository.checkAndUnlockBadges(tele.steps)
            }
        }
    }

    fun clearFeedback() {
        _userFeedback.value = null
    }

    // BLE Actions
    fun startScan() = bleManager.startScan()
    fun connectDevice(device: DiscoveredDevice) = bleManager.connectDevice(device)
    fun disconnectBle() = bleManager.disconnect()
    fun toggleMockSimulator(enabled: Boolean) = bleManager.toggleMockMode(enabled)
    fun toggleChargingSim(charging: Boolean) = bleManager.toggleCharging(charging)
    fun simulateButtonPress() = bleManager.simulateButtonPress()
    fun triggerFallSimulation() = bleManager.triggerFallSimulation()
    fun resetFallAlert() = bleManager.resetFallAlert()

    // Feature 4: Smartphone Notifications
    fun sendNotificationPayload(appName: String, messageText: String) {
        val payloadJson = "{\"cmd\": \"SCREEN\", \"app\": \"$appName\", \"txt\": \"$messageText\"}"
        bleManager.sendCommandJson(payloadJson)
        _userFeedback.value = "Pushed payload to OLED: $messageText"
    }

    fun toggleAppFilterMute(packageName: String, isMuted: Boolean) {
        viewModelScope.launch {
            repository.updateAppFilterMute(packageName, isMuted)
        }
    }

    // Feature 5: OLED Watch Face Designer Actions
    fun updateOledElementPosition(id: String, newX: Int, newY: Int) {
        _watchFaceElements.value = _watchFaceElements.value.map {
            if (it.id == id) it.copy(x = newX.coerceIn(0, 115), y = newY.coerceIn(0, 52)) else it
        }
    }

    fun addOledElement(type: OledElementType) {
        val id = "elem_${System.currentTimeMillis()}"
        val newElem = when (type) {
            OledElementType.TIME_DIGITAL -> OledElement(id, type, "Clock", 20, 15, 18, "12:00")
            OledElementType.DATE_TEXT -> OledElement(id, type, "Date", 30, 35, 10, "MON 01 JAN")
            OledElementType.BATTERY_BAR -> OledElement(id, type, "Battery", 5, 52, 10, "100% BAT")
            OledElementType.STEP_COUNTER -> OledElement(id, type, "Steps", 70, 52, 10, "0 STEPS")
            OledElementType.CALORIE_COUNTER -> OledElement(id, type, "Calories", 10, 40, 10, "0 KCAL")
            OledElementType.HEART_GLYPH -> OledElement(id, type, "Heart Glyph", 110, 10, 10, "♥ 72")
            OledElementType.CUSTOM_TEXT -> OledElement(id, type, "Custom Label", 25, 25, 12, "CYBER-ESP32")
            OledElementType.HARDWARE_LOGO -> OledElement(id, type, "Hardware Logo", 50, 5, 10, "⚙ ESP32")
        }
        _watchFaceElements.value = _watchFaceElements.value + newElem
    }

    fun removeOledElement(id: String) {
        _watchFaceElements.value = _watchFaceElements.value.filter { it.id != id }
    }

    fun applyWatchFacePreset(preset: WatchFacePreset) {
        _watchFaceElements.value = preset.elements
    }

    fun pushWatchFaceToEsp32() {
        viewModelScope.launch {
            _oledPushProgress.value = 0.0f
            for (p in 1..10) {
                delay(120)
                _oledPushProgress.value = p / 10f
            }
            delay(200)
            _oledPushProgress.value = null
            val payload = "{\"cmd\": \"OLED_FLASH\", \"count\": ${_watchFaceElements.value.size}}"
            bleManager.sendCommandJson(payload)
            _userFeedback.value = "Watch Face sent to ESP32 OLED over I2C/BLE!"
        }
    }

    // Feature 6: Buzzer & Melody Studio Actions
    fun updateMelodyStepNote(stepIndex: Int, newNote: String) {
        _melodyGrid.value = _melodyGrid.value.map {
            if (it.stepIndex == stepIndex) {
                it.copy(noteName = newNote, frequencyHz = getFreqForNote(newNote))
            } else it
        }
    }

    fun playFindMyWatchSos() {
        val payload = "{\"cmd\": \"BEEP\", \"dur\": 2000, \"freq\": 1046, \"pattern\": \"SOS\"}"
        bleManager.sendCommandJson(payload)
        _userFeedback.value = "SOS Beacon triggered on Watch Buzzer!"
    }

    fun pushMelodyToWatch() {
        val notesCsv = _melodyGrid.value.joinToString(",") { it.noteName }
        val payload = "{\"cmd\": \"MELODY\", \"notes\": \"$notesCsv\"}"
        bleManager.sendCommandJson(payload)
        _userFeedback.value = "Melody uploaded to ESP32 Buzzer memory!"
    }

    fun addAlarm(hour: Int, minute: Int, label: String, melody: String) {
        viewModelScope.launch {
            repository.addAlarm(
                AlarmModel(
                    hour = hour,
                    minute = minute,
                    label = label,
                    isEnabled = true,
                    melodyName = melody,
                    repeatDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
                )
            )
            val payload = "{\"cmd\": \"SET_ALARM\", \"time\": \"${String.format("%02d:%02d", hour, minute)}\"}"
            bleManager.sendCommandJson(payload)
            _userFeedback.value = "Alarm set for ${String.format("%02d:%02d", hour, minute)}"
        }
    }

    fun deleteAlarm(id: Int) {
        viewModelScope.launch {
            repository.deleteAlarm(id)
        }
    }

    // Voice Emergency Methods
    fun initVoiceManager(context: android.content.Context) {
        if (voiceManager == null) {
            voiceManager = VoiceEmergencyManager(context, viewModelScope) {
                triggerVoiceEmergency("Voice Shout 'HELP HELP'")
            }
            if (_isVoiceListening.value) {
                voiceManager?.startListening()
            }
        }
    }

    fun toggleVoiceListening(enabled: Boolean) {
        _isVoiceListening.value = enabled
        if (enabled) {
            voiceManager?.startListening()
            _userFeedback.value = "Voice trigger listening activated."
        } else {
            voiceManager?.stopListening()
            _userFeedback.value = "Voice trigger listening paused."
        }
    }

    fun simulateVoiceTriggerHelp() {
        _lastRecognizedSpeech.value = "HELP HELP"
        voiceManager?.simulateVoicePhrase("HELP HELP") ?: triggerVoiceEmergency("Simulated Shout 'HELP HELP'")
    }

    // Emergency Contact Mobile Number Management
    fun addEmergencyContact(name: String, phoneNumber: String, relationship: String, isPrimary: Boolean) {
        val current = _emergencyContacts.value.toMutableList()
        val formattedPrimary = if (isPrimary) {
            current.map { it.copy(isPrimary = false) }
        } else {
            current
        }
        val newContact = EmergencyContact(
            name = name,
            phoneNumber = phoneNumber,
            relationship = relationship,
            isPrimary = isPrimary || formattedPrimary.none { it.isPrimary },
            isEnabled = true
        )
        _emergencyContacts.value = (formattedPrimary + newContact).sortedByDescending { it.isPrimary }
        _userFeedback.value = "Added emergency mobile contact: $name ($phoneNumber)"
        addEmergencyLog("📱 Added emergency contact: $name ($phoneNumber)")
    }

    fun removeEmergencyContact(id: String) {
        val contact = _emergencyContacts.value.find { it.id == id }
        _emergencyContacts.value = _emergencyContacts.value.filterNot { it.id == id }
        contact?.let {
            _userFeedback.value = "Removed emergency contact: ${it.name}"
            addEmergencyLog("🗑️ Removed emergency contact: ${it.name}")
        }
    }

    fun toggleEmergencyContact(id: String, isEnabled: Boolean) {
        _emergencyContacts.value = _emergencyContacts.value.map {
            if (it.id == id) it.copy(isEnabled = isEnabled) else it
        }
    }

    fun setPrimaryEmergencyContact(id: String) {
        _emergencyContacts.value = _emergencyContacts.value.map {
            it.copy(isPrimary = (it.id == id))
        }.sortedByDescending { it.isPrimary }
        _userFeedback.value = "Primary emergency mobile contact updated."
    }

    fun triggerVoiceEmergency(reason: String = "HELP HELP Triggered") {
        if (_isEmergencyActive.value) return
        _isEmergencyActive.value = true
        _isEmergencyDispatched.value = false
        _emergencyCountdown.value = 5

        sirenManager.startSiren(viewModelScope)

        addEmergencyLog("🚨 $reason! High priority siren activated & 5s countdown started.")

        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (i in 5 downTo 1) {
                _emergencyCountdown.value = i
                delay(1000)
                if (!_isEmergencyActive.value) return@launch
            }
            _emergencyCountdown.value = 0
            _isEmergencyDispatched.value = true
            sirenManager.stopSiren()

            val activeContacts = _emergencyContacts.value.filter { it.isEnabled }
            val contactsSummary = if (activeContacts.isNotEmpty()) {
                activeContacts.joinToString(", ") { "${it.name} (${it.phoneNumber})" }
            } else {
                "Default 911 Emergency Dispatch"
            }

            val sosPayload = "{\"cmd\":\"EMERGENCY_SOS\",\"lat\":37.7749,\"lon\":-122.4194,\"msg\":\"HELP HELP DETECTED\",\"contacts\":\"$contactsSummary\"}"
            bleManager.sendCommandJson(sosPayload)

            addEmergencyLog("⚡ SOS SENT: GPS Location (37.7749° N, 122.4194° W) & Emergency SMS/Call Beacon dispatched to mobile numbers: $contactsSummary")
        }
    }

    fun cancelEmergencyAlert() {
        countdownJob?.cancel()
        countdownJob = null
        _isEmergencyActive.value = false
        _isEmergencyDispatched.value = false
        sirenManager.stopSiren()
        addEmergencyLog("✅ False Alarm Canceled. System returned to listening mode.")
        _userFeedback.value = "Emergency false alarm canceled."
    }

    private fun addEmergencyLog(logMsg: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val entry = "[$timestamp] $logMsg"
        _emergencyLog.value = listOf(entry) + _emergencyLog.value.take(20)
    }

    private fun getFreqForNote(note: String): Int {
        return when (note) {
            "C4" -> 262
            "D4" -> 294
            "E4" -> 330
            "F4" -> 349
            "G4" -> 392
            "A4" -> 440
            "B4" -> 494
            "C5" -> 523
            "D5" -> 587
            "E5" -> 659
            else -> 0
        }
    }
}
