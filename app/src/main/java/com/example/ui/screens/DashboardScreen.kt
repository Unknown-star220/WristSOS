package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BleConnectionState
import com.example.data.model.DiscoveredDevice
import com.example.data.model.WatchTelemetry
import com.example.ui.components.CyberCard
import com.example.ui.components.GyroVisualizer
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun DashboardScreen(
    telemetry: WatchTelemetry,
    discoveredDevices: List<DiscoveredDevice>,
    onStartScan: () -> Unit,
    onConnectDevice: (DiscoveredDevice) -> Unit,
    onDisconnect: () -> Unit,
    onToggleMockMode: (Boolean) -> Unit,
    onToggleCharging: (Boolean) -> Unit,
    onSimulateButtonPress: () -> Unit,
    onTriggerFallSim: () -> Unit,
    onResetFallAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Fall Alert Banner
        AnimatedVisibility(visible = telemetry.isFallDetected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberPink)
                    .padding(16.dp)
                    .clickable { onResetFallAlert() }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = "Fall Alert", tint = Color.White)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "CRITICAL: FALL DETECTED (MPU-6050 9G SPIKE)", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(text = "Tap to acknowledge and cancel SOS beacon", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                    }
                }
            }
        }

        // FEATURE 1: BLE Connection Manager Card
        CyberCard(modifier = Modifier.testTag("ble_connection_card")) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Bluetooth, contentDescription = null, tint = NeonCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BLE CONNECTION MANAGER",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    // Connection Badge
                    val (statusText, statusColor) = when (telemetry.connectionState) {
                        BleConnectionState.PAIRED -> "PAIRED" to NeonGreen
                        BleConnectionState.SCANNING -> "SCANNING..." to NeonCyan
                        BleConnectionState.SYNCING -> "SYNCING..." to NeonCyan
                        BleConnectionState.DISCONNECTED -> "DISCONNECTED" to CyberPink
                        BleConnectionState.ERROR -> "ERROR" to CyberPink
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.2f))
                            .border(1.dp, statusColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = telemetry.deviceName, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(text = "Target Hardware: ESP32 / MPU-6050 / OLED 0.96\"", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                    }

                    if (telemetry.connectionState == BleConnectionState.PAIRED) {
                        Button(
                            onClick = onDisconnect,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberDarkSurface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("disconnect_ble_button")
                        ) {
                            Text("Disconnect", color = CyberPink)
                        }
                    } else {
                        Button(
                            onClick = onStartScan,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("scan_ble_button")
                        ) {
                            Icon(Icons.Default.BluetoothSearching, contentDescription = null, tint = CyberBlack)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan BLE", color = CyberBlack)
                        }
                    }
                }

                // Discovered BLE Devices List
                if (discoveredDevices.isNotEmpty() && telemetry.connectionState != BleConnectionState.PAIRED) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Discovered Hardware Nodes:", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    discoveredDevices.forEach { dev ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberSurfaceVariant)
                                .padding(12.dp)
                                .clickable { onConnectDevice(dev) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Memory, contentDescription = null, tint = NeonCyan)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = dev.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                Text(text = "${dev.address} | RSSI: ${dev.rssi} dBm", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                            }
                            Text(text = "PAIR", style = MaterialTheme.typography.labelLarge, color = NeonGreen)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mock Data Simulator Toggle Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberBlack)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.BugReport, contentDescription = null, tint = NeonCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Mock Data Simulator", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(text = "Generates 60Hz ESP32 MPU-6050 & battery stream", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                    }
                    Switch(
                        checked = telemetry.isMockMode,
                        onCheckedChange = onToggleMockMode,
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = CyberDarkSurface),
                        modifier = Modifier.testTag("mock_simulator_switch")
                    )
                }
            }
        }

        // FEATURE 2: Battery & TP4056 Charging Status Widget
        CyberCard(modifier = Modifier.testTag("battery_tp4056_card")) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = NeonGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "3.7V LIPO BATTERY & TP4056 CHARGER",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonGreen
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (telemetry.isCharging) NeonGreen.copy(alpha = 0.2f) else CyberDarkSurface)
                            .border(1.dp, if (telemetry.isCharging) NeonGreen else CyberCardBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (telemetry.isCharging) "TP4056 CHARGING" else "BATTERY MODE",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (telemetry.isCharging) NeonGreen else TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${telemetry.batteryPercent}%",
                        style = MaterialTheme.typography.displayLarge,
                        color = if (telemetry.batteryPercent > 20) NeonGreen else CyberPink
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "ADC Output: ${String.format("%.2f", telemetry.batteryVoltage)}V DC", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { telemetry.batteryPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = if (telemetry.batteryPercent > 20) NeonGreen else CyberPink,
                            trackColor = CyberBlack,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onToggleCharging(!telemetry.isCharging) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberDarkSurface),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("toggle_charging_button")
                ) {
                    Icon(Icons.Default.Power, contentDescription = null, tint = NeonCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (telemetry.isCharging) "Disconnect Charger (Battery Mode)" else "Plug In TP4056 USB Charger",
                        color = TextPrimary
                    )
                }
            }
        }

        // FEATURE 2 CONTINUED: 3D Gyroscope Visualizer
        GyroVisualizer(
            gyroX = telemetry.gyroX,
            gyroY = telemetry.gyroY,
            gyroZ = telemetry.gyroZ
        )

        // HARDWARE CONTROLS & JSON TELEMETRY INSPECTOR
        CyberCard(modifier = Modifier.testTag("telemetry_inspector_card")) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = NeonCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ESP32 BUTTON & TELEMETRY JSON STREAM",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onSimulateButtonPress,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (telemetry.buttonPressed) CyberPink else CyberDarkSurface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("press_hardware_button")
                    ) {
                        Text(
                            text = if (telemetry.buttonPressed) "BUTTON PRESSED!" else "Simulate ESP32 Push Button",
                            color = if (telemetry.buttonPressed) Color.White else TextPrimary
                        )
                    }

                    Button(
                        onClick = onTriggerFallSim,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPink),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("trigger_fall_sim_button")
                    ) {
                        Text("Simulate 9G Fall", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Incoming Telemetry (Raw ESP32 JSON):", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberBlack)
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = telemetry.rawJsonIncoming,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Outgoing BLE Payload Command:", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberBlack)
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = telemetry.rawJsonOutgoing,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = NeonGreen
                    )
                }
            }
        }
    }
}
