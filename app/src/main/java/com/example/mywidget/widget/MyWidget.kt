package com.example.mywidget.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.caverock.androidsvg.SVG
import com.example.mywidget.core.WidgetPackageManager
import com.example.mywidget.core.storage.ResourceStorage
import com.example.mywidget.dynamic.UiElementHelper
import com.example.mywidget.json.UiElement
import kotlinx.coroutines.runBlocking
import java.io.File
import androidx.core.graphics.createBitmap
import androidx.glance.unit.ColorProvider
import com.example.mywidget.dynamic.UiElementHelper.getTextColor
import com.example.mywidget.fonts.FontRegistry

class MyWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val packageManager = WidgetPackageManager.getInstance(context)
        provideContent {
            WidgetContent(packageManager, context)
        }
    }

    @Composable
    fun WidgetContent(packageManager: WidgetPackageManager, context: Context) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            // Get UI element from package manager
            val uiElement = runBlocking {
                packageManager.getCurrentUI()
            }

            if (uiElement != null) {
                RenderUiElement(uiElement, context)
            } else {
                // Fallback UI when no configuration is available
                FallbackContent()
            }
        }
    }

    @Composable
    fun FallbackContent() {
        Column(
            modifier = GlanceModifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Widget",
                modifier = GlanceModifier.padding(bottom = 8.dp)
            )
            Text(
                text = "No UI configuration loaded",
                modifier = GlanceModifier.padding(4.dp)
            )
        }
    }

    @Composable
    fun WithMargin(attributes: Map<String, String>, content: @Composable () -> Unit) {
        val margins = UiElementHelper.calculateMargins(attributes)
        Box(
            modifier = GlanceModifier.padding(
                start = margins.start.dp,
                top = margins.top.dp,
                end = margins.end.dp,
                bottom = margins.bottom.dp
            )
        ) {
            content()
        }
    }

    @Composable
    fun RenderUiElement(element: UiElement, context: Context) {
        val attributes = element.attributes
        WithMargin(attributes) {
            val modifier = UiElementHelper.buildModifier(attributes)
            when (element) {
                is UiElement.Container -> {
                    when (element.layout) {
                        UiElement.Layout.Row -> Row(
                            modifier = modifier,
                            verticalAlignment = UiElementHelper.getVerticalAlignment(attributes),
                            horizontalAlignment = UiElementHelper.getHorizontalAlignment(attributes)
                        ) {
                            RenderChildren(element, context)
                        }

                        UiElement.Layout.Column -> Column(
                            modifier = modifier,
                            horizontalAlignment = UiElementHelper.getHorizontalAlignment(attributes),
                            verticalAlignment = UiElementHelper.getVerticalAlignment(attributes)
                        ) {
                            RenderChildren(element, context)
                        }

                        UiElement.Layout.Stack -> Box(
                            modifier = modifier,
                            contentAlignment = UiElementHelper.getAlignment(attributes)
                        ) {
                            RenderChildren(element, context)
                        }
                    }
                }

                is UiElement.TextNode -> {
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

                is UiElement.ImageNode -> {
                    RenderImage(element, modifier, context)
                }
            }
        }
    }

    @Composable
    fun RenderChildren(uiElement: UiElement.Container, context: Context) {
        uiElement.children.forEach { RenderUiElement(it, context) }
    }

    @Composable
    fun RenderImage(
        element: UiElement.ImageNode,
        modifier: GlanceModifier,
        context: Context
    ) {
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
    private fun loadSvgAsBitmapWithColor(svgFilePath: String, width: Int, height: Int, color: Int): Bitmap? {
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
            paint.colorFilter = android.graphics.PorterDuffColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
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
        paint.colorFilter = android.graphics.PorterDuffColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return tintedBitmap
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
}