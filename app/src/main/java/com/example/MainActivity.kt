package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.EmergencyAlertOverlay
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MelodyStudioScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.VoiceSosScreen
import com.example.ui.screens.WatchFaceDesignerScreen
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberWatchTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.SmartwatchViewModel

sealed class AppTab(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : AppTab("dashboard", "Dashboard", Icons.Default.Dashboard)
    data object VoiceSOS : AppTab("voice_sos", "Voice SOS", Icons.Default.Campaign)
    data object Notifications : AppTab("notifications", "Sync", Icons.Default.Notifications)
    data object WatchFace : AppTab("watch_face", "OLED Face", Icons.Default.Palette)
    data object Melody : AppTab("melody", "Buzzer Studio", Icons.Default.MusicNote)
}

class MainActivity : ComponentActivity() {

    private val viewModel: SmartwatchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CyberWatchTheme {
                val context = LocalContext.current
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(Unit) {
                    viewModel.initVoiceManager(context)
                }

                val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
                val discoveredDevices by viewModel.discoveredDevices.collectAsStateWithLifecycle()
                val alarms by viewModel.alarms.collectAsStateWithLifecycle()
                val appFilters by viewModel.appFilters.collectAsStateWithLifecycle()
                val watchFaceElements by viewModel.watchFaceElements.collectAsStateWithLifecycle()
                val melodyGrid by viewModel.melodyGrid.collectAsStateWithLifecycle()
                val raiseToWakeSensitivity by viewModel.raiseToWakeSensitivity.collectAsStateWithLifecycle()
                val pushButtonAction by viewModel.pushButtonAction.collectAsStateWithLifecycle()
                val oledPushProgress by viewModel.oledPushProgress.collectAsStateWithLifecycle()
                val userFeedback by viewModel.userFeedback.collectAsStateWithLifecycle()

                // Emergency Voice Alert States
                val isEmergencyActive by viewModel.isEmergencyActive.collectAsStateWithLifecycle()
                val emergencyCountdown by viewModel.emergencyCountdown.collectAsStateWithLifecycle()
                val isEmergencyDispatched by viewModel.isEmergencyDispatched.collectAsStateWithLifecycle()
                val isVoiceListening by viewModel.isVoiceListening.collectAsStateWithLifecycle()
                val lastRecognizedSpeech by viewModel.lastRecognizedSpeech.collectAsStateWithLifecycle()
                val emergencyLog by viewModel.emergencyLog.collectAsStateWithLifecycle()
                val emergencyContacts by viewModel.emergencyContacts.collectAsStateWithLifecycle()

                LaunchedEffect(userFeedback) {
                    userFeedback?.let { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        viewModel.clearFeedback()
                    }
                }

                var selectedTabIndex by remember { mutableIntStateOf(0) }
                val tabs = listOf(
                    AppTab.Dashboard,
                    AppTab.VoiceSOS,
                    AppTab.Notifications,
                    AppTab.WatchFace,
                    AppTab.Melody
                )

                val configuration = LocalConfiguration.current
                val isWideScreen = configuration.screenWidthDp >= 600

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        bottomBar = {
                            if (!isWideScreen) {
                                NavigationBar(
                                    containerColor = CyberDarkSurface,
                                    contentColor = TextPrimary,
                                    modifier = Modifier.testTag("mobile_navigation_bar")
                                ) {
                                    tabs.forEachIndexed { index, tab ->
                                        NavigationBarItem(
                                            selected = selectedTabIndex == index,
                                            onClick = { selectedTabIndex = index },
                                            icon = {
                                                Icon(
                                                    imageVector = tab.icon,
                                                    contentDescription = tab.title,
                                                    tint = if (selectedTabIndex == index) NeonCyan else TextMuted
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = tab.title,
                                                    color = if (selectedTabIndex == index) NeonCyan else TextMuted,
                                                    style = MaterialTheme.typography.labelMedium
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                indicatorColor = CyberDarkSurface
                                            )
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize().background(CyberBlack)
                    ) { innerPadding ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (isWideScreen) {
                                NavigationRail(
                                    containerColor = CyberDarkSurface,
                                    contentColor = TextPrimary,
                                    modifier = Modifier.testTag("tablet_navigation_rail")
                                ) {
                                    tabs.forEachIndexed { index, tab ->
                                        NavigationRailItem(
                                            selected = selectedTabIndex == index,
                                            onClick = { selectedTabIndex = index },
                                            icon = {
                                                Icon(
                                                    imageVector = tab.icon,
                                                    contentDescription = tab.title,
                                                    tint = if (selectedTabIndex == index) NeonCyan else TextMuted
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = tab.title,
                                                    color = if (selectedTabIndex == index) NeonCyan else TextMuted,
                                                    style = MaterialTheme.typography.labelMedium
                                                )
                                            },
                                            colors = NavigationRailItemDefaults.colors(
                                                indicatorColor = CyberDarkSurface
                                            )
                                        )
                                    }
                                }
                            }

                            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                                when (selectedTabIndex) {
                                    0 -> DashboardScreen(
                                        telemetry = telemetry,
                                        discoveredDevices = discoveredDevices,
                                        onStartScan = { viewModel.startScan() },
                                        onConnectDevice = { viewModel.connectDevice(it) },
                                        onDisconnect = { viewModel.disconnectBle() },
                                        onToggleMockMode = { viewModel.toggleMockSimulator(it) },
                                        onToggleCharging = { viewModel.toggleChargingSim(it) },
                                        onSimulateButtonPress = { viewModel.simulateButtonPress() },
                                        onTriggerFallSim = { viewModel.triggerVoiceEmergency("Manual Dashboard SOS Trigger") },
                                        onResetFallAlert = { viewModel.cancelEmergencyAlert() }
                                    )

                                    1 -> VoiceSosScreen(
                                        isListening = isVoiceListening,
                                        lastRecognizedText = lastRecognizedSpeech,
                                        emergencyLogs = emergencyLog,
                                        emergencyContacts = emergencyContacts,
                                        onToggleListening = { viewModel.toggleVoiceListening(it) },
                                        onSimulateTrigger = { viewModel.simulateVoiceTriggerHelp() },
                                        onAddContact = { name, num, rel, isPri -> viewModel.addEmergencyContact(name, num, rel, isPri) },
                                        onRemoveContact = { id -> viewModel.removeEmergencyContact(id) },
                                        onToggleContact = { id, enabled -> viewModel.toggleEmergencyContact(id, enabled) },
                                        onSetPrimaryContact = { id -> viewModel.setPrimaryEmergencyContact(id) }
                                    )

                                    2 -> NotificationsScreen(
                                        appFilters = appFilters,
                                        raiseToWakeSensitivity = raiseToWakeSensitivity,
                                        pushButtonAction = pushButtonAction,
                                        onSendNotification = { app, txt -> viewModel.sendNotificationPayload(app, txt) },
                                        onToggleMuteApp = { pkg, muted -> viewModel.toggleAppFilterMute(pkg, muted) },
                                        onRaiseToWakeChanged = { viewModel.raiseToWakeSensitivity.value = it },
                                        onPushActionChanged = { viewModel.pushButtonAction.value = it },
                                        onTriggerFallSim = { viewModel.triggerVoiceEmergency("Manual Safety Fall Trigger") }
                                    )

                                    3 -> WatchFaceDesignerScreen(
                                        elements = watchFaceElements,
                                        pushProgress = oledPushProgress,
                                        onPositionChanged = { id, x, y -> viewModel.updateOledElementPosition(id, x, y) },
                                        onAddElement = { viewModel.addOledElement(it) },
                                        onRemoveElement = { viewModel.removeOledElement(it) },
                                        onApplyPreset = { viewModel.applyWatchFacePreset(it) },
                                        onPushToWatch = { viewModel.pushWatchFaceToEsp32() }
                                    )

                                    4 -> MelodyStudioScreen(
                                        notes = melodyGrid,
                                        alarms = alarms,
                                        onNoteChanged = { step, note -> viewModel.updateMelodyStepNote(step, note) },
                                        onPushMelody = { viewModel.pushMelodyToWatch() },
                                        onFindMyWatch = { viewModel.playFindMyWatchSos() },
                                        onAddAlarm = { h, m, label, tone -> viewModel.addAlarm(h, m, label, tone) },
                                        onDeleteAlarm = { viewModel.deleteAlarm(it) }
                                    )
                                }
                            }
                        }
                    }

                    // High Priority Emergency Flashing Red Alert Overlay
                    AnimatedVisibility(
                        visible = isEmergencyActive,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        EmergencyAlertOverlay(
                            countdown = emergencyCountdown,
                            isDispatched = isEmergencyDispatched,
                            contacts = emergencyContacts,
                            onCancelAlert = { viewModel.cancelEmergencyAlert() }
                        )
                    }
                }
            }
        }
    }
}
