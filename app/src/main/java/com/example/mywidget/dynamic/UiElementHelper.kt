package com.example.mywidget.dynamic

import android.annotation.SuppressLint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.glance.layout.Alignment
import androidx.glance.text.TextAlign

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

    /**
     * Get text alignment from attributes
     */
    fun getTextAlignment(attrs: Map<String, String>): TextAlign {
        return when (attrs["align"]) {
            "start", "left" -> TextAlign.Start
            "center" -> TextAlign.Center
            "end", "right" -> TextAlign.End
            else -> TextAlign.Start
        }
    }

    /**
     * Get image color/tint from attributes
     */
    fun getImageColor(attrs: Map<String, String>): Color? {
        return attrs["tint"]?.let { colorRef ->
            ColorRegistry.getColor(colorRef)
        } ?: attrs["color"]?.let { colorRef ->
            ColorRegistry.getColor(colorRef)
        }
    }

    /**
     * Get horizontal alignment for containers
     */
    fun getHorizontalAlignment(attrs: Map<String, String>): Alignment.Horizontal {
        return when (attrs["horizontalAlign"]) {
            "start" -> Alignment.Start
            "center" -> Alignment.CenterHorizontally
            "end" -> Alignment.End
            else -> Alignment.Start
        }
    }

    /**
     * Get vertical alignment for containers
     */
    fun getVerticalAlignment(attrs: Map<String, String>): Alignment.Vertical {
        return when (attrs["verticalAlign"]) {
            "top" -> Alignment.Top
            "center" -> Alignment.CenterVertically
            "bottom" -> Alignment.Bottom
            else -> Alignment.Top
        }
    }

    /**
     * Get alignment for Box/Stack layouts
     */
    fun getAlignment(attrs: Map<String, String>): Alignment {
        return when (attrs["align"]) {
            "topStart" -> Alignment.TopStart
            "topCenter" -> Alignment.TopCenter
            "topEnd" -> Alignment.TopEnd
            "centerStart" -> Alignment.CenterStart
            "center" -> Alignment.Center
            "centerEnd" -> Alignment.CenterEnd
            "bottomStart" -> Alignment.BottomStart
            "bottomCenter" -> Alignment.BottomCenter
            "bottomEnd" -> Alignment.BottomEnd
            "top" -> Alignment.TopCenter
            "bottom" -> Alignment.BottomCenter
            "start" -> Alignment.CenterStart
            "end" -> Alignment.CenterEnd
            else -> Alignment.TopStart
        }
    }

    fun getFontName(attrs: Map<String, String>): String? {
        return attrs["font"]
    }

    fun getFontSize(attrs: Map<String, String>): androidx.compose.ui.unit.TextUnit? {
        val fontSizeStr = attrs["fontSize"] ?: return null
        return try {
            fontSizeStr.toFloatOrNull()?.sp
        } catch (e: Exception) {
            null
        }
    }
}