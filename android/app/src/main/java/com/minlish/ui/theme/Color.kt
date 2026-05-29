package com.minlish.ui.theme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// 1. Định nghĩa các mã màu thô (Hex Colors) theo ảnh mẫu
val WarmBackground = Color(0xFFFFF6EB)   // Màu vàng kem ấm làm nền app
val PureWhite = Color(0xFFFFFFFF)        // Màu trắng tinh cho các khối Card
val DeepBlack = Color(0xFF000000)         // Màu đen tuyền cho Text chính và Nút nhấn
val SoftGray = Color(0xFF7F7F7F)          // Màu xám cho text phụ, sub-title
val BorderLightColor = Color(0xFFE8E2DA)  // Màu viền nhạt cho các ô Quiz/Input
val EnergeticOrange = Color(0xFFFF9F43)   // Màu cam rực rỡ cho Streak hoặc Progress
val PlayfulPurple = Color(0xFFE8E3F5)     // Màu tím nhạt pastel làm thẻ tag phụ

// 2. Map vào Light Color Scheme chuẩn của Material Design 3
val MinLishColorScheme = lightColorScheme(
    primary = DeepBlack,                  // Các thành phần hành động chính (Button, Active Icon)
    onPrimary = PureWhite,                // Màu chữ/icon nằm TRÊN nền Primary
    background = WarmBackground,          // Màu nền toàn màn hình
    onBackground = DeepBlack,             // Màu chữ nằm TRÊN nền Background
    surface = PureWhite,                  // Màu nền của các thành phần như Card, Dialog
    onSurface = DeepBlack,                // Màu chữ nằm TRÊN nền Surface
    surfaceVariant = PlayfulPurple,       // Màu nền phụ cho các thẻ đặc biệt
    onSurfaceVariant = DeepBlack,
    outline = BorderLightColor,           // Màu dùng cho các đường Border (như OutlinedButton)
    secondary = SoftGray                  // Màu phụ cho các chi tiết nhỏ
)