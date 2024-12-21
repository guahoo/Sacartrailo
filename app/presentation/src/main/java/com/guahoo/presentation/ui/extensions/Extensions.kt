package com.guahoo.presentation.ui.extensions

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

@Composable
fun getBitmapFromDrawable(drawableResId: Int): Bitmap? {
    val context = LocalContext.current
    val drawable = ContextCompat.getDrawable(context, drawableResId)
    return drawable?.let {
        val bitmap = Bitmap.createBitmap(
            it.intrinsicWidth,
            it.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        DrawableCompat.wrap(it).apply {
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
        }
        bitmap
    }
}