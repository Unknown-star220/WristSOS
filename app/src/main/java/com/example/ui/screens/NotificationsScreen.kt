package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.NotificationAppFilter
import com.example.ui.components.CyberCard
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberPink
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun NotificationsScreen(
    appFilters: List<NotificationAppFilter>,
    raiseToWakeSensitivity: Float,
    pushButtonAction: String,
    onSendNotification: (appName: String, text: String) -> Unit,
    onToggleMuteApp: (packageName: String, isMuted: Boolean) -> Unit,
    onRaiseToWakeChanged: (Float) -> Unit,
    onPushActionChanged: (String) -> Unit,
    onTriggerFallSim: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var appNameInput by remember { mutableStateOf("Messages") }
    var notificationTextMsg by remember { mutableStateOf("Meeting in 10 mins!") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // FEATURE 4: Notification Payload Pusher to OLED
        CyberCard(modifier = Modifier.testTag("push_notification_card")) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = NeonCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SMARTPHONE NOTIFICATIONS SYNC",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = appNameInput,
                    onValueChange = { appNameInput = it },
                    label = { Text("App Name", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("app_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notificationTextMsg,
                    onValueChange = { notificationTextMsg = it },
                    label = { Text("Notification Text Payload (0.96\" OLED)", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("notification_text_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (notificationTextMsg.isNotBlank()) {
                            onSendNotification(appNameInput, notificationTextMsg)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("send_notification_payload_button")
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = CyberBlack)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Push Notification Payload over BLE", color = CyberBlack)
                }
            }
        }

        // FEATURE 4 CONTINUED: App Filter UI
        CyberCard(modifier = Modifier.testTag("app_filter_card")) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FilterList, contentDescription = null, tint = NeonGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "APP NOTIFICATION MUTE FILTER",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonGreen
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                appFilters.forEach { filter ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberDarkSurface)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Smartphone, contentDescription = null, tint = if (filter.isMuted) TextMuted else NeonCyan)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = filter.appName, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text(text = filter.packageName, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                        }
                        Text(
                            text = if (filter.isMuted) "MUTED" else "ACTIVE",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (filter.isMuted) CyberPink else NeonGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = !filter.isMuted,
                            onCheckedChange = { active -> onToggleMuteApp(filter.packageName, !active) },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen, checkedTrackColor = CyberBlack)
                        )
                    }
                }
            }
        }

        // FEATURE 7: Gesture Controls & Safety Settings
        CyberCard(modifier = Modifier.testTag("gesture_safety_card")) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Gesture, contentDescription = null, tint = CyberPink)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GESTURE CONTROLS & SAFETY",
                        style = MaterialTheme.typography.labelLarge,
                        color = CyberPink
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Raise to Wake Slider
                Text(text = "Raise to Wake Wrist Tilt Sensitivity: ${(raiseToWakeSensitivity).toInt()}%", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Slider(
                    value = raiseToWakeSensitivity,
                    onValueChange = onRaiseToWakeChanged,
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan, inactiveTrackColor = CyberBlack),
                    modifier = Modifier.testTag("raise_to_wake_slider")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Push Button Action Mapping
                Text(text = "Hardware Push Button Action Mapping:", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                val actions = listOf(
                    "TAKE_PHOTO" to "Trigger Phone Camera Flash & Snap",
                    "FIND_PHONE" to "Ring Phone at Max Volume",
                    "TOGGLE_MUTE" to "Toggle Silent / Mute Mode",
                    "SOS_CALL" to "Trigger Immediate Emergency SOS"
                )

                actions.forEach { (actionKey, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (pushButtonAction == actionKey) CyberDarkSurface else CyberBlack)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (pushButtonAction == actionKey),
                            onClick = { onPushActionChanged(actionKey) },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onTriggerFallSim,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPink),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("fall_detection_test_button")
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test 9G Fall Detection Alert", color = Color.White)
                }
            }
        }
    }
}
