package com.example.mywidget.dynamic.renderer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.mywidget.data.model.UiElement
import com.example.mywidget.dynamic.UiElementHelper
import com.example.mywidget.dynamic.UiElementHelper.getTextColor
import com.example.mywidget.fonts.FontRegistry
import java.io.File

@Composable
fun RenderTextNode(context: Context, element: UiElement.TextNode) {
    val attributes = element.attributes
    val modifier = UiElementHelper.buildModifier(attributes)
    val fontName = UiElementHelper.getFontName(attributes)
    val fontSize = UiElementHelper.getFontSize(attributes)
    val textColor = getTextColor(attributes)

    if (fontName != null && fontSize != null) {
        val fontPath = FontRegistry.getFontPath(fontName)
        if (fontPath != null) {
            GlanceTextFontPath(
                text = element.text,
                fontPath = fontPath,
                fontSize = fontSize,
                modifier = modifier,
                color = textColor,
                context = context
            )
        } else {
            Text(
                text = element.text,
                modifier = modifier,
                style = TextStyle(
                    color = ColorProvider(textColor),
                    textAlign = UiElementHelper.getTextAlignment(attributes)
                )
            )
        }
    } else {
        Text(
            text = element.text,
            modifier = modifier,
            style = TextStyle(
                color = ColorProvider(textColor),
                textAlign = UiElementHelper.getTextAlignment(attributes)
            )
        )
    }
}

@Composable
fun GlanceTextFontPath(
    text: String,
    fontPath: String?,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: GlanceModifier = GlanceModifier,
    color: Color = Color.Black,
    context: Context
) {
    val bitmap = remember(text, fontPath, fontSize, color) {
        context.textAsBitmapFontPath(
            text = text,
            fontSize = fontSize,
            color = color,
            fontPath = fontPath
        )
    }
    Image(
        modifier = modifier,
        provider = ImageProvider(bitmap),
        contentDescription = null,
    )
}

private fun Context.textAsBitmapFontPath(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    color: Color = Color.Black,
    fontResId: Int? = null,
    fontPath: String? = null
): Bitmap {
    val paint = android.text.TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    paint.textSize = fontSize.value * resources.displayMetrics.scaledDensity
    paint.color = color.toArgb()

    // Load typeface with error handling
    paint.typeface = try {
        when {
            fontPath != null && File(fontPath).exists() -> {
                android.graphics.Typeface.createFromFile(fontPath)
            }

            fontResId != null -> {
                androidx.core.content.res.ResourcesCompat.getFont(this, fontResId)
            }

            else -> android.graphics.Typeface.DEFAULT
        }
    } catch (e: Exception) {
        // Fallback to default typeface if custom font loading fails
        android.graphics.Typeface.DEFAULT
    }

    val baseline = -paint.ascent()
    val width = (paint.measureText(text)).toInt().coerceAtLeast(1)
    val height = (baseline + paint.descent()).toInt().coerceAtLeast(1)

    // Create bitmap with error handling
    val image = try {
        createBitmap(width, height)
    } catch (e: OutOfMemoryError) {
        // Create smaller bitmap if memory is low
        createBitmap(1, 1)
    }

    val canvas = Canvas(image)
    canvas.drawText(text, 0f, baseline, paint)
    return image
}