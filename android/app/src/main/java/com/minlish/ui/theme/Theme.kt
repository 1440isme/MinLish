package com.minlish.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Color System
val BgWarm = Color(0xFFFFF9F2)        // Nền kem ấm áp xuyên suốt các màn hình
val SurfaceWhite = Color(0xFFFFFFFF)  // Thẻ Card trắng tinh
val TextPrimary = Color(0xFF000000)   // Chữ đen đậm tương phản cao
val TextSecondary = Color(0xFF7F7F7F) // Chữ xám cho thông tin phụ
val AccentOrange = Color(0xFFFF9F43)  // Màu cam rực rỡ cho Streak/Progress
val TagPurple = Color(0xFFE8E3F5)     // Màu tím pastel cho thẻ/thanh tiến trình

// Shape System (Góc bo lớn đặc trưng)
val LargeCardShape = RoundedCornerShape(32.dp)
val ButtonCapsuleShape = RoundedCornerShape(50.dp)
val SmallItemShape = RoundedCornerShape(16.dp)

@Composable
fun MinLishTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MinLishColorScheme,
        typography = MinLishTypography,
        content = content
    )
}
