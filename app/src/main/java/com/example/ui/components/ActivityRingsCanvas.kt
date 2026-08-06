package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberPink
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun ActivityRingsCanvas(
    steps: Int,
    stepGoal: Int = 10000,
    activeMinutes: Int,
    activeGoalMinutes: Int = 60,
    calories: Int,
    calorieGoal: Int = 500,
    modifier: Modifier = Modifier
) {
    val stepsProgress by animateFloatAsState(
        targetValue = (steps.toFloat() / stepGoal).coerceIn(0f, 1f),
        animationSpec = tween(800), label = "steps_ring"
    )
    val activeProgress by animateFloatAsState(
        targetValue = (activeMinutes.toFloat() / activeGoalMinutes).coerceIn(0f, 1f),
        animationSpec = tween(800), label = "active_ring"
    )
    val caloriesProgress by animateFloatAsState(
        targetValue = (calories.toFloat() / calorieGoal).coerceIn(0f, 1f),
        animationSpec = tween(800), label = "calories_ring"
    )

    CyberCard(modifier = modifier.testTag("activity_rings_card")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(140.dp)) {
                    val stroke = 12.dp.toPx()
                    val center = size.width / 2f

                    // Outer Ring: Steps (Neon Cyan)
                    val r1 = (size.width / 2f) - stroke / 2f
                    drawArc(
                        color = NeonCyan.copy(alpha = 0.15f),
                        startAngle = 0f, sweepAngle = 360f, useCenter = false,
                        topLeft = Offset(center - r1, center - r1), size = Size(r1 * 2, r1 * 2),
                        style = Stroke(stroke)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(listOf(NeonCyan, NeonGreen)),
                        startAngle = -90f, sweepAngle = stepsProgress * 360f, useCenter = false,
                        topLeft = Offset(center - r1, center - r1), size = Size(r1 * 2, r1 * 2),
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )

                    // Middle Ring: Active Time (Cyber Pink)
                    val r2 = r1 - stroke - 4.dp.toPx()
                    drawArc(
                        color = CyberPink.copy(alpha = 0.15f),
                        startAngle = 0f, sweepAngle = 360f, useCenter = false,
                        topLeft = Offset(center - r2, center - r2), size = Size(r2 * 2, r2 * 2),
                        style = Stroke(stroke)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(listOf(CyberPink, NeonCyan)),
                        startAngle = -90f, sweepAngle = activeProgress * 360f, useCenter = false,
                        topLeft = Offset(center - r2, center - r2), size = Size(r2 * 2, r2 * 2),
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )

                    // Inner Ring: Calories (Neon Green)
                    val r3 = r2 - stroke - 4.dp.toPx()
                    drawArc(
                        color = NeonGreen.copy(alpha = 0.15f),
                        startAngle = 0f, sweepAngle = 360f, useCenter = false,
                        topLeft = Offset(center - r3, center - r3), size = Size(r3 * 2, r3 * 2),
                        style = Stroke(stroke)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(listOf(NeonGreen, CyberPink)),
                        startAngle = -90f, sweepAngle = caloriesProgress * 360f, useCenter = false,
                        topLeft = Offset(center - r3, center - r3), size = Size(r3 * 2, r3 * 2),
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(stepsProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "GOAL",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                RingMetricRow(label = "Steps", value = "$steps / $stepGoal", color = NeonCyan)
                Spacer(modifier = Modifier.height(10.dp))
                RingMetricRow(label = "Active Time", value = "${activeMinutes}m / ${activeGoalMinutes}m", color = CyberPink)
                Spacer(modifier = Modifier.height(10.dp))
                RingMetricRow(label = "Calories", value = "$calories / $calorieGoal kcal", color = NeonGreen)
            }
        }
    }
}

@Composable
private fun RingMetricRow(label: String, value: String, color: Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = TextMuted)
        Text(text = value, style = MaterialTheme.typography.labelLarge, color = color)
    }
}
