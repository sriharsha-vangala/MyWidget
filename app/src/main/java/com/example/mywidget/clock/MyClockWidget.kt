package com.example.mywidget.clock

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyClockWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent()
        }
    }

    @Composable
    fun WidgetContent() {
        val prefs = currentState<Preferences>()
        val nowMillis = System.currentTimeMillis()
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(nowMillis))
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color.White)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentTime,
                style = TextStyle(fontSize = 20.sp)
            )
        }
    }


    companion object {
        private val keyNow = longPreferencesKey("now")

        fun triggerUpdate(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(MyClockWidget::class.java)
                glanceIds.forEach { id ->
                    updateAppWidgetState(context, id) {
                        it[keyNow] = System.currentTimeMillis()
                    }
                    MyClockWidget().update(context, id)
                }
            }
        }
    }

}