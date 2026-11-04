package com.newsperso.app.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import kotlin.math.abs

object LogoHelper {

    private val palette = intArrayOf(
        0xFF2F6BFF.toInt(), 0xFF7C4DFF.toInt(), 0xFFE85D75.toInt(),
        0xFF2E9E6E.toInt(), 0xFFD6803B.toInt(), 0xFF3B8EA5.toInt()
    )

    fun letterDrawable(context: Context, name: String): Drawable {
        val size = 128
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val color = palette[abs(name.hashCode()) % palette.size]
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, bg)
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            textSize = size * 0.44f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val letter = (name.trim().firstOrNull()?.uppercaseChar() ?: '?').toString()
        val yPos = size / 2f - (text.descent() + text.ascent()) / 2f
        canvas.drawText(letter, size / 2f, yPos, text)
        return BitmapDrawable(context.resources, bitmap)
    }
}
