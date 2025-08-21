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

    enum class Layout { Row, Column, Stack }
}
