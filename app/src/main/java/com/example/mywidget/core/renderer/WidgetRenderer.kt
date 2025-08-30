package com.example.mywidget.core.renderer

import android.content.Context
import com.example.mywidget.core.storage.ResourceStorage
import com.example.mywidget.json.UiElement
import com.example.mywidget.json.UiParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull

/**
 * Handles UI rendering and real-time updates for widgets
 * Manages the current UI state and provides reactive updates
 */
class WidgetRenderer(
    private val context: Context,
    private val resourceStorage: ResourceStorage
) {
    private val _currentUI = MutableStateFlow<UiElement?>(null)
    private val _renderingState = MutableStateFlow<RenderingState>(RenderingState.Idle)

    /**
     * Get current UI element
     */
    suspend fun getCurrentUI(): UiElement? {
        return _currentUI.value ?: loadUIFromStorage()
    }

    /**
     * Observe UI changes for reactive updates
     */
    fun observeUIChanges(): Flow<UiElement?> {
        return _currentUI.asStateFlow()
    }

    /**
     * Observe rendering state changes
     */
    fun observeRenderingState(): Flow<RenderingState> {
        return _renderingState.asStateFlow()
    }

    /**
     * Update UI configuration and trigger re-render
     */
    suspend fun updateConfiguration(uiJsonString: String): RenderResult {
        return try {
            _renderingState.value = RenderingState.Processing

            // Parse UI from JSON
            val uiElement = UiParser.parseUiJsonMap(uiJsonString)

            // Validate resources are available
            val validationResult = validateUIResources(uiElement)
            if (!validationResult.isValid) {
                _renderingState.value = RenderingState.Error(validationResult.errors.joinToString(", "))
                return RenderResult.ValidationError(validationResult.errors)
            }

            // Update current UI
            _currentUI.value = uiElement
            _renderingState.value = RenderingState.Ready

            RenderResult.Success(uiElement)
        } catch (e: Exception) {
            val errorMessage = "Failed to update UI configuration: ${e.message}"
            _renderingState.value = RenderingState.Error(errorMessage)
            RenderResult.Error(errorMessage)
        }
    }

    /**
     * Clear current UI configuration
     */
    suspend fun clearConfiguration() {
        _currentUI.value = null
        _renderingState.value = RenderingState.Idle
    }

    /**
     * Force refresh UI from storage
     */
    suspend fun refreshFromStorage(): UiElement? {
        return loadUIFromStorage()
    }

    /**
     * Load UI from storage and update current state
     */
    private suspend fun loadUIFromStorage(): UiElement? {
        return try {
            val uiJsonString = resourceStorage.getUIConfiguration()
            if (uiJsonString != null) {
                val uiElement = UiParser.parseUiJsonMap(uiJsonString)
                _currentUI.value = uiElement
                _renderingState.value = RenderingState.Ready
                uiElement
            } else {
                _renderingState.value = RenderingState.Idle
                null
            }
        } catch (e: Exception) {
            _renderingState.value = RenderingState.Error("Failed to load UI: ${e.message}")
            null
        }
    }

    /**
     * Validate that all UI resources are available
     */
    private fun validateUIResources(uiElement: UiElement): ResourceValidationResult {
        val missingResources = mutableListOf<String>()

        fun validateElement(element: UiElement) {
            // Check font resources
            element.attributes["fontFamily"]?.let { fontName ->
                if (!resourceStorage.hasFontResource(fontName)) {
                    missingResources.add("Font: $fontName")
                }
            }

            // Check image resources
            listOf("image", "backgroundImage", "src").forEach { attr ->
                element.attributes[attr]?.let { imageName ->
                    if (!resourceStorage.hasImageResource(imageName) &&
                        !resourceStorage.hasSvgResource(imageName)) {
                        missingResources.add("Image: $imageName")
                    }
                }
            }

            // Recursively check children
            if (element is UiElement.Container) {
                element.children.forEach { validateElement(it) }
            }
        }

        validateElement(uiElement)

        return ResourceValidationResult(
            isValid = missingResources.isEmpty(),
            errors = missingResources
        )
    }

    /**
     * Get current rendering state
     */
    fun getCurrentRenderingState(): RenderingState {
        return _renderingState.value
    }

    /**
     * Check if UI is ready for rendering
     */
    fun isUIReady(): Boolean {
        return _renderingState.value is RenderingState.Ready && _currentUI.value != null
    }

    /**
     * Get UI element count for statistics
     */
    fun getUIElementCount(): Int {
        return _currentUI.value?.let { countUIElements(it) } ?: 0
    }

    /**
     * Count UI elements recursively
     */
    private fun countUIElements(element: UiElement): Int {
        return when (element) {
            is UiElement.Container -> 1 + element.children.sumOf { countUIElements(it) }
            is UiElement.TextNode,
            is UiElement.ImageNode,
            is UiElement.ProgressNode -> 1
        }
    }
}

/**
 * Rendering state enumeration
 */
sealed class RenderingState {
    object Idle : RenderingState()
    object Processing : RenderingState()
    object Ready : RenderingState()
    data class Error(val message: String) : RenderingState()
}

/**
 * Render operation result
 */
sealed class RenderResult {
    data class Success(val uiElement: UiElement) : RenderResult()
    data class ValidationError(val errors: List<String>) : RenderResult()
    data class Error(val message: String) : RenderResult()
}

/**
 * Resource validation result
 */
private data class ResourceValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)
