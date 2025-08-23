package com.example.mywidget.core.extractor

import android.content.Context
import android.net.Uri
import com.example.mywidget.core.ExtractedPackage
import com.example.mywidget.core.PackageResources
import com.example.mywidget.core.PackageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

/**
 * Responsible for extracting widget packages from zip files
 * Handles different resource types (fonts, images including SVGs) and UI configuration
 *
 * Expected zip structure:
 * - ui.json (root level)
 * - fonts/ (directory containing font files)
 * - images/ (directory containing all images including SVGs and PNGs)
 * - package.json (optional, root level)
 * - colors.json (optional, root level)
 */
class PackageExtractor(
    private val context: Context
) {

    /**
     * Extract complete package from zip URI
     */
    suspend fun extractPackage(zipUri: Uri): ExtractedPackage = withContext(Dispatchers.IO) {
        val resources = PackageResources()
        var uiJson = ""
        var metadata: PackageMetadata? = null

        val fonts = mutableMapOf<String, ByteArray>()
        val images = mutableMapOf<String, ByteArray>()
        val colors = mutableMapOf<String, String>()

        context.contentResolver.openInputStream(zipUri)?.use { inputStream ->
            ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
                var entry = zis.nextEntry

                while (entry != null) {
                    val name = entry.name

                    // Normalize the path by removing any root folder (e.g., "testui/fonts/file.ttf" -> "fonts/file.ttf")
                    val normalizedName = normalizePath(name)

                    // Skip macOS metadata and hidden files
                    if (shouldSkipFile(name)) {
                        zis.closeEntry()
                        entry = zis.nextEntry
                        continue
                    }

                    if (!entry.isDirectory) {
                        val data = readEntryData(zis)

                        when {
                            // UI Configuration (at any level, but specifically ui.json)
                            normalizedName.equals("ui.json", ignoreCase = true) ||
                            name.endsWith("/ui.json", ignoreCase = true) -> {
                                uiJson = String(data)
                            }

                            // Package metadata (at any level, but specifically package.json)
                            normalizedName.equals("package.json", ignoreCase = true) ||
                            name.endsWith("/package.json", ignoreCase = true) -> {
                                metadata = parseMetadata(String(data))
                            }

                            // Font files (in any fonts/ directory)
                            normalizedName.startsWith("fonts/", ignoreCase = true) && isFontFile(normalizedName) -> {
                                val fontName = extractResourceName(normalizedName)
                                fonts[fontName] = data
                            }

                            // Image files including SVGs (in any images/ directory)
                            normalizedName.startsWith("images/", ignoreCase = true) && (isImageFile(normalizedName) || isSvgFile(normalizedName)) -> {
                                val imageName = extractResourceName(normalizedName)
                                images[imageName] = data
                            }

                            // Color definitions (at any level, but specifically colors.json)
                            normalizedName.equals("colors.json", ignoreCase = true) ||
                            name.endsWith("/colors.json", ignoreCase = true) -> {
                                colors.putAll(parseColors(String(data)))
                            }
                        }
                    }

                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        }

        // Merge colors from UI.json if they exist
        if (uiJson.isNotEmpty()) {
            colors.putAll(extractColorsFromUIJson(uiJson))
        }

        ExtractedPackage(
            uiJson = uiJson,
            resources = PackageResources(
                fonts = fonts,
                images = images,
                colors = colors
            ),
            metadata = metadata
        )
    }

    /**
     * Read entry data from zip input stream
     */
    private fun readEntryData(zis: ZipInputStream): ByteArray {
        val buffer = ByteArray(8192)
        val output = ByteArrayOutputStream()

        var len = zis.read(buffer)
        while (len != -1) {
            output.write(buffer, 0, len)
            len = zis.read(buffer)
        }

        return output.toByteArray()
    }

    /**
     * Check if file should be skipped during extraction
     */
    private fun shouldSkipFile(fileName: String): Boolean {
        return fileName.contains("__MACOSX") ||
               fileName.endsWith(".DS_Store") ||
               fileName.startsWith(".") ||
               fileName.contains("/.git/") ||
               fileName.contains("/node_modules/")
    }

    /**
     * Check if file is a font file
     */
    private fun isFontFile(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in setOf("ttf", "otf", "woff", "woff2")
    }

    /**
     * Check if file is an image file
     */
    private fun isImageFile(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in setOf("png", "jpg", "jpeg", "webp", "gif", "bmp")
    }

    /**
     * Check if file is an SVG file
     */
    private fun isSvgFile(fileName: String): Boolean {
        return fileName.lowercase().endsWith(".svg")
    }

    /**
     * Extract resource name from file path
     */
    private fun extractResourceName(filePath: String): String {
        return filePath.substringAfterLast('/').substringBeforeLast('.')
    }

    /**
     * Parse package metadata from JSON
     */
    private fun parseMetadata(jsonString: String): PackageMetadata? {
        return try {
            val json = JSONObject(jsonString)
            PackageMetadata(
                name = json.optString("name", "Unnamed Widget"),
                version = json.optString("version", "1.0.0"),
                description = json.optString("description"),
                author = json.optString("author")
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parse colors from colors.json file
     */
    private fun parseColors(jsonString: String): Map<String, String> {
        return try {
            val json = JSONObject(jsonString)
            val colors = mutableMapOf<String, String>()

            json.keys().forEach { key ->
                colors[key] = json.getString(key)
            }

            colors
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Extract colors from UI.json resources section
     */
    private fun extractColorsFromUIJson(uiJsonString: String): Map<String, String> {
        return try {
            val json = JSONObject(uiJsonString)
            val resources = json.optJSONObject("resources")
            val colorSection = resources?.optJSONObject("color")

            val colors = mutableMapOf<String, String>()
            colorSection?.keys()?.forEach { key ->
                colors[key] = colorSection.getString(key)
            }

            colors
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Validate extracted package has required files
     */
    fun validateExtractedPackage(extractedPackage: ExtractedPackage): ExtractionValidationResult {
        val errors = mutableListOf<String>()

        // Check if UI.json exists and is not empty
        if (extractedPackage.uiJson.isEmpty()) {
            errors.add("Missing or empty ui.json file")
        }

        // Validate UI.json structure
        try {
            JSONObject(extractedPackage.uiJson)
        } catch (e: Exception) {
            errors.add("Invalid JSON format in ui.json: ${e.message}")
        }

        // Check for at least one resource (optional warning)
        val resourceCount = extractedPackage.resources.fonts.size +
                           extractedPackage.resources.images.size

        val warnings = mutableListOf<String>()
        if (resourceCount == 0) {
            warnings.add("No resources found in package")
        }

        return ExtractionValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }

    /**
     * Normalize the file path by removing any root folder
     */
    private fun normalizePath(path: String): String {
        return path.substringAfter('/')
    }
}

/**
 * Result of extraction validation
 */
data class ExtractionValidationResult(
    val isValid: Boolean,
    val errors: List<String>,
    val warnings: List<String> = emptyList()
)
