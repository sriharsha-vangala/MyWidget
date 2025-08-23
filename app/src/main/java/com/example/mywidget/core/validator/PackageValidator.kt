package com.example.mywidget.core.validator

import com.example.mywidget.core.ExtractedPackage
import com.example.mywidget.json.UiSchemaValidator
import org.json.JSONObject

/**
 * Validates widget packages for structural integrity and resource consistency
 * Ensures all referenced resources exist and UI structure is valid
 */
class PackageValidator {

    /**
     * Validate complete extracted package
     */
    fun validatePackage(extractedPackage: ExtractedPackage): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // Validate UI JSON structure
        val uiValidation = validateUIJson(extractedPackage.uiJson)
        errors.addAll(uiValidation.errors)

        // Validate resource references
        val resourceValidation = validateResourceReferences(extractedPackage)
        errors.addAll(resourceValidation.errors)
        warnings.addAll(resourceValidation.warnings)

        // Validate resource integrity
        val integrityValidation = validateResourceIntegrity(extractedPackage.resources)
        errors.addAll(integrityValidation.errors)
        warnings.addAll(integrityValidation.warnings)

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }

    /**
     * Validate UI JSON structure and schema
     */
    fun validateUIJson(uiJsonString: String): ValidationResult {
        val errors = mutableListOf<String>()

        try {
            val jsonObject = JSONObject(uiJsonString)
            val schemaValidation = UiSchemaValidator.validate(jsonObject)

            if (!schemaValidation.isValid) {
                errors.addAll(schemaValidation.errors)
            }
        } catch (e: Exception) {
            errors.add("Invalid JSON format: ${e.message}")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    /**
     * Validate that all resources referenced in UI JSON exist in the package
     */
    private fun validateResourceReferences(extractedPackage: ExtractedPackage): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        try {
            val referencedResources = extractResourceReferences(extractedPackage.uiJson)
            val availableResources = extractedPackage.resources

            // Check font references
            referencedResources.fonts.forEach { fontName ->
                if (!availableResources.fonts.containsKey(fontName)) {
                    errors.add("Referenced font '$fontName' not found in package")
                }
            }

            // Check image references (now includes SVGs)
            referencedResources.images.forEach { imageName ->
                if (!availableResources.images.containsKey(imageName)) {
                    errors.add("Referenced image '$imageName' not found in package")
                }
            }

            // Check color references
            referencedResources.colors.forEach { colorName ->
                if (!availableResources.colors.containsKey(colorName)) {
                    warnings.add("Referenced color '$colorName' not found in package resources")
                }
            }

            // Check for unused resources
            val unusedFonts = availableResources.fonts.keys - referencedResources.fonts
            val unusedImages = availableResources.images.keys - referencedResources.images
            val unusedColors = availableResources.colors.keys - referencedResources.colors

            if (unusedFonts.isNotEmpty()) {
                warnings.add("Unused fonts found: ${unusedFonts.joinToString(", ")}")
            }
            if (unusedImages.isNotEmpty()) {
                warnings.add("Unused images found: ${unusedImages.joinToString(", ")}")
            }
            if (unusedColors.isNotEmpty()) {
                warnings.add("Unused colors found: ${unusedColors.joinToString(", ")}")
            }

        } catch (e: Exception) {
            errors.add("Error validating resource references: ${e.message}")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }

    /**
     * Validate resource integrity (file sizes, formats, etc.)
     */
    private fun validateResourceIntegrity(resources: com.example.mywidget.core.PackageResources): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // Validate fonts
        resources.fonts.forEach { (name, data) ->
            when {
                data.isEmpty() -> errors.add("Font '$name' has no data")
                data.size > 5 * 1024 * 1024 -> warnings.add("Font '$name' is very large (${data.size / 1024 / 1024}MB)")
                !isValidFontData(data) -> errors.add("Font '$name' appears to have invalid format")
            }
        }

        // Validate images (now includes SVGs)
        resources.images.forEach { (name, data) ->
            when {
                data.isEmpty() -> errors.add("Image '$name' has no data")
                data.size > 10 * 1024 * 1024 -> warnings.add("Image '$name' is very large (${data.size / 1024 / 1024}MB)")
                !isValidImageOrSvgData(data) -> errors.add("Image '$name' appears to have invalid format")
            }
        }

        // Validate colors
        resources.colors.forEach { (name, value) ->
            if (!isValidColorFormat(value)) {
                errors.add("Color '$name' has invalid format: $value")
            }
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }

    /**
     * Extract resource references from UI JSON
     */
    private fun extractResourceReferences(uiJsonString: String): ResourceReferences {
        val fonts = mutableSetOf<String>()
        val images = mutableSetOf<String>()
        val colors = mutableSetOf<String>()

        try {
            val json = JSONObject(uiJsonString)

            // Recursively scan all JSON objects for resource references
            scanJsonForReferences(json, fonts, images, colors)

        } catch (e: Exception) {
            // If JSON parsing fails, return empty references
        }

        return ResourceReferences(fonts, images, colors)
    }

    /**
     * Recursively scan JSON for resource references
     */
    private fun scanJsonForReferences(
        json: JSONObject,
        fonts: MutableSet<String>,
        images: MutableSet<String>,
        colors: MutableSet<String>
    ) {
        json.keys().forEach { key ->
            val value = json.get(key)

            when {
                // Font references
                key.equals("fontFamily", ignoreCase = true) && value is String -> {
                    fonts.add(value)
                }

                // Image references
                (key.equals("image", ignoreCase = true) ||
                 key.equals("backgroundImage", ignoreCase = true) ||
                 key.equals("src", ignoreCase = true)) && value is String -> {
                    // Strip file extension for consistency with storage
                    val imageNameWithoutExtension = value.substringBeforeLast('.')
                    images.add(imageNameWithoutExtension)
                }

                // Color references
                (key.contains("color", ignoreCase = true) ||
                 key.contains("background", ignoreCase = true)) && value is String -> {
                    if (value.startsWith("@color/")) {
                        colors.add(value.removePrefix("@color/"))
                    }
                }

                // Recursive scan for nested objects
                value is JSONObject -> {
                    scanJsonForReferences(value, fonts, images, colors)
                }

                // Scan arrays
                value is org.json.JSONArray -> {
                    for (i in 0 until value.length()) {
                        val item = value.get(i)
                        if (item is JSONObject) {
                            scanJsonForReferences(item, fonts, images, colors)
                        }
                    }
                }
            }
        }
    }

    /**
     * Validate font data format
     */
    private fun isValidFontData(data: ByteArray): Boolean {
        if (data.size < 4) return false

        // Check for common font file signatures
        return when {
            // TTF signature
            data[0] == 0x00.toByte() && data[1] == 0x01.toByte() &&
            data[2] == 0x00.toByte() && data[3] == 0x00.toByte() -> true

            // OTF signature
            data[0] == 0x4F.toByte() && data[1] == 0x54.toByte() &&
            data[2] == 0x54.toByte() && data[3] == 0x4F.toByte() -> true

            else -> true // Be lenient for other formats
        }
    }

    /**
     * Validate image or SVG data format (combined validation)
     */
    private fun isValidImageOrSvgData(data: ByteArray): Boolean {
        return isValidImageData(data) || isValidSvgData(data)
    }

    /**
     * Validate image data format
     */
    private fun isValidImageData(data: ByteArray): Boolean {
        if (data.size < 4) return false

        return when {
            // PNG signature
            data[0] == 0x89.toByte() && data[1] == 0x50.toByte() &&
            data[2] == 0x4E.toByte() && data[3] == 0x47.toByte() -> true

            // JPEG signature
            data[0] == 0xFF.toByte() && data[1] == 0xD8.toByte() -> true

            // WebP signature
            data.size >= 12 && data[0] == 0x52.toByte() && data[1] == 0x49.toByte() -> true

            // GIF signature
            data.size >= 6 && data[0] == 0x47.toByte() && data[1] == 0x49.toByte() -> true

            else -> false
        }
    }

    /**
     * Validate SVG data format
     */
    private fun isValidSvgData(data: ByteArray): Boolean {
        val content = String(data).trim()
        return content.startsWith("<svg") || content.startsWith("<?xml")
    }

    /**
     * Validate color format
     */
    private fun isValidColorFormat(color: String): Boolean {
        return try {
            android.graphics.Color.parseColor(color)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}

/**
 * Validation result
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>,
    val warnings: List<String> = emptyList()
)

/**
 * Resource references found in UI JSON
 */
private data class ResourceReferences(
    val fonts: Set<String>,
    val images: Set<String>,
    val colors: Set<String>
)
