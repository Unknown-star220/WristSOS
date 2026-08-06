package com.example.ui.components

import android.media.ToneGenerator
import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NoteStep
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberPink
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

private val AVAILABLE_NOTES = listOf("C4", "D4", "E4", "F4", "G4", "A4", "B4", "C5", "REST")

/**
 * Interactive 8-Bit Chip-Tune Melody Sequencer.
 * Allows picking tones across 16 steps and testing buzzer audio locally.
 */
@Composable
fun MelodySequencer(
    notes: List<NoteStep>,
    onNoteChanged: (stepIndex: Int, newNote: String) -> Unit,
    onPushMelody: () -> Unit,
    modifier: Modifier = Modifier
) {
    val toneGen = remember {
        try {
            ToneGenerator(AudioManager.STREAM_ALARM, 70)
        } catch (e: Exception) {
            null
        }
    }

    CyberCard(modifier = modifier.testTag("melody_sequencer_card")) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "8-BIT CHIP-TUNE MELODY STUDIO",
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonGreen
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "ESP32 BUZZER TONES",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step Grid (16 Steps)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(notes) { index, step ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberBlack)
                            .border(1.dp, if (step.noteName != "REST") NeonCyan else CyberCardBorder, RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "#${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (step.noteName != "REST") CyberDarkSurface else CyberBlack)
                                .border(1.dp, if (step.noteName != "REST") CyberPink else CyberCardBorder, RoundedCornerShape(6.dp))
                                .clickable {
                                    val currentIdx = AVAILABLE_NOTES.indexOf(step.noteName)
                                    val nextNote = AVAILABLE_NOTES[(currentIdx + 1) % AVAILABLE_NOTES.size]
                                    onNoteChanged(index, nextNote)
                                    playToneForNote(toneGen, nextNote)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = step.noteName,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (step.noteName != "REST") NeonCyan else TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        // Play sequence
                        notes.forEach { n ->
                            if (n.noteName != "REST") {
                                playToneForNote(toneGen, n.noteName)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberDarkSurface),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("preview_melody_button")
                ) {
                    Text(text = "▶ PREVIEW SYNTH", color = NeonCyan)
                }

                Button(
                    onClick = onPushMelody,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("upload_melody_button")
                ) {
                    Text(text = "FLASH TO WATCH BUZZER", color = CyberBlack)
                }
            }
        }
    }
}

private fun playToneForNote(toneGen: ToneGenerator?, note: String) {
    if (toneGen == null) return
    val toneType = when (note) {
        "C4" -> ToneGenerator.TONE_DTMF_1
        "D4" -> ToneGenerator.TONE_DTMF_2
        "E4" -> ToneGenerator.TONE_DTMF_3
        "F4" -> ToneGenerator.TONE_DTMF_4
        "G4" -> ToneGenerator.TONE_DTMF_5
        "A4" -> ToneGenerator.TONE_DTMF_6
        "B4" -> ToneGenerator.TONE_DTMF_7
        "C5" -> ToneGenerator.TONE_DTMF_8
        else -> -1
    }
    if (toneType != -1) {
        try {
            toneGen.startTone(toneType, 120)
        } catch (_: Exception) {}
    }
}
