package com.example.wrenchempire.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NesColorScheme = darkColorScheme(
    primary          = NesYellow,
    onPrimary        = NesBlack,
    secondary        = NesOrange,
    onSecondary      = NesBlack,
    tertiary         = NesGreen,
    onTertiary       = NesBlack,
    background       = NesNearBlack,
    onBackground     = NesWhite,
    surface          = NesDarkGray,
    onSurface        = NesWhite,
    surfaceVariant   = NesMidGray,
    onSurfaceVariant = NesLightGray,
    error            = NesRed,
    onError          = NesWhite
)

@Composable
fun WrenchEmpireTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NesColorScheme,
        typography  = NesTypography,
        content     = content
    )
}
