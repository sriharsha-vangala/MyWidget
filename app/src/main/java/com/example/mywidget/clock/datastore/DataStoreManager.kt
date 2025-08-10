package com.example.mywidget.clock.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore(name = "widget_prefs")
val WIDGET_HEIGHT = intPreferencesKey("widget_height")
val WIDGET_WIDTH = intPreferencesKey("widget_width")

fun storeWidgetDimens(context: Context, height: Int, width: Int) {
 CoroutineScope(Dispatchers.IO).launch {
  context.dataStore.edit { prefs ->
   prefs[WIDGET_WIDTH] = width
   prefs[WIDGET_HEIGHT] = height
  }
 }
}

suspend fun getWidgetSize(context: Context): Pair<Int, Int> {
 val prefs = context.dataStore.data.first()
 val height = prefs[WIDGET_HEIGHT] ?: 100
 val width = prefs[WIDGET_WIDTH] ?: 100
 return height to width
}