package cn.nobody.framework.span

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.widget.TextView
import cn.nobody.framework.entity.ShadowEntity
import cn.nobody.framework.entity.TextRuleEntity
import java.lang.ref.WeakReference
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Created by zpf on 2020/12/12.
 */
class TextStyleRuleSpan(textView: TextView, private val textRule: TextRuleEntity) {

    private val weakTextViewRef = WeakReference<TextView>(textView)

    companion object {
        fun optColor(e: ShadowEntity): Int {
            return if (e.opaque != 100 && e.shadowColor != Color.TRANSPARENT) {
                val r = Color.red(e.shadowColor)
                val g = Color.green(e.shadowColor)
                val b = Color.blue(e.shadowColor)
                val a = (255 * (min(100f, e.opaque.toFloat()) / 100f)).roundToInt()
                Color.argb(a, r, g, b)
            } else {
                e.shadowColor
            }
        }

        fun optDx(e: ShadowEntity): Float {
            return if (e.angle == 90 || e.angle == 270) {
                0f
            } else {
                if ((e.angle in 1..89) || (e.angle in 271..359)) {
                    -e.dx
                } else {
                    e.dx
                }
            }
        }

        fun optDy(e: ShadowEntity): Float {
            return if (e.angle in 0..180) {
                e.dy
            } else {
                -e.dy
            }
        }
    }

    private fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fontMetrics: Paint.FontMetricsInt?
    ): Int {
        if (fontMetrics != null && paint.fontMetricsInt != null) {
            fontMetrics.bottom = paint.fontMetricsInt.bottom
            fontMetrics.top = paint.fontMetricsInt.top
            fontMetrics.descent = paint.fontMetricsInt.descent
            fontMetrics.leading = paint.fontMetricsInt.leading
        }
        return paint.measureText(text.toString().substring(start until end)).toInt()
    }

    private fun drawLinesText(
        chars: CharSequence,
        x: Float,
        y: Float,
        canvas: Canvas,
        paint: Paint
    ) {
        canvas.drawText(
            chars,
            0,
            chars.length,
            x,
            y,
            paint
        )
    }

    private fun isInvalidStyle(): Boolean {
        return (textRule.stroke == null && textRule.shadows == null)
                || (textRule.stroke != null && (textRule.stroke!!.width == null || textRule.stroke!!.width!! == 0f))
                || (textRule.shadows != null && textRule.shadows!!.isEmpty())
    }

    private fun onDraw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        if (text.isNullOrBlank()) {
            return
        }
        if (text.trim().isEmpty()) {
            if (end > start) {
                canvas.drawText(text.toString(), start, end, x, y.toFloat(), paint)
            }
            return
        }
        val textView = weakTextViewRef.get() ?: return

        val currentPaintColor = textView.currentTextColor
        val currentPaintStyle = paint.style
        val currentTextSize = textView.textSize
        paint.let { p ->
            p.color = currentPaintColor
            p.isDither = true
            p.isAntiAlias = true
            p.style = Paint.Style.FILL
            p.textSize = currentTextSize
        }
        if (!isInvalidStyle()) {
            // 绘制描边
            textRule.stroke?.let { stroke ->
                val strokeWidth = stroke.width ?: 0f
                text.let { chars ->
                    paint.let { p ->
                        p.color = stroke.strokeColor ?: currentPaintColor
                        p.style = Paint.Style.FILL_AND_STROKE
                        p.textSize = currentTextSize
                        p.strokeWidth = strokeWidth
                    }
                    drawLinesText(
                        chars.subSequence(start, end),
                        x,
                        y.toFloat(),
                        canvas,
                        paint
                    )
                    // 再次绘制文字，避免出现缩放过程存在镂空现象
                    paint.let { p ->
                        p.color = currentPaintColor
                        p.isDither = true
                        p.isAntiAlias = true
                        p.style = Paint.Style.FILL
                        p.textSize = currentTextSize
                    }
                    drawLinesText(
                        chars.subSequence(start, end),
                        x,
                        y.toFloat(),
                        canvas,
                        paint
                    )
                }
            }
            // 绘制投影
            textRule.shadows?.let {
                paint.apply {
                    strokeWidth = 0f
                    style = Paint.Style.FILL
                }
                text.let { chars ->
                    val list: List<ShadowEntity> =
                        if (it.first().angle in 91..270) {
                            it.reversed()
                        } else {
                            it
                        }
                    list.forEach { e ->
                        val dx = optDx(e)
                        val dy = optDy(e)

                        canvas.save()
                        canvas.translate(dx, dy)
                        val color = optColor(e)
                        paint.color = color
                        drawLinesText(
                            chars.subSequence(start, end),
                            x,
                            y.toFloat(),
                            canvas,
                            paint
                        )
                        canvas.restore()
                    }
                }
            }
            paint.color = currentPaintColor
            paint.style = currentPaintStyle
            paint.textSize = currentTextSize
            paint.strokeWidth = 0f
        }
        canvas.drawText(text, start, end, x, y.toFloat(), paint)
    }
}