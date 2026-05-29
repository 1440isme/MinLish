package com.minlish.core.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

@Composable
fun RobotIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(64.dp)) {
        val w = size.width
        val h = size.height
        // background frame
        drawRoundRect(
            color = Color(0xFFE2F1ED),
            cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())
        )
        val headWidth = w * 0.55f
        val headHeight = h * 0.45f
        val left = (w - headWidth) / 2
        val top = (h - headHeight) / 2 + 1.dp.toPx()
        
        drawRoundRect(
            color = Color(0xFF0D9488),
            topLeft = Offset(left, top),
            size = Size(headWidth, headHeight),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
        )
        drawRoundRect(
            color = Color(0xFFCCFBF1),
            topLeft = Offset(left + 4.dp.toPx(), top + 4.dp.toPx()),
            size = Size(headWidth - 8.dp.toPx(), headHeight - 8.dp.toPx()),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
        drawCircle(
            color = Color(0xFF0D9488),
            radius = 3.dp.toPx(),
            center = Offset(left + headWidth * 0.3f, top + headHeight * 0.45f)
        )
        drawCircle(
            color = Color(0xFF0D9488),
            radius = 3.dp.toPx(),
            center = Offset(left + headWidth * 0.7f, top + headHeight * 0.45f)
        )
        drawLine(
            color = Color(0xFF0F766E),
            start = Offset(w / 2, top),
            end = Offset(w / 2, top - 6.dp.toPx()),
            strokeWidth = 2.dp.toPx()
        )
        drawCircle(
            color = Color(0xFF14B8A6),
            radius = 2.5.dp.toPx(),
            center = Offset(w / 2, top - 6.dp.toPx())
        )
    }
}

@Composable
fun BookIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(64.dp)) {
        val w = size.width
        val h = size.height
        drawRoundRect(
            color = Color(0xFFFEF3C7),
            cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())
        )
        val cw = w * 0.55f
        val ch = h * 0.5f
        val cl = (w - cw) / 2
        val ct = (h - ch) / 2
        
        drawRoundRect(
            color = Color(0xFFD97706),
            topLeft = Offset(cl, ct),
            size = Size(cw, ch),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(cl + 3.dp.toPx(), ct + 2.dp.toPx()),
            size = Size(cw * 0.8f, ch * 0.85f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
        drawLine(
            color = Color(0xFFF59E0B),
            start = Offset(cl + 6.dp.toPx(), ct + 5.dp.toPx()),
            end = Offset(cl + cw - 6.dp.toPx(), ct + 5.dp.toPx()),
            strokeWidth = 1.5.dp.toPx()
        )
        drawRect(
            color = Color(0xFFEF4444),
            topLeft = Offset(cl + cw * 0.5f, ct - 2.dp.toPx()),
            size = Size(3.dp.toPx(), ch * 0.35f)
        )
    }
}

@Composable
fun StarIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(64.dp)) {
        val w = size.width
        val h = size.height
        drawRoundRect(
            color = Color(0xFFEEF2F6),
            cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())
        )
        val cx = w / 2
        val cy = h / 2
        val path = Path().apply {
            moveTo(cx, cy - 14.dp.toPx())
            lineTo(cx + 4.dp.toPx(), cy - 4.dp.toPx())
            lineTo(cx + 14.dp.toPx(), cy - 3.dp.toPx())
            lineTo(cx + 6.dp.toPx(), cy + 4.dp.toPx())
            lineTo(cx + 8.dp.toPx(), cy + 14.dp.toPx())
            lineTo(cx, cy + 8.dp.toPx())
            lineTo(cx - 8.dp.toPx(), cy + 14.dp.toPx())
            lineTo(cx - 6.dp.toPx(), cy + 4.dp.toPx())
            lineTo(cx - 14.dp.toPx(), cy - 3.dp.toPx())
            lineTo(cx - 4.dp.toPx(), cy - 4.dp.toPx())
            close()
        }
        drawPath(path, color = Color(0xFFEAB308))
    }
}

@Composable
fun GiraffeMascot(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Draw the lovely giraffe with a blue hoodie and skateboard from Rovio!
        // Blue Hoodie body
        drawRoundRect(
            color = Color(0xFF3B82F6),
            topLeft = Offset(w * 0.25f, h * 0.58f),
            size = Size(w * 0.5f, h * 0.35f),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
        )
        // Hoodie hood
        drawCircle(
            color = Color(0xFF2563EB),
            radius = 12.dp.toPx(),
            center = Offset(w * 0.5f, h * 0.58f)
        )
        
        // Giraffe Neck
        val neckW = w * 0.14f
        val neckH = h * 0.38f
        drawRoundRect(
            color = Color(0xFFFBBF24),
            topLeft = Offset((w - neckW) / 2, h * 0.25f),
            size = Size(neckW, neckH)
        )
        // Spots on neck
        drawCircle(color = Color(0xFFD97706), radius = 3.dp.toPx(), center = Offset(w * 0.48f, h * 0.35f))
        drawCircle(color = Color(0xFFD97706), radius = 2.dp.toPx(), center = Offset(w * 0.53f, h * 0.44f))

        // Head oval
        drawOval(
            color = Color(0xFFFBBF24),
            topLeft = Offset(w * 0.37f, h * 0.14f),
            size = Size(w * 0.26f, h * 0.18f)
        )
        
        // Snout
        drawOval(
            color = Color(0xFFFDE047),
            topLeft = Offset(w * 0.39f, h * 0.22f),
            size = Size(w * 0.22f, h * 0.09f)
        )
        // Nostrils
        drawCircle(color = Color(0xFFB45309), radius = 1.5.dp.toPx(), center = Offset(w * 0.47f, h * 0.26f))
        drawCircle(color = Color(0xFFB45309), radius = 1.5.dp.toPx(), center = Offset(w * 0.53f, h * 0.26f))

        // Eyes
        drawCircle(color = Color.White, radius = 7.dp.toPx(), center = Offset(w * 0.44f, h * 0.18f))
        drawCircle(color = Color.Black, radius = 3.dp.toPx(), center = Offset(w * 0.45f, h * 0.18f))
        drawCircle(color = Color.White, radius = 7.dp.toPx(), center = Offset(w * 0.56f, h * 0.18f))
        drawCircle(color = Color.Black, radius = 3.dp.toPx(), center = Offset(w * 0.55f, h * 0.18f))

        // Horns
        drawLine(color = Color(0xFFB45309), start = Offset(w * 0.46f, h * 0.14f), end = Offset(w * 0.44f, h * 0.08f), strokeWidth = 2.5f)
        drawCircle(color = Color(0xFFD97706), radius = 3.dp.toPx(), center = Offset(w * 0.44f, h * 0.08f))
        drawLine(color = Color(0xFFB45309), start = Offset(w * 0.54f, h * 0.14f), end = Offset(w * 0.56f, h * 0.08f), strokeWidth = 2.5f)
        drawCircle(color = Color(0xFFD97706), radius = 3.dp.toPx(), center = Offset(w * 0.56f, h * 0.08f))

        // Ear left
        val pathL = Path().apply {
            moveTo(w * 0.37f, h * 0.16f)
            lineTo(w * 0.28f, h * 0.13f)
            lineTo(w * 0.35f, h * 0.19f)
            close()
        }
        drawPath(pathL, color = Color(0xFFFBBF24))

        // Ear right
        val pathR = Path().apply {
            moveTo(w * 0.63f, h * 0.16f)
            lineTo(w * 0.72f, h * 0.13f)
            lineTo(w * 0.65f, h * 0.19f)
            close()
        }
        drawPath(pathR, color = Color(0xFFFBBF24))

        // Skateboard (bottom right)
        drawRoundRect(
            color = Color(0xFF1F2937),
            topLeft = Offset(w * 0.65f, h * 0.65f),
            size = Size(w * 0.12f, h * 0.28f),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
        )
        // Wheels
        drawCircle(color = Color(0xFFE5E7EB), radius = 3.dp.toPx(), center = Offset(w * 0.64f, h * 0.72f))
        drawCircle(color = Color(0xFFE5E7EB), radius = 3.dp.toPx(), center = Offset(w * 0.78f, h * 0.72f))
        drawCircle(color = Color(0xFFE5E7EB), radius = 3.dp.toPx(), center = Offset(w * 0.64f, h * 0.86f))
        drawCircle(color = Color(0xFFE5E7EB), radius = 3.dp.toPx(), center = Offset(w * 0.78f, h * 0.86f))
    }
}

@Composable
fun BeeMascot(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        val cx = w / 2
        val cy = h / 2 + 8.dp.toPx()
        
        // Translucent wings (Rovio design)
        drawOval(
            color = Color(0x9093C5FD),
            topLeft = Offset(cx - 48.dp.toPx(), cy - 38.dp.toPx()),
            size = Size(36.dp.toPx(), 22.dp.toPx())
        )
        drawOval(
            color = Color(0x9093C5FD),
            topLeft = Offset(cx + 12.dp.toPx(), cy - 38.dp.toPx()),
            size = Size(36.dp.toPx(), 22.dp.toPx())
        )
        
        // Yellow capsule body
        drawOval(
            color = Color(0xFFFBBF24),
            topLeft = Offset(cx - 24.dp.toPx(), cy - 14.dp.toPx()),
            size = Size(48.dp.toPx(), 40.dp.toPx())
        )
        // Black Stripes
        drawRect(
            color = Color(0xFF1F2937),
            topLeft = Offset(cx - 10.dp.toPx(), cy - 14.dp.toPx()),
            size = Size(10.dp.toPx(), 39.5.dp.toPx())
        )
        drawRect(
            color = Color(0xFF1F2937),
            topLeft = Offset(cx + 8.dp.toPx(), cy - 11.dp.toPx()),
            size = Size(10.dp.toPx(), 34.dp.toPx())
        )
        
        // Cute face
        drawCircle(
            color = Color(0xFFFEF08A),
            radius = 14.dp.toPx(),
            center = Offset(cx - 18.dp.toPx(), cy + 6.dp.toPx())
        )
        // Glowing cyan eye
        drawCircle(
            color = Color(0xFF06B6D4),
            radius = 4.5.dp.toPx(),
            center = Offset(cx - 24.dp.toPx(), cy + 4.dp.toPx())
        )
        drawCircle(
            color = Color.White,
            radius = 1.5.dp.toPx(),
            center = Offset(cx - 26.dp.toPx(), cy + 2.dp.toPx())
        )
        
        // Stinger flame
        drawCircle(
            color = Color(0xFFF97316),
            radius = 5.dp.toPx(),
            center = Offset(cx + 25.dp.toPx(), cy + 6.dp.toPx())
        )
        
        // Antennas
        drawLine(
            color = Color(0xFF1F2937),
            start = Offset(cx - 18.dp.toPx(), cy - 8.dp.toPx()),
            end = Offset(cx - 24.dp.toPx(), cy - 22.dp.toPx()),
            strokeWidth = 2.5f
        )
        drawCircle(
            color = Color(0xFFF1F5F9),
            radius = 2.5.dp.toPx(),
            center = Offset(cx - 24.dp.toPx(), cy - 22.dp.toPx())
        )
    }
}
