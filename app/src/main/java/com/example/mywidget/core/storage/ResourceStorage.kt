package com.example.mywidget.core.storage

import android.content.Context
import android.util.Log
import com.example.mywidget.core.PackageResources
import com.example.mywidget.datastore.storeUIJson
import com.example.mywidget.datastore.getUIJsonString
import com.example.mywidget.fonts.FontRegistry
import com.example.mywidget.json.ColorRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Manages storage and retrieval of widget resources
 * Handles fonts, images (including SVGs), colors, and UI configurations
 *
 * Storage structure:
 * /data/data/com.example.mywidget/files/
 * ├── widget_resources/
 * │   ├── fonts/
 * │   ├── images/ (contains both regular images and SVGs)
 * │   └── package_metadata.json
 * └── datastore/
 *     └── ui_json (stored via DataStore)
 */
class ResourceStorage(
    private val context: Context
) {
    private val baseDir = File(context.filesDir, "widget_resources")
    private val fontsDir = File(baseDir, "fonts")
    private val imagesDir = File(baseDir, "images")
    private val metadataFile = File(baseDir, "package_metadata.json")

    init {
        createDirectories()
    }

    /**
     * Store all package resources
     */
    suspend fun storeResources(resources: PackageResources) = withContext(Dispatchers.IO) {
        // Clear existing resources
        clearResourceDirectories()

        // Store fonts
        storeFonts(resources.fonts)

        // Store all images (including SVGs)
        storeImages(resources.images)

        // Register colors
        registerColors(resources.colors)
    }

    /**
     * Store UI configuration
     */
    suspend fun storeUIConfiguration(uiJson: String) {
        storeUIJson(context, uiJson)
    }

    /**
     * Get current UI configuration
     */
    suspend fun getUIConfiguration(): String? {
        return getUIJsonString(context)
    }

    /**
     * Store font files and register them
     */
    private suspend fun storeFonts(fonts: Map<String, ByteArray>) = withContext(Dispatchers.IO) {
        fonts.forEach { (name, data) ->
            // Determine extension from original filename or default to ttf
            val extension = determineFontExtension(data) ?: "ttf"
            val fontFile = File(fontsDir, "$name.$extension")

            FileOutputStream(fontFile).use { fos ->
                fos.write(data)
            }
            Log.d("debug", "storeFonts: Name $name and path ${fontFile.absolutePath}")

            // Register font in FontRegistry
            FontRegistry.registerFont(name, fontFile.absolutePath)
        }
    }

    /**
     * Store image files (including SVGs)
     */
    private suspend fun storeImages(images: Map<String, ByteArray>) = withContext(Dispatchers.IO) {
        images.forEach { (name, data) ->
            // Determine file extension from data
            val extension = determineFileExtension(data) ?: "png"
            val imageFile = File(imagesDir, "$name.$extension")

            FileOutputStream(imageFile).use { fos ->
                fos.write(data)
            }
        }
    }

    /**
     * Register colors in ColorRegistry
     */
    private fun registerColors(colors: Map<String, String>) {
        colors.forEach { (name, value) ->
            ColorRegistry.registerColor(name, value)
        }
    }

    /**
     * Get font file path by name
     */
    fun getFontPath(fontName: String): String? {
        // Try different font extensions
        val extensions = listOf("ttf", "otf", "woff", "woff2")
        for (ext in extensions) {
            val fontFile = File(fontsDir, "$fontName.$ext")
            if (fontFile.exists()) return fontFile.absolutePath
        }
        return null
    }

    /**
     * Get image file path by name (includes SVGs)
     */
    fun getImagePath(imageName: String): String? {
        // Try different extensions including SVG
        val extensions = listOf("png", "jpg", "jpeg", "webp", "gif", "bmp", "svg")
        for (ext in extensions) {
            val imageFile = File(imagesDir, "$imageName.$ext")
            if (imageFile.exists()) return imageFile.absolutePath
        }
        return null
    }

    /**
     * Get SVG file path by name (same as getImagePath since SVGs are now in images/)
     */
    fun getSvgPath(svgName: String): String? {
        val svgFile = File(imagesDir, "$svgName.svg")
        return if (svgFile.exists()) svgFile.absolutePath else null
    }

    /**
     * Check if font exists
     */
    fun hasFontResource(fontName: String): Boolean {
        return getFontPath(fontName) != null
    }

    /**
     * Check if image exists (includes SVGs)
     */
    fun hasImageResource(imageName: String): Boolean {
        return getImagePath(imageName) != null
    }

    /**
     * Check if SVG exists
     */
    fun hasSvgResource(svgName: String): Boolean {
        return getSvgPath(svgName) != null
    }

    /**
     * Clear all stored resources
     */
    suspend fun clearAll() = withContext(Dispatchers.IO) {
        clearResourceDirectories()
        ColorRegistry.clearAll()
        FontRegistry.clearAll()

        if (metadataFile.exists()) {
            metadataFile.delete()
        }
    }

    /**
     * Create necessary directories
     */
    private fun createDirectories() {
        baseDir.mkdirs()
        fontsDir.mkdirs()
        imagesDir.mkdirs()
    }

    /**
     * Clear resource directories
     */
    private fun clearResourceDirectories() {
        listOf(fontsDir, imagesDir).forEach { dir ->
            if (dir.exists()) {
                dir.listFiles()?.forEach { it.delete() }
            }
        }
    }

    /**
     * Count total resources
     */
    private fun countResources(): Int {
        val fontCount = fontsDir.listFiles()?.size ?: 0
        val imageCount = imagesDir.listFiles()?.size ?: 0
        return fontCount + imageCount
    }

    /**
     * Count UI elements from current configuration
     */
    private suspend fun countUIElements(): Int {
        return try {
            val uiJson = getUIConfiguration()
            if (uiJson != null) {
                val json = org.json.JSONObject(uiJson)
                // Count non-resource keys as UI elements
                json.keys().asSequence().count { it != "resources" }
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Determine file extension from byte data
     */
    private fun determineFileExtension(data: ByteArray): String? {
        if (data.size < 4) return null

        return when {
            // SVG files (check for XML header and svg tag)
            data.size > 10 && String(data.sliceArray(0..10)).contains("<svg") -> "svg"

            // PNG signature
            data[0] == 0x89.toByte() && data[1] == 0x50.toByte() &&
                    data[2] == 0x4E.toByte() && data[3] == 0x47.toByte() -> "png"

            // JPEG signature
            data[0] == 0xFF.toByte() && data[1] == 0xD8.toByte() -> "jpg"

            // WebP signature
            data.size >= 12 && data[0] == 0x52.toByte() && data[1] == 0x49.toByte() &&
                    data[2] == 0x46.toByte() && data[3] == 0x46.toByte() &&
                    data[8] == 0x57.toByte() && data[9] == 0x45.toByte() &&
                    data[10] == 0x42.toByte() && data[11] == 0x50.toByte() -> "webp"

            // GIF signature
            data.size >= 6 && data[0] == 0x47.toByte() && data[1] == 0x49.toByte() &&
                    data[2] == 0x46.toByte() && data[3] == 0x38.toByte() -> "gif"

            else -> "png" // Default fallback
        }
    }

    /**
     * Determine font extension from byte data
     */
    private fun determineFontExtension(data: ByteArray): String? {
        if (data.size < 4) return null

        return when {
            // TTF signature
            data[0] == 0x00.toByte() && data[1] == 0x01.toByte() &&
                    data[2] == 0x00.toByte() && data[3] == 0x00.toByte() -> "ttf"

            // OTF signature
            data[0] == 0x4F.toByte() && data[1] == 0x54.toByte() &&
                    data[2] == 0x54.toByte() && data[3] == 0x4F.toByte() -> "otf"

            // WOFF signature
            data[0] == 0x77.toByte() && data[1] == 0x4F.toByte() &&
                    data[2] == 0x46.toByte() && data[3] == 0x46.toByte() -> "woff"

            // WOFF2 signature
            data[0] == 0x77.toByte() && data[1] == 0x4F.toByte() &&
                    data[2] == 0x46.toByte() && data[3] == 0x32.toByte() -> "woff2"

            else -> "ttf" // Default fallback
        }
    }

    fun loadFontsFromResources(resources: Map<String, Any>?) {
        val fontMap = resources?.get("font") as? Map<*, *>
        fontMap?.forEach { (name, fileName) ->
            val fontName = name as? String
            val fontFile = fileName as? String
            if (fontName != null && fontFile != null) {
                val fontPath = File(context.filesDir, "widget_resources/fonts/$fontFile").absolutePath
                FontRegistry.registerFont(fontName, fontPath)
            }
        }
    }
}
