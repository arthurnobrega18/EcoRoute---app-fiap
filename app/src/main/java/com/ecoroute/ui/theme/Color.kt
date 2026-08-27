package com.ecoroute.ui.theme

import androidx.compose.ui.graphics.Color

val EcoGreen80 = Color(0xFFA8D5BA)
val EcoGreenGrey80 = Color(0xFFC1CCC1)
val EcoBlue80 = Color(0xFFA6D4E0)

val EcoGreen40 = Color(0xFF2E7D4F)
val EcoGreenGrey40 = Color(0xFF4C6B4F)
val EcoBlue40 = Color(0xFF2A6F7F)

// Paleta de app (fundo, superfícies e texto), usada diretamente pelas telas
// em vez do ColorScheme padrão do Material3 — ver EcoRouteTheme.
val AppBackground = Color(0xFFFAF9F5)
val AppSurface = Color(0xFFFFFFFF)
val AppInk = Color(0xFF1C231F)
val AppInkSoft = Color(0xFF4B554E)
val AppInkMuted = Color(0xFF8A9089)
val AppBorder = Color(0xFFE7E4DA)
val AppGreenTint = Color(0xFFE4F2E9)
val AppBlueTint = Color(0xFFE1EEF1)

// Escala de cor semântica por nível de emissão de CO2 (ver EmissionTier),
// usada em ComparisonScreen, HistoryScreen e InsightsScreen.
val EmissionZero = EcoGreen40
val EmissionZeroTint = AppGreenTint
val EmissionLow = Color(0xFFA9791A)
val EmissionLowTint = Color(0xFFF6EFDD)
val EmissionMid = Color(0xFFC96F2C)
val EmissionMidTint = Color(0xFFFBECDF)
val EmissionHigh = Color(0xFFB24628)
val EmissionHighTint = Color(0xFFFBE4DD)
