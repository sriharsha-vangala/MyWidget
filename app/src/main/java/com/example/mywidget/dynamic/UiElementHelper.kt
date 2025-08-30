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

    fun GlanceModifier.applyIfNotNull(modifier: GlanceModifier?) = modifier?.let { this.then(it) } ?: this

    @SuppressLint("RestrictedApi")
    fun buildModifier(attrs: Map<String, String>): GlanceModifier {
        val modifier: GlanceModifier = GlanceModifier.padding(0.dp)

        modifier.applyIfNotNull(attrs["backgroundColor"]?.let {
            val color = ColorRegistry.getColor(it)
            GlanceModifier.background(ColorProvider(color))
        })

        modifier.applyIfNotNull(attrs["width"]?.let { width ->
            parseDimension(width, true)
        })

        modifier.applyIfNotNull(attrs["height"]?.let { height ->
            parseDimension(height, false)
        })

        return modifier
    }

    fun parseDimension(value: String, isWidth: Boolean): GlanceModifier {
        return when (value) {
            "wrap_content" -> if (isWidth) GlanceModifier.wrapContentWidth() else GlanceModifier.wrapContentHeight()
            "match_parent" -> if (isWidth) GlanceModifier.fillMaxWidth() else GlanceModifier.fillMaxHeight()
            else -> value.toIntOrNull()?.let { dim -> if (isWidth) GlanceModifier.width(dim.dp) else GlanceModifier.height(dim.dp) }
        } ?: GlanceModifier
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

    fun getProgressColor(attrs: Map<String, String>): Color {
        return attrs["progressColor"]?.let { colorRef ->
            ColorRegistry.getColor(colorRef)
        } ?: Color.Green
    }

    fun getProgressBackgroundColor(attrs: Map<String, String>): Color {
        return attrs["progressBackgroundColor"]?.let { colorRef ->
            ColorRegistry.getColor(colorRef)
        } ?: Color.Gray
    }

    fun getProgressSize(attrs: Map<String, String>): Int {
        return attrs["size"]?.toIntOrNull() ?: 50
    }
}