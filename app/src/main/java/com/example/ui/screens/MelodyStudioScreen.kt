package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import com.example.data.model.AlarmModel
import com.example.data.model.NoteStep
import com.example.ui.components.CyberCard
import com.example.ui.components.MelodySequencer
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberPink
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun MelodyStudioScreen(
    notes: List<NoteStep>,
    alarms: List<AlarmModel>,
    onNoteChanged: (stepIndex: Int, newNote: String) -> Unit,
    onPushMelody: () -> Unit,
    onFindMyWatch: () -> Unit,
    onAddAlarm: (hour: Int, minute: Int, label: String, melody: String) -> Unit,
    onDeleteAlarm: (id: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var alarmHourStr by remember { mutableStateOf("07") }
    var alarmMinuteStr by remember { mutableStateOf("30") }
    var alarmLabelStr by remember { mutableStateOf("Morning Cyber Wakeup") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // FEATURE 6: Giant Red "FIND MY WATCH" SOS Button
        CyberCard(
            borderColor = CyberPink,
            glowColor = CyberPink.copy(alpha = 0.3f),
            modifier = Modifier.testTag("find_my_watch_card")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = CyberPink)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FIND MY WATCH (SOS BEACON)",
                        style = MaterialTheme.typography.labelLarge,
                        color = CyberPink
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onFindMyWatch,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPink),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("find_my_watch_sos_button")
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "TRIGGER LOUD SOS BEEP ON WATCH BUZZER",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }

        // FEATURE 6 CONTINUED: 8-bit Chip-Tune Melody Sequencer
        MelodySequencer(
            notes = notes,
            onNoteChanged = onNoteChanged,
            onPushMelody = onPushMelody
        )

        // FEATURE 6 CONTINUED: Multi-Alarm Manager
        CyberCard(modifier = Modifier.testTag("multi_alarm_manager_card")) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Alarm, contentDescription = null, tint = NeonCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MULTI-ALARM MANAGER",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // New Alarm Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = alarmHourStr,
                        onValueChange = { if (it.length <= 2) alarmHourStr = it },
                        label = { Text("HH (00-23)", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f).testTag("alarm_hour_input")
                    )

                    OutlinedTextField(
                        value = alarmMinuteStr,
                        onValueChange = { if (it.length <= 2) alarmMinuteStr = it },
                        label = { Text("MM (00-59)", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f).testTag("alarm_minute_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = alarmLabelStr,
                    onValueChange = { alarmLabelStr = it },
                    label = { Text("Alarm Label / Notes", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("alarm_label_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val h = alarmHourStr.toIntOrNull() ?: 7
                        val m = alarmMinuteStr.toIntOrNull() ?: 30
                        onAddAlarm(h, m, alarmLabelStr, "Custom 8-Bit Melody")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_alarm_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = CyberBlack)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Alarm to ESP32 Memory", color = CyberBlack)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Saved Alarms List
                alarms.forEach { alarm ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberDarkSurface)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = String.format("%02d:%02d", alarm.hour, alarm.minute),
                                style = MaterialTheme.typography.displayMedium,
                                color = NeonCyan
                            )
                            Text(text = alarm.label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            Text(text = "Tone: ${alarm.melodyName} | Days: ${alarm.repeatDays.joinToString()}", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                        }

                        Button(
                            onClick = { onDeleteAlarm(alarm.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberBlack),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CyberPink)
                        }
                    }
                }
            }
        }
    }
}
