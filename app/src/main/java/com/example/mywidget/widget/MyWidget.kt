package com.example.mywidget.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
import com.caverock.androidsvg.SVG
import com.example.mywidget.core.WidgetPackageManager
import com.example.mywidget.core.storage.ResourceStorage
import com.example.mywidget.dynamic.UiElementHelper
import com.example.mywidget.json.UiElement
import kotlinx.coroutines.runBlocking
import java.io.File
import androidx.core.graphics.createBitmap
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.mywidget.dynamic.UiElementHelper.getTextColor

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
                    Text(
                        text = element.text,
                        modifier = modifier,
                        style = TextStyle(
                            color = ColorProvider(getTextColor(attributes)),
                            textAlign = UiElementHelper.getTextAlignment(attributes)
                        )
                    )
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

        if (imagePath != null && File(imagePath).exists()) {
            if (imagePath.endsWith(".svg", ignoreCase = true)) {
                val svgBitmap = loadSvgAsBitmap(imagePath, width, height)
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
                    Image(
                        provider = ImageProvider(bitmap),
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
}