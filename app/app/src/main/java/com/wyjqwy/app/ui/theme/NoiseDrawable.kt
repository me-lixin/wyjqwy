package com.wyjqwy.app.ui.theme

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.drawable.Drawable
import kotlin.random.Random

/**
 * Lightweight noise texture drawable (runtime generated, no image assets).
 * Call [setTintColor] to adapt this texture to a theme primary color.
 */
class NoiseDrawable(
    private var baseColor: Int,
    private val noiseAlpha: Int = 22
) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private var bitmap: Bitmap? = null
    private var bitmapBounds: Rect = Rect()
    private var randomSeed: Int = 0x4A3B27

    override fun draw(canvas: Canvas) {
        val b = bounds
        if (b.isEmpty) return
        ensureBitmap(b.width(), b.height())
        canvas.drawColor(baseColor)
        bitmap?.let { canvas.drawBitmap(it, b.left.toFloat(), b.top.toFloat(), paint) }
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        ensureBitmap(bounds.width(), bounds.height())
    }

    fun setTintColor(color: Int) {
        baseColor = color
        paint.colorFilter = PorterDuffColorFilter(baseColor, PorterDuff.Mode.SRC_ATOP)
        invalidateSelf()
    }

    private fun ensureBitmap(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        if (bitmap != null && bitmapBounds.width() == width && bitmapBounds.height() == height) return

        bitmapBounds = Rect(0, 0, width, height)
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bm ->
            val c = Canvas(bm)
            val p = Paint(Paint.ANTI_ALIAS_FLAG)
            val rnd = Random(randomSeed + width * 31 + height * 17)
            repeat((width * height * 0.02f).toInt().coerceAtLeast(600)) {
                val x = rnd.nextInt(width).toFloat()
                val y = rnd.nextInt(height).toFloat()
                val a = (rnd.nextInt(noiseAlpha / 2, noiseAlpha)).coerceIn(8, 40)
                p.color = android.graphics.Color.argb(a, 255, 255, 255)
                c.drawPoint(x, y, p)
            }
        }
    }
}
