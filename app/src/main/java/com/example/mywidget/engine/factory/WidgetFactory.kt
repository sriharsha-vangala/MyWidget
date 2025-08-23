package com.example.mywidget.engine.factory

/**
 * Factory for creating widget configurations for different grid sizes
 * Handles all supported grid combinations from 1x1 to 5x5
 */
class WidgetFactory {

    companion object {
        // Base cell size in dp
        private const val BASE_CELL_WIDTH = 54
        private const val BASE_CELL_HEIGHT = 54

        // Minimum sizes
        private const val MIN_CELL_WIDTH = 40
        private const val MIN_CELL_HEIGHT = 40
    }

    /**
     * Create widget configuration for specific grid size
     */
    fun createConfiguration(gridSize: GridSize): WidgetConfiguration {
        val minWidth = calculateMinWidth(gridSize.width)
        val minHeight = calculateMinHeight(gridSize.height)

        return WidgetConfiguration(
            gridSize = gridSize,
            minWidth = minWidth,
            minHeight = minHeight,
            targetCellWidth = gridSize.width,
            targetCellHeight = gridSize.height,
            resizeMode = "horizontal|vertical"
        )
    }

    /**
     * Get all supported grid sizes (1x1 to 5x5)
     */
    fun getSupportedGridSizes(): List<GridSize> {
        val gridSizes = mutableListOf<GridSize>()

        for (height in 1..5) {
            for (width in 1..5) {
                gridSizes.add(GridSize(width, height))
            }
        }

        return gridSizes
    }

    /**
     * Get grid sizes formatted as strings
     */
    fun getSupportedGridSizesAsStrings(): List<String> {
        return getSupportedGridSizes().map { it.toString() }
    }

    /**
     * Calculate minimum width based on cell count
     */
    private fun calculateMinWidth(cellWidth: Int): Int {
        return maxOf(MIN_CELL_WIDTH * cellWidth, BASE_CELL_WIDTH * cellWidth)
    }

    /**
     * Calculate minimum height based on cell count
     */
    private fun calculateMinHeight(cellHeight: Int): Int {
        return maxOf(MIN_CELL_HEIGHT * cellHeight, BASE_CELL_HEIGHT * cellHeight)
    }
}

/**
 * Grid size configuration
 */
data class GridSize(
    val width: Int,
    val height: Int
) {
    override fun toString() = "${height}x${width}"

    companion object {
        fun fromString(sizeStr: String): GridSize {
            val parts = sizeStr.split("x")
            return GridSize(parts[1].toInt(), parts[0].toInt())
        }
    }
}

/**
 * Widget configuration for specific grid size
 */
data class WidgetConfiguration(
    val gridSize: GridSize,
    val minWidth: Int,
    val minHeight: Int,
    val targetCellWidth: Int,
    val targetCellHeight: Int,
    val resizeMode: String = "horizontal|vertical"
)
