package com.example.mywidget.dynamic.renderer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.size
import androidx.glance.text.Text
import com.caverock.androidsvg.SVG
import com.example.mywidget.core.storage.ResourceStorage
import com.example.mywidget.data.model.UiElement
import com.example.mywidget.dynamic.UiElementHelper
import java.io.File
import kotlin.text.endsWith
import kotlin.text.toIntOrNull

@Composable
fun RenderImageNode(
    context: Context,
    element: UiElement.ImageNode
) {
    val attributes = element.attributes
    val modifier = UiElementHelper.buildModifier(attributes)
    val resourceStorage = ResourceStorage(context)

    val imageName = element.src.substringBeforeLast('.')
    val imagePath = resourceStorage.getImagePath(imageName)

    val width = element.attributes["width"]?.toIntOrNull() ?: 100
    val height = element.attributes["height"]?.toIntOrNull() ?: 100
    val imageModifier = modifier.size(width.dp, height.dp)

    // Get color tint from attributes
    val imageColor = UiElementHelper.getImageColor(element.attributes)

    if (imagePath != null && File(imagePath).exists()) {
        if (imagePath.endsWith(".svg", ignoreCase = true)) {
            val svgBitmap = if (imageColor != null) {
                loadSvgAsBitmapWithColor(imagePath, width, height, imageColor.toArgb())
            } else {
                loadSvgAsBitmap(imagePath, width, height)
            }

            if (svgBitmap != null) {
                Image(
                    provider = ImageProvider(svgBitmap),
                    contentDescription = element.src,
                    modifier = imageModifier
                )
            } else {
                Text("${element.src} error", modifier = imageModifier)
            }
        } else {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            if (bitmap != null) {
                val finalBitmap = if (imageColor != null) {
                    applyColorTintToBitmap(bitmap, imageColor.toArgb())
                } else {
                    bitmap
                }

                Image(
                    provider = ImageProvider(finalBitmap),
                    contentDescription = element.src,
                    modifier = imageModifier
                )
            } else {
                Text("${element.src} Error", modifier = imageModifier)
            }
        }
    } else {
        Text("${element.src}: No such File", modifier = imageModifier)
    }
}

/**
 * Load an SVG file and render it into a Bitmap.
 */
private fun loadSvgAsBitmap(svgFilePath: String, width: Int, height: Int): Bitmap? {
    return try {
        val file = File(svgFilePath)
        if (!file.exists()) return null

        val svg = SVG.getFromInputStream(file.inputStream())
        svg.setDocumentWidth(width.toFloat())
        svg.setDocumentHeight(height.toFloat())

        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)

        // Transparent background
        canvas.drawColor(android.graphics.Color.TRANSPARENT)

        svg.renderToCanvas(canvas)
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Load an SVG file and render it into a Bitmap with a color tint.
 */
private fun loadSvgAsBitmapWithColor(
    svgFilePath: String,
    width: Int,
    height: Int,
    color: Int
): Bitmap? {
    return try {
        val file = File(svgFilePath)
        if (!file.exists()) return null

        val svg = SVG.getFromInputStream(file.inputStream())
        svg.setDocumentWidth(width.toFloat())
        svg.setDocumentHeight(height.toFloat())

        // First render the SVG to a bitmap
        val originalBitmap = createBitmap(width, height)
        val originalCanvas = Canvas(originalBitmap)
        originalCanvas.drawColor(android.graphics.Color.TRANSPARENT)
        svg.renderToCanvas(originalCanvas)

        // Then apply color filter to the rendered SVG
        val tintedBitmap = createBitmap(width, height)
        val tintedCanvas = Canvas(tintedBitmap)

        // Clear with transparent background
        tintedCanvas.drawColor(android.graphics.Color.TRANSPARENT)

        // Apply color filter and draw the original bitmap
        val paint = android.graphics.Paint()
        paint.colorFilter =
            android.graphics.PorterDuffColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
        tintedCanvas.drawBitmap(originalBitmap, 0f, 0f, paint)

        tintedBitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun applyColorTintToBitmap(bitmap: Bitmap, color: Int): Bitmap {
    val config = bitmap.config ?: Bitmap.Config.ARGB_8888
    val tintedBitmap = bitmap.copy(config, true)
    val canvas = Canvas(tintedBitmap)
    val paint = android.graphics.Paint()
    paint.colorFilter =
        android.graphics.PorterDuffColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(bitmap, 0f, 0f, paint)
    return tintedBitmap
}