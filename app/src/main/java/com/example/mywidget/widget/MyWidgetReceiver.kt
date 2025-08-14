package com.example.mywidget.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

open class BaseWidgetReceiver(private val widgetProvider: GlanceAppWidget) : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = widgetProvider
}

class MyWidgetReceiver1x1 : BaseWidgetReceiver(MyWidget())
class MyWidgetReceiver1x2 : BaseWidgetReceiver(MyWidget())
class MyWidgetReceiver1x3 : BaseWidgetReceiver(MyWidget())
class MyWidgetReceiver1x4 : BaseWidgetReceiver(MyWidget())
class MyWidgetReceiver2x1 : BaseWidgetReceiver(MyWidget())
class MyWidgetReceiver2x2 : BaseWidgetReceiver(MyWidget())
class MyWidgetReceiver2x3 : BaseWidgetReceiver(MyWidget())
class MyWidgetReceiver2x4 : BaseWidgetReceiver(MyWidget())
class MyWidgetReceiver3x1 : BaseWidgetReceiver(MyWidget())
class MyWidgetReceiver3x2 : BaseWidgetReceiver(MyWidget())
class MyWidgetReceiver3x3 : BaseWidgetReceiver(MyWidget())
class MyWidgetReceiver3x4 : BaseWidgetReceiver(MyWidget())
class MyWidgetReceiver4x1 : BaseWidgetReceiver(MyWidget())
class MyWidgetReceiver4x2 : BaseWidgetReceiver(MyWidget())
class MyWidgetReceiver4x3 : BaseWidgetReceiver(MyWidget())
class MyWidgetReceiver4x4 : BaseWidgetReceiver(MyWidget())