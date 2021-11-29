package cn.nobody.framework.span

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.TypefaceSpan


/**
 * Created by zpf on 2020/12/8.
 */
class CustomTypeFaceSpan(
    private val newTypeface: Typeface,
    private val enFont: Boolean = false,
    family: String? = null
) :
    TypefaceSpan(family) {

    companion object {
        const val SKEW_X = 0.015f
    }

    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeFace(ds, newTypeface)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint, newTypeface)
    }

    private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
        paint.typeface = newTypeface
        when (newTypeface.style) {
            Typeface.BOLD_ITALIC -> {
                paint.isFakeBoldText = true
                paint.textSkewX = if (enFont) 0f else SKEW_X
            }
            Typeface.ITALIC -> {
                paint.isFakeBoldText = false
                paint.textSkewX = if (enFont) 0f else SKEW_X
            }
            Typeface.BOLD -> {
                paint.isFakeBoldText = true
                paint.textSkewX = 0f
            }
            else -> {
                paint.isFakeBoldText = false
                paint.textSkewX = 0f
            }
        }
    }
}