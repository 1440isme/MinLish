package com.minlish.ui.theme


import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Định nghĩa Typography chuẩn Material 3 tùy biến cho MinLish App
val MinLishTypography = Typography(

    // Dùng cho tiêu đề siêu lớn (Ví dụ: "Welcome Back to MinLish")
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),

    // Dùng cho tiêu đề màn hình chính (Ví dụ: "Hello, Nixtio!", "Choose the correct answer")
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),

    // Dùng cho tiêu đề của các thẻ Card (Ví dụ: "Daily Quiz", "Weekly progress")
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),

    // Dùng cho chữ bên trong các nút bấm (Ví dụ: "Log in", "Wings", "I got it!")
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),

    // Dùng cho nội dung văn bản thông thường hoặc câu hỏi phụ
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),

    // Dùng cho các đoạn ghi chú nhỏ, text phụ màu xám (Ví dụ: "Time left: 04:57", "days used")
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )
)