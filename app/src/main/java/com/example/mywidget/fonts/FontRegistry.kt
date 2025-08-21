package com.example.mywidget.fonts

import android.content.Context
import java.io.File

object FontRegistry {
    private val fonts: MutableMap<String, String> = mutableMapOf() // name -> path

    fun registerFont(name: String, path: String) {
        fonts[name] = path
    }

    fun getFontPath(name: String): String? = fonts[name]

    fun loadFontsFromDir(context: Context) {
        val fontsDir = File(context.filesDir, "fonts")
        if (!fontsDir.exists()) return

        fontsDir.listFiles()?.forEach { file ->
            fonts[file.nameWithoutExtension] = file.absolutePath
        }
    }
}