package com.example.mywidget.clock

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

open class BaseClockWidgetReceiver(private val widgetProvider: GlanceAppWidget) : GlanceAppWidgetReceiver() {
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        val intent = Intent(context, ScreenEventService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        val intent = Intent(context, ScreenEventService::class.java)
        context.stopService(intent)
    }

    override val glanceAppWidget: GlanceAppWidget
        get() = widgetProvider
}

class MyClockWidgetReceiver1x1 : BaseClockWidgetReceiver(MyClockWidget())
class MyClockWidgetReceiver1x2 : BaseClockWidgetReceiver(MyClockWidget())
class MyClockWidgetReceiver1x3 : BaseClockWidgetReceiver(MyClockWidget())
class MyClockWidgetReceiver1x4 : BaseClockWidgetReceiver(MyClockWidget())
class MyClockWidgetReceiver2x1 : BaseClockWidgetReceiver(MyClockWidget())
class MyClockWidgetReceiver2x2 : BaseClockWidgetReceiver(MyClockWidget())
class MyClockWidgetReceiver2x3 : BaseClockWidgetReceiver(MyClockWidget())
class MyClockWidgetReceiver2x4 : BaseClockWidgetReceiver(MyClockWidget())
class MyClockWidgetReceiver3x1 : BaseClockWidgetReceiver(MyClockWidget())
class MyClockWidgetReceiver3x2 : BaseClockWidgetReceiver(MyClockWidget())
class MyClockWidgetReceiver3x3 : BaseClockWidgetReceiver(MyClockWidget())
class MyClockWidgetReceiver3x4 : BaseClockWidgetReceiver(MyClockWidget())
class MyClockWidgetReceiver4x1 : BaseClockWidgetReceiver(MyClockWidget())
class MyClockWidgetReceiver4x2 : BaseClockWidgetReceiver(MyClockWidget())
class MyClockWidgetReceiver4x3 : BaseClockWidgetReceiver(MyClockWidget())
class MyClockWidgetReceiver4x4 : BaseClockWidgetReceiver(MyClockWidget())