package com.example.mywidget.dynamic.renderer

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.padding
import com.example.mywidget.data.model.UiElement
import com.example.mywidget.dynamic.UiElementHelper

@Composable
fun RenderContainer(context: Context, element: UiElement.Container) {
    val attributes = element.attributes
    val modifier = UiElementHelper.buildModifier(attributes)
    WithMargin(attributes) {
        when (element.layout) {
            UiElement.Layout.Row -> Row(
                modifier = modifier,
                verticalAlignment = UiElementHelper.getVerticalAlignment(attributes),
                horizontalAlignment = UiElementHelper.getHorizontalAlignment(attributes)
            ) {
                RenderChildren(context, element)
            }

            UiElement.Layout.Column -> Column(
                modifier = modifier,
                horizontalAlignment = UiElementHelper.getHorizontalAlignment(attributes),
                verticalAlignment = UiElementHelper.getVerticalAlignment(attributes)
            ) {
                RenderChildren(context, element)
            }

            UiElement.Layout.Stack -> Box(
                modifier = modifier,
                contentAlignment = UiElementHelper.getAlignment(attributes)
            ) {
                RenderChildren(context, element)
            }
        }
    }
}

@Composable
fun WithMargin(attributes: Map<String, String>, content: @Composable () -> Unit) {
    val margins = UiElementHelper.calculateMargins(attributes)
    Box(
        modifier = GlanceModifier.padding(
            start = margins.start.dp,
            top = margins.top.dp,
            end = margins.end.dp,
            bottom = margins.bottom.dp
        )
    ) {
        content()
    }
}