package com.example.wrenchempire.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.wrenchempire.R

val PressStart2P = FontFamily(
    Font(R.font.press_start_2p, FontWeight.Normal)
)

val NesTypography = Typography(
    displayLarge  = TextStyle(fontFamily = PressStart2P, fontSize = 20.sp, color = NesWhite),
    displayMedium = TextStyle(fontFamily = PressStart2P, fontSize = 16.sp, color = NesWhite),
    displaySmall  = TextStyle(fontFamily = PressStart2P, fontSize = 14.sp, color = NesWhite),
    headlineLarge = TextStyle(fontFamily = PressStart2P, fontSize = 14.sp, color = NesYellow),
    headlineMedium= TextStyle(fontFamily = PressStart2P, fontSize = 12.sp, color = NesYellow),
    headlineSmall = TextStyle(fontFamily = PressStart2P, fontSize = 11.sp, color = NesYellow),
    titleLarge    = TextStyle(fontFamily = PressStart2P, fontSize = 12.sp, color = NesWhite),
    titleMedium   = TextStyle(fontFamily = PressStart2P, fontSize = 11.sp, color = NesWhite),
    titleSmall    = TextStyle(fontFamily = PressStart2P, fontSize = 10.sp, color = NesWhite),
    bodyLarge     = TextStyle(fontFamily = PressStart2P, fontSize = 10.sp, color = NesWhite),
    bodyMedium    = TextStyle(fontFamily = PressStart2P, fontSize = 9.sp,  color = NesWhite),
    bodySmall     = TextStyle(fontFamily = PressStart2P, fontSize = 8.sp,  color = NesLightGray),
    labelLarge    = TextStyle(fontFamily = PressStart2P, fontSize = 11.sp, color = NesYellow),
    labelMedium   = TextStyle(fontFamily = PressStart2P, fontSize = 10.sp, color = NesYellow),
    labelSmall    = TextStyle(fontFamily = PressStart2P, fontSize = 8.sp,  color = NesOrange),
)
