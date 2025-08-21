package com.example.mywidget.json

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

object ColorRegistry {
    private val colors: MutableMap<String, Color> = mutableMapOf()

    fun registerColor(name: String, hex: String) {
        try {
            colors[name] = Color(hex.toColorInt())
        } catch (_: Exception) {
        }
    }

    fun getColor(name: String): Color = colors[name] ?: Color.White
}

