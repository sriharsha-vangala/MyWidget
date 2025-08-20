package com.example.mywidget.json

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

data class UiResources(
    val colors: Map<String, Color>,
    val fonts: Map<String, FontFamily>
)