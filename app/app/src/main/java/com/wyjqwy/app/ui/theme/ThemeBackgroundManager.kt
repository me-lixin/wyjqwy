package com.wyjqwy.app.ui.theme

import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.view.View

class ThemeBackgroundManager {

    enum class TextureType {
        GRADIENT_FLOW,
        PATTERN_OVERLAY,
        NOISE_TEXTURE
    }

    fun applyTextureToView(view: View, type: TextureType, primaryColor: Int) {
        val density = view.resources.displayMetrics.density
        val drawable = when (type) {
            TextureType.GRADIENT_FLOW -> createGradientFlow(primaryColor, density)
            TextureType.PATTERN_OVERLAY -> createPatternOverlay(primaryColor, density)
            TextureType.NOISE_TEXTURE -> createNoiseTexture(primaryColor)
        }
        view.background = drawable
    }

    private fun createGradientFlow(primaryColor: Int, density: Float): GradientDrawable {
        val secondary = shiftBrightness(primaryColor, 1.18f)
        return GradientDrawable(
            GradientDrawable.Orientation.BL_TR,
            intArrayOf(primaryColor, secondary)
        ).apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f * density
        }
    }

    private fun createPatternOverlay(primaryColor: Int, density: Float): LayerDrawable {
        val base = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(primaryColor)
            cornerRadius = 12f * density
        }
        val pattern = createStripeOverlay(density)
        return LayerDrawable(arrayOf(base, pattern))
    }

    private fun createNoiseTexture(primaryColor: Int): NoiseDrawable {
        return NoiseDrawable(baseColor = primaryColor).apply {
            setTintColor(primaryColor)
        }
    }

    private fun createStripeOverlay(density: Float): ShapeDrawable {
        return ShapeDrawable(RectShape()).apply {
            // ~10% alpha geometric overlay
            paint.color = Color.argb(26, 255, 255, 255)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f * density
            paint.pathEffect = DashPathEffect(floatArrayOf(8f * density, 8f * density), 0f)
        }
    }

    private fun shiftBrightness(color: Int, factor: Float): Int {
        val r = (Color.red(color) * factor).toInt().coerceIn(0, 255)
        val g = (Color.green(color) * factor).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) * factor).toInt().coerceIn(0, 255)
        return Color.argb(Color.alpha(color), r, g, b)
    }
}
