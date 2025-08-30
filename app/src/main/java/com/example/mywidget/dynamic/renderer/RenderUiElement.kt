package com.example.mywidget.dynamic.renderer

import android.content.Context
import androidx.compose.runtime.Composable
import com.example.mywidget.data.model.UiElement

@Composable
fun RenderUiElement(context: Context, element: UiElement) {
    when (element) {
        is UiElement.Container -> RenderContainer(context, element)
        is UiElement.TextNode -> RenderTextNode(context, element)
        is UiElement.ImageNode -> RenderImageNode(context, element)
        is UiElement.ProgressNode -> RenderProgressNode(element)
    }
}