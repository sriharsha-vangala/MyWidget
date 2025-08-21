package com.example.mywidget.dynamic

import android.annotation.SuppressLint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.background
import androidx.glance.layout.width
import androidx.glance.unit.ColorProvider
import androidx.core.graphics.toColorInt
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.wrapContentHeight
import androidx.glance.layout.wrapContentWidth
import com.example.mywidget.json.ColorRegistry

object UiElementHelper {
    @SuppressLint("RestrictedApi")
    fun buildModifier(attrs: Map<String, String>): GlanceModifier {
        var modifier: GlanceModifier = GlanceModifier.padding(0.dp)

        attrs["background"]?.let {
            try {
                val color = ColorRegistry.getColor(it)
                modifier = modifier.background(ColorProvider(color))
            } catch (_: Exception) { }
        }

        attrs["width"]?.let {
            when (it) {
                "wrap_content" -> modifier = modifier.wrapContentWidth()
                "match_parent" -> modifier = modifier.fillMaxWidth()
                else -> it.toIntOrNull()?.let { w -> modifier = modifier.width(w.dp) }
            }
        }

        attrs["height"]?.let {
            when (it) {
                "wrap_content" -> modifier = modifier.wrapContentHeight()
                "match_parent" -> modifier = modifier.fillMaxHeight()
                else -> it.toIntOrNull()?.let { h -> modifier = modifier.height(h.dp) }
            }
        }

        return modifier
    }

    fun getTextColor(attrs: Map<String, String?>): Color {
        return attrs["color"]?.let { Color(it.toColorInt()) } ?: Color.Red
    }
}