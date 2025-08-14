package com.example.mywidget.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore(name = "widget_prefs")
val UI_JSON = stringPreferencesKey("ui_json")

fun storeUIJson(context: Context, uiJsonString : String) {
    CoroutineScope(Dispatchers.IO).launch {
        context.dataStore.edit { prefs ->
            prefs[UI_JSON] = uiJsonString
        }
    }
}

suspend fun getUIJsonString(context: Context): String? {
    val prefs = context.dataStore.data.first()
    return prefs[UI_JSON]
}