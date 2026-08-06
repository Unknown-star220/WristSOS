package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import com.example.data.model.OledElement
import com.example.data.model.OledElementType
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberPink
import com.example.ui.theme.NeonCyan
import kotlin.math.roundToInt

/**
 * 128x64 Monochrome OLED Display Canvas.
 * Supports dragging elements around the pixel grid to position clock, step counter, battery, date.
 */
@Composable
fun OledCanvas(
    elements: List<com.example.data.model.OledElement>,
    onPositionChanged: (id: String, newX: Int, newY: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(280.dp) // Scaled 128x64 OLED layout
            .height(140.dp)
            .shadow(12.dp, RoundedCornerShape(8.dp), spotColor = NeonCyan)
            .clip(RoundedCornerShape(8.dp))
            .background(CyberBlack)
            .border(2.dp, NeonCyan, RoundedCornerShape(8.dp))
            .testTag("oled_designer_canvas")
    ) {
        // OLED Pixel Grid & Glass Frame
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacing = 8.dp.toPx()
            val gridCols = (size.width / gridSpacing).toInt()
            val gridRows = (size.height / gridSpacing).toInt()

            for (i in 0..gridCols) {
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(i * gridSpacing, 0f),
                    end = Offset(i * gridSpacing, size.height)
                )
            }
            for (j in 0..gridRows) {
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(0f, j * gridSpacing),
                    end = Offset(size.width, j * gridSpacing)
                )
            }
        }

        // Draggable Elements
        elements.forEach { elem ->
            val scaleFactor = 2.1f // Map 128x64 coordinates to ~280x140 dp container
            val xDp = (elem.x * scaleFactor).dp
            val yDp = (elem.y * scaleFactor).dp

            Box(
                modifier = Modifier
                    .offset { IntOffset(xDp.toPx().roundToInt(), yDp.toPx().roundToInt()) }
                    .border(
                        width = 1.dp,
                        color = NeonCyan.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(2.dp)
                    )
                    .background(CyberBlack.copy(alpha = 0.8f))
                    .padding(2.dp)
                    .pointerInput(elem.id) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val newX = ((xDp.toPx() + dragAmount.x) / scaleFactor / density).toInt()
                            val newY = ((yDp.toPx() + dragAmount.y) / scaleFactor / density).toInt()
                            onPositionChanged(elem.id, newX, newY)
                        }
                    }
            ) {
                Text(
                    text = elem.text,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = (elem.fontSize * 0.9f).sp,
                    color = Color.White
                )
            }
        }
    }
}
