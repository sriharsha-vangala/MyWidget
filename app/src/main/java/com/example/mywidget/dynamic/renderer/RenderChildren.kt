package com.example.mywidget.dynamic.renderer

import android.content.Context
import androidx.compose.runtime.Composable
import com.example.mywidget.data.model.UiElement

@Composable
fun RenderChildren(context: Context, uiElement: UiElement.Container) {
    uiElement.children.forEach { RenderUiElement(context, it) }
}