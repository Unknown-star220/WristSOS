package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberPink
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import kotlin.math.cos
import kotlin.math.sin

/**
 * Real-time 3D Wireframe Cube & Pitch/Roll Radar visualizer.
 * Debounced and smoothed to 30 FPS using Compose state animations.
 */
@Composable
fun GyroVisualizer(
    gyroX: Float,
    gyroY: Float,
    gyroZ: Float,
    modifier: Modifier = Modifier
) {
    // Smooth angle interpolation to prevent visual jitter
    val smoothX by animateFloatAsState(targetValue = gyroX, animationSpec = tween(100), label = "gx")
    val smoothY by animateFloatAsState(targetValue = gyroY, animationSpec = tween(100), label = "gy")
    val smoothZ by animateFloatAsState(targetValue = gyroZ, animationSpec = tween(100), label = "gz")

    CyberCard(modifier = modifier.testTag("gyro_visualizer_card")) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MPU-6050 GYRO / ACCEL RADAR",
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonCyan
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "3D HARDWARE TILT",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(CyberBlack, RoundedCornerShape(12.dp))
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f

                    // Draw Cyber Grid Target Lines
                    drawCircle(
                        color = NeonCyan.copy(alpha = 0.2f),
                        radius = 70.dp.toPx(),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = NeonCyan.copy(alpha = 0.1f),
                        radius = 40.dp.toPx(),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawLine(
                        color = NeonCyan.copy(alpha = 0.2f),
                        start = Offset(cx - 80.dp.toPx(), cy),
                        end = Offset(cx + 80.dp.toPx(), cy),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = NeonCyan.copy(alpha = 0.2f),
                        start = Offset(cx, cy - 70.dp.toPx()),
                        end = Offset(cx, cy + 70.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Compute 3D Cube Rotation based on smooth Gyro X, Y, Z
                    val rotX = Math.toRadians((smoothX * 1.5).toDouble())
                    val rotY = Math.toRadians((smoothY * 1.5).toDouble())
                    val rotZ = Math.toRadians((smoothZ * 1.5).toDouble())

                    val cubeSize = 38.dp.toPx()
                    val vertices = arrayOf(
                        floatArrayOf(-1f, -1f, -1f),
                        floatArrayOf(1f, -1f, -1f),
                        floatArrayOf(1f, 1f, -1f),
                        floatArrayOf(-1f, 1f, -1f),
                        floatArrayOf(-1f, -1f, 1f),
                        floatArrayOf(1f, -1f, 1f),
                        floatArrayOf(1f, 1f, 1f),
                        floatArrayOf(-1f, 1f, 1f)
                    )

                    // Project 3D points to 2D screen space
                    val projected = vertices.map { vertex ->
                        var x = vertex[0] * cubeSize
                        var y = vertex[1] * cubeSize
                        var z = vertex[2] * cubeSize

                        // Rotate Pitch (X)
                        val y1 = y * cos(rotX) - z * sin(rotX)
                        val z1 = y * sin(rotX) + z * cos(rotX)

                        // Rotate Roll (Y)
                        val x2 = x * cos(rotY) + z1 * sin(rotY)
                        val z2 = -x * sin(rotY) + z1 * cos(rotY)

                        // Rotate Yaw (Z)
                        val x3 = x2 * cos(rotZ) - y1 * sin(rotZ)
                        val y3 = x2 * sin(rotZ) + y1 * cos(rotZ)

                        Offset(cx + x3.toFloat(), cy + y3.toFloat())
                    }

                    // Cube Edges
                    val edges = arrayOf(
                        Pair(0, 1), Pair(1, 2), Pair(2, 3), Pair(3, 0),
                        Pair(4, 5), Pair(5, 6), Pair(6, 7), Pair(7, 4),
                        Pair(0, 4), Pair(1, 5), Pair(2, 6), Pair(3, 7)
                    )

                    for (edge in edges) {
                        val p1 = projected[edge.first]
                        val p2 = projected[edge.second]
                        drawLine(
                            color = CyberPink,
                            start = p1,
                            end = p2,
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    // Center Orientation Reticle
                    val offsetX = (smoothY * 1.2f).coerceIn(-60f, 60f)
                    val offsetY = (smoothX * 1.2f).coerceIn(-50f, 50f)
                    drawCircle(
                        color = NeonGreen,
                        radius = 6.dp.toPx(),
                        center = Offset(cx + offsetX, cy + offsetY)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Telemetry Readout Grid
            Row(modifier = Modifier.fillMaxWidth()) {
                SensorValChip(label = "X (Pitch)", value = "${String.format("%.1f", gyroX)}°", color = NeonCyan, modifier = Modifier.weight(1f))
                SensorValChip(label = "Y (Roll)", value = "${String.format("%.1f", gyroY)}°", color = CyberPink, modifier = Modifier.weight(1f))
                SensorValChip(label = "Z (Yaw)", value = "${String.format("%.1f", gyroZ)}°", color = NeonGreen, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SensorValChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = TextMuted)
        Text(text = value, style = MaterialTheme.typography.labelLarge, color = color)
    }
}
