package com.example.mywidget.dynamic

import android.annotation.SuppressLint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.background
import androidx.glance.layout.width
import androidx.glance.unit.ColorProvider
import kotlinx.serialization.json.Json
import androidx.core.graphics.toColorInt
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.wrapContentHeight
import androidx.glance.layout.wrapContentWidth

object UiElementHelper {
    val jsonParser = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }

    fun parseUiElement(json: String): List<UiElement> {
        return jsonParser.decodeFromString<List<UiElement>>(json)
    }

    fun buildChildrenMap(elements: List<UiElement>): Map<String?, List<UiElement>> {
        return elements.groupBy { it.parentId }
    }

    @SuppressLint("RestrictedApi")
    fun buildModifier(attrs: Map<String, String?>): GlanceModifier {
        var modifier: GlanceModifier = GlanceModifier.padding(0.dp)

        attrs["background"]?.let {
            try {
                modifier = modifier.background(ColorProvider(Color(it.toColorInt())))
            } catch (_: Exception) {
            }
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

        attrs["padding"]?.toIntOrNull()?.let { p ->
            modifier = modifier.padding(p.dp)
        }

        return modifier
    }

    fun getTextColor(attrs: Map<String, String?>): Color {
        return attrs["color"]?.let { Color(it.toColorInt()) } ?: Color.Red
    }
}