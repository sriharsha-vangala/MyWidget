package com.example.mywidget.clock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ScreenSwitchReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> {
                val stopIntent = Intent(context, ClockUpdateService::class.java)
                context.stopService(stopIntent)
            }
            Intent.ACTION_SCREEN_ON -> {
                val startIntent = Intent(context, ClockUpdateService::class.java)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(startIntent)
                } else {
                    context.startService(startIntent)
                }
            }
        }
    }
}
