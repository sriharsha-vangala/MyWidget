package com.example.mywidget.core

import android.content.Context
import android.net.Uri
import com.example.mywidget.core.extractor.PackageExtractor
import com.example.mywidget.core.storage.ResourceStorage
import com.example.mywidget.core.validator.PackageValidator
import com.example.mywidget.core.renderer.WidgetRenderer
import com.example.mywidget.data.model.UiElement
import kotlinx.coroutines.flow.Flow

/**
 * Main orchestrator for widget package management
 * Handles the complete lifecycle from zip extraction to UI rendering
 */
class WidgetPackageManager private constructor(
    private val context: Context
) {
    private val packageExtractor = PackageExtractor(context)
    private val resourceStorage = ResourceStorage(context)
    private val packageValidator = PackageValidator()
    private val widgetRenderer = WidgetRenderer(context, resourceStorage)

    companion object {
        @Volatile
        private var INSTANCE: WidgetPackageManager? = null

        fun getInstance(context: Context): WidgetPackageManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WidgetPackageManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Process widget package from zip file
     */
    suspend fun processPackage(zipUri: Uri): ProcessResult {
        return try {
            // Step 1: Extract package contents
            val extractedPackage = packageExtractor.extractPackage(zipUri)

            // Step 2: Validate package structure and content
            val validationResult = packageValidator.validatePackage(extractedPackage)
            if (!validationResult.isValid) {
                return ProcessResult.ValidationError(validationResult.errors)
            }

            // Step 3: Store resources and UI configuration
            resourceStorage.storeResources(extractedPackage.resources)
            resourceStorage.storeUIConfiguration(extractedPackage.uiJson)

            // Step 4: Parse JSON and register fonts from resources section
            val jsonObject = org.json.JSONObject(extractedPackage.uiJson)
            val resources = jsonObject.optJSONObject("resources")
            if (resources != null) {
                val fontsObject = resources.optJSONObject("font")
                if (fontsObject != null) {
                    val fontsMap = mutableMapOf<String, String>()
                    fontsObject.keys().forEach { key ->
                        fontsMap[key] = fontsObject.getString(key)
                    }
                    resourceStorage.loadFontsFromResources(mapOf("font" to fontsMap))
                }
            }

            // Step 5: Initialize renderer with new configuration
            widgetRenderer.updateConfiguration(extractedPackage.uiJson)

            ProcessResult.Success(extractedPackage)
        } catch (e: Exception) {
            ProcessResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    /**
     * Get current UI element for rendering
     */
    suspend fun getCurrentUI(): UiElement? {
        return widgetRenderer.getCurrentUI()
    }

    /**
     * Observe UI changes for reactive updates
     */
    fun observeUIChanges(): Flow<UiElement?> {
        return widgetRenderer.observeUIChanges()
    }

    /**
     * Update UI configuration dynamically
     */
    suspend fun updateUIConfiguration(jsonConfig: String): UpdateResult {
        return try {
            val validationResult = packageValidator.validateUIJson(jsonConfig)
            if (!validationResult.isValid) {
                return UpdateResult.ValidationError(validationResult.errors)
            }

            resourceStorage.storeUIConfiguration(jsonConfig)
            widgetRenderer.updateConfiguration(jsonConfig)

            UpdateResult.Success
        } catch (e: Exception) {
            UpdateResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    /**
     * Clear all stored data
     */
    suspend fun clearAll() {
        resourceStorage.clearAll()
        widgetRenderer.clearConfiguration()
    }
}

/**
 * Result of package processing
 */
sealed class ProcessResult {
    data class Success(val extractedPackage: ExtractedPackage) : ProcessResult()
    data class ValidationError(val errors: List<String>) : ProcessResult()
    data class Error(val message: String) : ProcessResult()
}

/**
 * Result of UI configuration update
 */
sealed class UpdateResult {
    object Success : UpdateResult()
    data class ValidationError(val errors: List<String>) : UpdateResult()
    data class Error(val message: String) : UpdateResult()
}

/**
 * Extracted package data structure
 */
data class ExtractedPackage(
    val uiJson: String,
    val resources: PackageResources,
    val metadata: PackageMetadata? = null
)

/**
 * Package resources - contains all extracted resources from widget package
 * Images field now includes both regular images (PNG, JPG, etc.) and SVGs
 */
data class PackageResources(
    val fonts: Map<String, ByteArray> = emptyMap(),
    val images: Map<String, ByteArray> = emptyMap(),
    val colors: Map<String, String> = emptyMap()
)

/**
 * Package metadata
 */
data class PackageMetadata(
    val name: String,
    val version: String,
    val description: String? = null,
    val author: String? = null
)

/**
 * Package information
 */
data class PackageInfo(
    val metadata: PackageMetadata?,
    val resourceCount: Int,
    val uiElementCount: Int,
    val lastUpdated: Long
)
