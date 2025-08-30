package com.example.mywidget.dynamic.renderer

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.mywidget.R
import com.example.mywidget.data.model.UiElement
import com.example.mywidget.dynamic.UiElementHelper
import com.example.mywidget.dynamic.UiElementHelper.getTextColor

@Composable
fun RenderProgressNode(element: UiElement.ProgressNode) {
    val attributes = element.attributes
    val modifier = UiElementHelper.buildModifier(attributes)
    val progressColor = UiElementHelper.getProgressColor(attributes)
    val size = UiElementHelper.getProgressSize(attributes)

    // Ensure percentage is between 0 and 1
    val normalizedProgress = (element.percentage / 100f).coerceIn(0f, 1f)

    when (element.type) {
        UiElement.ProgressType.Circular -> {

            Box(
                modifier = modifier
                    .size(size.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressBar(percentage = element.percentage.toInt(), size = size)
                val textColor = getTextColor(attributes)
                val fontSize = UiElementHelper.getFontSize(attributes) ?: 12.sp

                Text(
                    text = "${element.percentage.toInt()}%",
                    style = TextStyle(
                        color = ColorProvider(textColor),
                        fontSize = fontSize
                    )
                )
            }
        }

        UiElement.ProgressType.Linear -> {
            // For linear progress, create a custom progress bar using stacked Boxes
            val height = attributes["height"]?.toIntOrNull() ?: 8
            val backgroundColor = UiElementHelper.getProgressBackgroundColor(attributes)

            // Create the progress bar bitmap
            val progressBitmap = createLinearProgressBitmap(
                width = attributes["width"]?.toIntOrNull() ?: 200,
                height = height,
                progress = normalizedProgress,
                progressColor = progressColor,
                backgroundColor = backgroundColor
            )

            Image(
                provider = ImageProvider(progressBitmap),
                contentDescription = "Progress ${element.percentage.toInt()}%",
                modifier = modifier
            )
        }
    }
}

private fun createLinearProgressBitmap(
    width: Int,
    height: Int,
    progress: Float,
    progressColor: Color,
    backgroundColor: Color,
    cornerRadius: Float = height / 2f
): Bitmap {
    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)

    val backgroundPaint = Paint().apply {
        color = backgroundColor.toArgb()
    }
    val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, backgroundPaint)

    val progressPaint = Paint().apply {
        color = progressColor.toArgb()
    }

    val progressWidth = (width * progress).coerceIn(0f, width.toFloat())
    if (progressWidth > 0) {
        val progressRect = RectF(0f, 0f, progressWidth, height.toFloat())
        canvas.drawRoundRect(progressRect, cornerRadius, cornerRadius, progressPaint)
    }

    return bitmap
}

@Composable
fun CircularProgressBar(
    percentage: Int,
    size: Int,
    progressColor: Color = Color(0xFF4CAF50),
    backgroundColor: Color = Color(0xFFE0E0E0)
) {
    val context = LocalContext.current

    AndroidRemoteViews(
        remoteViews = RemoteViews(context.packageName, R.layout.circular_progress_indicator).apply {
            setProgressBar(R.id.circular_progress, 100, percentage, false)
            setColorStateList(
                R.id.circular_progress,
                "setProgressTintList",
                ColorStateList.valueOf(progressColor.toArgb())
            )
            setColorStateList(
                R.id.circular_progress,
                "setProgressBackgroundTintList",
                ColorStateList.valueOf(backgroundColor.toArgb())
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Resize the entire layout root
                setViewLayoutWidth(
                    R.id.circular_progress_indicator_root,
                    size.toFloat(),
                    TypedValue.COMPLEX_UNIT_DIP
                )
                setViewLayoutHeight(
                    R.id.circular_progress_indicator_root,
                    size.toFloat(),
                    TypedValue.COMPLEX_UNIT_DIP
                )
            }
        }
    )
}