package com.example.mywidget.dynamic


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class UiElement {
    abstract val id: String
    abstract val parentId: String?
    abstract val attributes: Map<String, String?>

    @Serializable
    @SerialName("column")
    data class Column(
        override val id: String,
        override val attributes: Map<String, String?> = emptyMap(),
        override val parentId: String?
    ) : UiElement()

    @Serializable
    @SerialName("row")
    data class Row(
        override val id: String,
        override val attributes: Map<String, String?> = emptyMap(),
        override val parentId: String?
    ) : UiElement()

    @Serializable
    @SerialName("stack")
    data class Stack(
        override val id: String,
        override val attributes: Map<String, String?> = emptyMap(),
        override val parentId: String?
    ) : UiElement()

    @Serializable
    @SerialName("text")
    data class Text(
        override val id: String,
        val text: String,
        override val attributes: Map<String, String?> = emptyMap(),
        override val parentId: String?
    ) : UiElement()
}

