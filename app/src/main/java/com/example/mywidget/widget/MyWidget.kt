package com.example.mywidget.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
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
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.mywidget.datastore.getUIJsonString
import com.example.mywidget.dynamic.UiElement
import com.example.mywidget.dynamic.UiElementHelper

class MyWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val uiJsonString = getUIJsonString(context) ?: "{}"
        provideContent {
            WidgetContent(uiJsonString)
        }
    }

    @Composable
    fun WidgetContent(uiJsonString: String) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color.White)),
            contentAlignment = Alignment.Center
        ) {
            val elements = UiElementHelper.parseUiElement(uiJsonString)
            val root = elements.first { it.parentId == null }
            val childrenMap = UiElementHelper.buildChildrenMap(elements)
            RenderElement(root, childrenMap)
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    fun RenderElement(element: UiElement, childrenMap: Map<String?, List<UiElement>>) {
        val children = childrenMap[element.id] ?: emptyList()
        WithMargin(element.attributes) {
            val modifier = UiElementHelper.buildModifier(element.attributes)
            when (element) {
                is UiElement.Column -> {
                    Column(modifier = modifier) {
                        children.forEach { RenderElement(it, childrenMap) }
                    }
                }

                is UiElement.Row -> {
                    Row(modifier = modifier) {
                        children.forEach { RenderElement(it, childrenMap) }
                    }
                }

                is UiElement.Stack -> {
                    Box(modifier = modifier) {
                        children.forEach { RenderElement(it, childrenMap) }
                    }
                }

                is UiElement.Text -> {
                    Text(
                        text = element.text,
                        modifier = modifier,
                        style = TextStyle(
                            color = ColorProvider(
                                color = UiElementHelper.getTextColor(element.attributes)
                            )
                        )
                    )
                }
            }
        }
    }

    @Composable
    fun WithMargin(attrs: Map<String, String?>, content: @Composable () -> Unit) {
        val margin = attrs["margin"]?.toIntOrNull()?.dp ?: 0.dp
        if (margin > 0.dp) {
            Box(modifier = GlanceModifier.padding(margin)) {
                content()
            }
        } else {
            content()
        }
    }
}