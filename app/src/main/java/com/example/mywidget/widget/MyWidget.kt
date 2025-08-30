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
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import com.example.mywidget.core.WidgetPackageManager
import com.example.mywidget.dynamic.renderer.RenderUiElement
import kotlinx.coroutines.runBlocking

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
                RenderUiElement(context, uiElement)
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
}