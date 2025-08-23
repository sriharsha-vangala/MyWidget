package com.example.mywidget.dynamic

import android.annotation.SuppressLint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.background
import androidx.glance.layout.width
import androidx.glance.unit.ColorProvider
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

        // Handle background color with color registry support
        attrs["backgroundColor"]?.let { colorRef ->
            val color = ColorRegistry.getColor(colorRef)
            modifier = modifier.background(ColorProvider(color))
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
        return attrs["color"]?.let {
            ColorRegistry.getColor(it)
        } ?: Color.Black
    }

    data class MarginValues(
        val top: Int,
        val bottom: Int,
        val start: Int,
        val end: Int
    )

    fun calculateMargins(attrs: Map<String, String>): MarginValues {
        val marginTop = attrs["marginTop"]?.toIntOrNull()
        val marginBottom = attrs["marginBottom"]?.toIntOrNull()
        val marginStart = attrs["marginStart"]?.toIntOrNull()
        val marginEnd = attrs["marginEnd"]?.toIntOrNull()

        val marginVertical = attrs["marginVertical"]?.toIntOrNull()
        val marginHorizontal = attrs["marginHorizontal"]?.toIntOrNull()

        val margin = attrs["margin"]?.toIntOrNull()

        // Calculate final margin values with priority order:
        // 1. Individual sides (highest priority)
        // 2. Vertical/Horizontal margins
        // 3. General margin (lowest priority)

        val finalTop = marginTop ?: marginVertical ?: margin ?: 0
        val finalBottom = marginBottom ?: marginVertical ?: margin ?: 0
        val finalStart = marginStart ?: marginHorizontal ?: margin ?: 0
        val finalEnd = marginEnd ?: marginHorizontal ?: margin ?: 0

        return MarginValues(finalTop, finalBottom, finalStart, finalEnd)
    }
}