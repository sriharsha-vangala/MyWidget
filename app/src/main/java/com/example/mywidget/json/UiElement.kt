package com.example.mywidget.json

sealed class UiElement {
    abstract val id: String
    abstract val attributes: Map<String, String>

    data class Container(
        override val id: String,
        val layout: Layout,
        override val attributes: Map<String, String> = emptyMap(),
        val children: List<UiElement> = emptyList()
    ) : UiElement()

    data class TextNode(
        override val id: String,
        val text: String,
        override val attributes: Map<String, String> = emptyMap()
    ) : UiElement()

    data class ImageNode(
        override val id: String,
        val src: String,
        override val attributes: Map<String, String> = emptyMap()
    ) : UiElement()

    data class ProgressNode(
        override val id: String,
        override val attributes: Map<String, String> = emptyMap(),
        val type : ProgressType = ProgressType.Linear,
        val percentage : Float = 0f
    ) : UiElement()

    enum class Layout { Row, Column, Stack }
    enum class ProgressType { Linear, Circular }
}
