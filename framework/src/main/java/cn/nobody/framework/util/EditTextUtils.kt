package cn.nobody.framework.util

import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.widget.TextView
import cn.nobody.framework.span.CustomTypeFaceSpan


/**
 * Created by zpf on 2020/12/1.
 */
object EditTextUtils {

    private fun getTypeFaceSpan(typeface: Typeface, en: Boolean): CustomTypeFaceSpan {
        return CustomTypeFaceSpan(typeface, enFont = en)
    }

    private fun setSpan(
        spannableStringBuilder: SpannableStringBuilder,
        typeface: Typeface,
        start: Int,
        end: Int,
        en: Boolean
    ) {
        val span = getTypeFaceSpan(typeface, en)
        spannableStringBuilder.setSpan(
            span,
            start,
            end,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    @JvmStatic
    fun replaceTypeFaces(
        spannableStringBuilder: SpannableStringBuilder,
        fullChars: CharSequence,
        zh: Typeface,
        en: Typeface,
        startExecuteIndex: Int = 0
    ) {
        if (fullChars.isEmpty()) {
            return
        }
        val currentLineChars = if (startExecuteIndex > 0) {
            fullChars.subSequence(startExecuteIndex, fullChars.length)
        } else {
            fullChars
        }
        val matchResults: Sequence<MatchResult> = TextUtils.regex.findAll(currentLineChars)
        val iterator = matchResults.iterator()
        val list = ArrayList<Pair<Int, Int>>()
        var lastEnd = 0
        while (iterator.hasNext()) {
            val mr = iterator.next()
            val content = currentLineChars.subSequence(mr.range.first, mr.range.last + 1)
            if (content.length == 1 && content == '\n'.toString()) {
                continue
            }
            val englishTypeFaceSpan = getTypeFaceSpan(en, true)
            val start = startExecuteIndex + mr.range.first
            val end = startExecuteIndex + mr.range.last + 1
            spannableStringBuilder.setSpan(
                englishTypeFaceSpan,
                start,
                end,
                SpannableString.SPAN_INCLUSIVE_EXCLUSIVE
            )
            list.add(Pair(lastEnd, start))
            lastEnd = end
        }
        if (list.isNotEmpty()) {
            for (p in list) {
                val start = p.first
                val end = p.second
                if (start < end) {
                    val zhTypeFaceSpan = getTypeFaceSpan(zh, false)
                    spannableStringBuilder.setSpan(
                        zhTypeFaceSpan,
                        start,
                        end,
                        SpannableString.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
            }
            if (lastEnd < fullChars.length) {
                val start = lastEnd
                val end = fullChars.length
                val zhTypeFaceSpan = getTypeFaceSpan(zh, false)
                spannableStringBuilder.setSpan(
                    zhTypeFaceSpan,
                    start,
                    end,
                    SpannableString.SPAN_INCLUSIVE_EXCLUSIVE
                )
            }
        } else {
            val zhTypeFaceSpan = getTypeFaceSpan(zh, false)
            spannableStringBuilder.setSpan(
                zhTypeFaceSpan,
                startExecuteIndex + 0,
                startExecuteIndex + fullChars.length,
                SpannableString.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
    }

    @JvmStatic
    fun computeLinePosition(
        tv: TextView,
        leftCharIndex: Int,
        chars: CharSequence,
        minWidth: Float
    ): RectF {
        val rectF = RectF()
        val bound = Rect()
        rectF.setEmpty()
        bound.setEmpty()
        if (tv.layout == null) {
            return rectF
        }
        val layout: Layout = tv.layout
        val line: Int = layout.getLineForOffset(leftCharIndex)
        layout.getLineBounds(line, bound)
        val textWidth = StaticLayout.getDesiredWidth(
            chars,
            0,
            chars.length,
            tv.paint
        )
        val leftX = layout.getPrimaryHorizontal(leftCharIndex)
        rectF.set(
            leftX,
            bound.top.toFloat(),
            leftX + textWidth,
            bound.bottom.toFloat()
        )
        if (rectF.width() < minWidth) {
            val delta = -((minWidth - rectF.width()) / 2)
            rectF.inset(delta, 0f)
        }
        return rectF
    }
}