package cn.nobody.framework.core

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.widget.TextView
import cn.nobody.framework.entity.ShadowEntity
import cn.nobody.framework.entity.TextRuleEntity
import cn.nobody.framework.span.CustomTypeFaceSpan
import cn.nobody.framework.span.TextStyleRuleSpan
import cn.nobody.framework.util.EditTextUtils
import cn.nobody.framework.util.TextUtils
import cn.nobody.framework.util.FreeLogUtils

/**
 * Created by zpf on 2021/6/9.
 */
class RuleHelper(private val textView: TextView) {

    var zhTypeFace: Typeface? = null
    var enTypeFace: Typeface? = null
    var textRule: TextRuleEntity? = null

    companion object {

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

        private fun isInvalidStyle(textRule: TextRuleEntity): Boolean {
            return (textRule.stroke == null && textRule.shadows == null)
                    || (textRule.stroke != null && (textRule.stroke!!.width == null || textRule.stroke!!.width!! == 0f))
                    || (textRule.shadows != null && textRule.shadows!!.isEmpty())
        }
    }

    private fun updateTypeFace(paint: Paint, enFont: Boolean) {
        val typeface: Typeface = paint.typeface
        when (typeface.style) {
            Typeface.BOLD_ITALIC -> {
                paint.isFakeBoldText = true
                paint.textSkewX = if (enFont) 0f else CustomTypeFaceSpan.SKEW_X
            }
            Typeface.ITALIC -> {
                paint.isFakeBoldText = false
                paint.textSkewX = if (enFont) 0f else CustomTypeFaceSpan.SKEW_X
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

    private fun drawEmoji(
        currentLineChars: CharSequence,
        lastEmojiIndex: Int,
        index: Int,
        start: Int,
        paint: Paint,
        canvas: Canvas,
        baseline: Int
    ) {
        val emojiString = currentLineChars.subSequence(lastEmojiIndex, index)
        paint.typeface = enTypeFace!!
        val textBound = EditTextUtils.computeLinePosition(
            textView,
            start + lastEmojiIndex,
            currentLineChars, 0f
        )
        updateTypeFace(paint, true)
        drawText(canvas, paint, baseline, emojiString, textBound.left)
    }

    private fun drawTypeFaces(
        canvas: Canvas,
        paint: Paint,
        baseline: Int,
        text: CharSequence,
        s: Int,
        end: Int
    ) {
        val origTypeFace = paint.typeface
        val currentLineChars = text.subSequence(s, end)
        var lastEmojiIndex = -1

        repeat(currentLineChars.length) { position ->
            val char = currentLineChars[position]
            if (char != '\n') {
                if (char.isSurrogate()) {
                    if (lastEmojiIndex == -1) {
                        lastEmojiIndex = position
                    }
                } else {
                    // 先绘制emoji
                    if (lastEmojiIndex > -1) {
                        drawEmoji(
                            currentLineChars,
                            lastEmojiIndex,
                            position,
                            s,
                            paint,
                            canvas,
                            baseline
                        )
                        lastEmojiIndex = -1
                    }
                    // 继续绘制
                    val str = currentLineChars.subSequence(position, position + 1)
                    val enFont = TextUtils.regex.matches(str)

                    if (enFont) {
                        // en
                        paint.typeface = enTypeFace!!
                    } else {
                        // zh
                        paint.typeface = zhTypeFace!!
                    }
                    val textBound = EditTextUtils.computeLinePosition(
                        textView,
                        s + position,
                        currentLineChars, 0f
                    )
                    updateTypeFace(paint, enFont)
                    drawText(canvas, paint, baseline, str, textBound.left)
                }
            }
        }
        if (lastEmojiIndex > -1) {
            drawEmoji(
                currentLineChars,
                lastEmojiIndex,
                currentLineChars.length,
                s,
                paint,
                canvas,
                baseline
            )
            lastEmojiIndex = -1
        }
        paint.typeface = origTypeFace
    }

    private fun drawText(
        canvas: Canvas,
        paint: Paint,
        baseline: Int,
        text: CharSequence,
        x: Float
    ) {
        if (null == textRule) {
            FreeLogUtils.w("no text rule")
            return
        }
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
        if (!isInvalidStyle(textRule!!)) {
            val y = baseline.toFloat()
            // 绘制描边
            textRule!!.stroke?.let { stroke ->
                val strokeWidth = stroke.width ?: 0f
                text.let { chars ->
                    paint.let { p ->
                        p.color = stroke.strokeColor ?: currentPaintColor
                        p.style = Paint.Style.FILL_AND_STROKE
                        p.textSize = currentTextSize
                        p.strokeWidth = strokeWidth
                    }
                    drawLinesText(
                        chars,
                        x,
                        y,
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
                        chars,
                        x,
                        y,
                        canvas,
                        paint
                    )
                }
            }
            // 绘制投影
            textRule!!.shadows?.let {
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
                        val dx = TextStyleRuleSpan.optDx(e)
                        val dy = TextStyleRuleSpan.optDy(e)

                        canvas.save()
                        canvas.translate(dx, dy)
                        val color = TextStyleRuleSpan.optColor(e)
                        paint.color = color
                        drawLinesText(
                            chars,
                            x,
                            y,
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
    }

    fun draw(
        canvas: Canvas,
        paint: Paint,
        baseline: Int,
        text: CharSequence,
        start: Int,
        end: Int
    ) {
        if (textView.width <= 0 || textView.height <= 0) {
            return
        }
        if (null == zhTypeFace || null == enTypeFace) {
            return
        }
        drawTypeFaces(
            canvas,
            paint,
            baseline,
            text,
            start,
            end
        )
    }
}