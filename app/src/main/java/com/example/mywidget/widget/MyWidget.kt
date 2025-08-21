package com.example.mywidget.widget

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
import com.example.mywidget.dynamic.UiElementHelper
import com.example.mywidget.json.UiElement
import com.example.mywidget.json.UiParser

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
            val elements = UiParser.parseUiJsonMap(jsonString = uiJsonString)
            RenderUiElement(elements)
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

    @Composable
    fun RenderUiElement(element: UiElement) {
        val attributes = element.attributes
        WithMargin(attributes){
            val modifier = UiElementHelper.buildModifier(attributes)
            when (element) {
                is UiElement.Container -> {
                    when (element.layout) {
                        UiElement.Layout.Row -> Row(modifier = modifier) {
                            RenderChildren(element)
                        }
                        UiElement.Layout.Column -> Column(modifier = modifier) {
                            RenderChildren(element)
                        }
                        UiElement.Layout.Stack -> Box(modifier = modifier) {
                            RenderChildren(element)
                        }
                    }
                }
                is UiElement.TextNode -> {
                    Text(
                        text = element.text,
                        modifier = modifier
                    )
                }
            }
        }
    }

    @Composable
    fun RenderChildren(uiElement: UiElement.Container) {
        uiElement.children.forEach { RenderUiElement(it) }
    }
}