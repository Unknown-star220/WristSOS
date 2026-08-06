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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.OledElement
import com.example.data.model.OledElementType
import com.example.data.model.WatchFacePreset
import com.example.ui.components.CyberCard
import com.example.ui.components.OledCanvas
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
fun WatchFaceDesignerScreen(
    elements: List<OledElement>,
    pushProgress: Float?,
    onPositionChanged: (id: String, x: Int, y: Int) -> Unit,
    onAddElement: (OledElementType) -> Unit,
    onRemoveElement: (id: String) -> Unit,
    onApplyPreset: (WatchFacePreset) -> Unit,
    onPushToWatch: () -> Unit,
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
        // OLED CANVAS DISPLAY & TRANSMISSION PROGRESS
        CyberCard(modifier = Modifier.testTag("oled_canvas_card")) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = NeonCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "128x64 MONOCHROME OLED DESIGNER",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OledCanvas(
                    elements = elements,
                    onPositionChanged = onPositionChanged
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Drag elements on canvas to reposition for 0.96-inch OLED screen",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )

                // Flash Progress
                AnimatedVisibility(visible = pushProgress != null) {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        Text(
                            text = "Pushing layout to ESP32 over I2C/BLE... ${(pushProgress ?: 0f) * 100}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonGreen
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { pushProgress ?: 0f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = NeonGreen,
                            trackColor = CyberBlack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onPushToWatch,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("push_to_watch_button")
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = CyberBlack)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Push Layout to ESP32 Watch", color = CyberBlack)
                }
            }
        }

        // TOOLKIT: Add Elements
        CyberCard(modifier = Modifier.testTag("add_elements_toolkit_card")) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Widgets, contentDescription = null, tint = NeonGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CANVAS ELEMENT TOOLKIT",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonGreen
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val elementTypes = listOf(
                    OledElementType.TIME_DIGITAL to "Digital Clock",
                    OledElementType.DATE_TEXT to "Date Text",
                    OledElementType.BATTERY_BAR to "Battery Bar",
                    OledElementType.STEP_COUNTER to "Step Counter",
                    OledElementType.CALORIE_COUNTER to "Calorie Counter",
                    OledElementType.HEART_GLYPH to "Heart Glyph",
                    OledElementType.CUSTOM_TEXT to "Custom Label",
                    OledElementType.HARDWARE_LOGO to "ESP32 Logo"
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    elementTypes.chunked(2).forEach { rowPair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowPair.forEach { (type, label) ->
                                Button(
                                    onClick = { onAddElement(type) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberDarkSurface),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).testTag("add_${type.name}")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = NeonCyan)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = label, color = TextPrimary, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ACTIVE ELEMENTS MANAGER LIST
        CyberCard(modifier = Modifier.testTag("active_elements_card")) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ACTIVE CANVAS LAYOUT ELEMENTS (${elements.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonCyan
                )

                Spacer(modifier = Modifier.height(12.dp))

                elements.forEach { elem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberDarkSurface)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = elem.label, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text(text = "Grid Position: (${elem.x}, ${elem.y}) | Preview: ${elem.text}", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                        }
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = CyberPink,
                            modifier = Modifier.clickable { onRemoveElement(elem.id) }
                        )
                    }
                }
            }
        }

        // PRESET PICKER
        CyberCard(modifier = Modifier.testTag("presets_card")) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "WATCH FACE DESIGN PRESETS",
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonGreen
                )

                Spacer(modifier = Modifier.height(12.dp))

                val presets = listOf(
                    WatchFacePreset(
                        id = "p1",
                        name = "Cyberpunk Matrix",
                        elements = listOf(
                            OledElement("p1_1", OledElementType.TIME_DIGITAL, "Clock", 18, 10, 20, "12:45"),
                            OledElement("p1_2", OledElementType.DATE_TEXT, "Date", 28, 35, 10, "THU 06 AUG"),
                            OledElement("p1_3", OledElementType.BATTERY_BAR, "Battery", 5, 52, 10, "88% BATT"),
                            OledElement("p1_4", OledElementType.STEP_COUNTER, "Steps", 75, 52, 10, "4.2K STEPS")
                        )
                    ),
                    WatchFacePreset(
                        id = "p2",
                        name = "Minimalist HUD",
                        elements = listOf(
                            OledElement("p2_1", OledElementType.TIME_DIGITAL, "Clock", 30, 20, 22, "12:45"),
                            OledElement("p2_2", OledElementType.BATTERY_BAR, "Battery", 45, 48, 10, "⚡ 88%")
                        )
                    )
                )

                presets.forEach { preset ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberSurfaceVariant)
                            .padding(12.dp)
                            .clickable { onApplyPreset(preset) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = preset.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text(text = "${preset.elements.size} OLED Elements", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                        }
                        Text(text = "APPLY PRESET", style = MaterialTheme.typography.labelLarge, color = NeonCyan)
                    }
                }
            }
        }
    }
}
