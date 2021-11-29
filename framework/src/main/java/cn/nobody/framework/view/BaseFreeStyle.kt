package cn.nobody.framework.view

import android.annotation.SuppressLint
import android.graphics.*
import android.text.*
import android.util.TypedValue
import android.view.Gravity
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.collection.SparseArrayCompat
import cn.nobody.framework.core.BackgroundStyle
import cn.nobody.framework.core.LineText
import cn.nobody.framework.core.RoundRectBackgroundDrawStyle
import cn.nobody.framework.entity.TextRuleEntity
import cn.nobody.framework.span.CustomTypeFaceSpan
import cn.nobody.framework.span.FreeStyleBackgroundSpan
import cn.nobody.framework.util.EditTextUtils
import cn.nobody.framework.util.FreeLogUtils
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * 通用逻辑
 * Created by zpf on 2020/12/10.
 */
abstract class BaseFreeStyle(private val textView: TextView) :
    FreeStyleView, TextWatcher {

    private var lastString: CharSequence = ""
    private var lastGravity = -1
    private var lastTextSize = 0f
    private var roundRectRadius: Float = 0f

    private val backgroundPaint = Paint()

    // 记录当前最大的字号
    private var maxTextSize = 0f
    private var maxRadius = 0f
    private val lineTexts = SparseArrayCompat<LineText>()

    // 字体样式
    private var currentFontStyle: Int = STYLE_NONE
    private var currentZhTypeface: Typeface? = null
    private var currentEnTypeFace: Typeface? = null

    // 规则玩法
    internal val textRule: TextRuleEntity = TextRuleEntity()

    // 绘制背景
    private val backgroundSpan = FreeStyleBackgroundSpan(textView)
    private val emptyBackgroundDrawStyle: RoundRectBackgroundDrawStyle by lazy {
        RoundRectBackgroundDrawStyle(radius = roundRectRadius, lineTexts = lineTexts)
    }
    private val recycledObjPool = LinkedList<LineText>()

    fun getTextRect(): RectF {
        val rect = RectF()
        if (!lineTexts.isEmpty) {
            var left: Float = Int.MAX_VALUE.toFloat()
            var right = 0f
            var top = 0f
            var bottom = 0f

            for (i in 0 until lineTexts.size()) {
                val r = lineTexts.valueAt(i).textBound
                if (r.left < left) {
                    left = r.left
                }
                if (r.right > right) {
                    right = r.right
                }
                if (i == 0) {
                    top = r.top
                }
                if (i == lineTexts.size() - 1) {
                    bottom = r.bottom
                }
            }
            rect.set(left, top, right, bottom)
        }
        return rect
    }

    companion object {
        internal const val STYLE_NONE = 0
        internal const val STYLE_BOLD = 1
        internal const val STYLE_ITALIC = 2
    }

    internal abstract fun doTextChanged(s: CharSequence?)
    internal abstract fun doTextBeforeChange()
    internal abstract fun doTextAfterChanged(s: Editable?)

    final override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        doTextChanged(s)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        doTextBeforeChange()
    }

    override fun afterTextChanged(s: Editable?) {
        doTextAfterChanged(s)
    }

    @CallSuper
    protected open fun onInitialized() {
        textView.run {
            this.addTextChangedListener(this@BaseFreeStyle)
        }
    }

    init {
        textView.isSingleLine = false
        textView.background = null
        this.onInitialized()
    }

    fun clearCache(clearAll: Boolean = true) {
        val gravity = RoundRectBackgroundDrawStyle.getGravity(textView)
        maxTextSize = max(maxTextSize, textView.textSize)

        if (maxTextSize > 0f && textView.textSize != maxTextSize &&
            lastTextSize != textView.textSize
            && maxTextSize >= 1f && roundRectRadius > 0f && maxRadius > 0f
        ) {
            // 修改radius
            val newRadius: Float =
                (maxRadius * (textView.textSize.div(maxTextSize))).toInt().toFloat()
            roundRectRadius = min(maxRadius, newRadius)
            if (backgroundSpan.styleBackground is RoundRectBackgroundDrawStyle) {
                // 重新设置
                (backgroundSpan.styleBackground as RoundRectBackgroundDrawStyle).updateRadius(
                    roundRectRadius
                )
            }
        }
        if (clearAll || gravity != lastGravity || lastTextSize != textView.textSize) {
            if (recycledObjPool.isEmpty()) {
                for (index in 0 until lineTexts.size()) {
                    recycledObjPool.add(lineTexts.valueAt(index))
                }
            }
            lineTexts.clear()
        }
        lastTextSize = textView.textSize
        lastGravity = gravity
        if (clearAll) {
            if (recycledObjPool.isEmpty()) {
                for (index in 0 until lineTexts.size()) {
                    recycledObjPool.add(lineTexts.valueAt(index))
                }
            }
            lineTexts.clear()
        }
    }

    private fun buildCalculateLayout(
        text: CharSequence,
        host: TextView,
        textViewAvailableWidth: Float
    ): Layout {
        val paint = TextPaint(host.paint)
        paint.textSize = host.paint.textSize
        if (host is EditText) {
            return DynamicLayout(
                text,
                paint,
                textViewAvailableWidth.toInt(),
                host.layout?.alignment ?: Layout.Alignment.ALIGN_NORMAL,
                host.layout?.spacingMultiplier ?: 1.0f,
                host.layout?.spacingAdd ?: 0f,
                host.includeFontPadding
            )
        } else {
            return StaticLayout(
                text,
                paint,
                textViewAvailableWidth.toInt(),
                host.layout?.alignment ?: Layout.Alignment.ALIGN_NORMAL,
                host.layout?.spacingMultiplier ?: 1.0f,
                host.layout?.spacingAdd ?: 0f,
                host.includeFontPadding
            )
        }
    }

    private fun getVerticalOffset(): Int {
        var voffset = 0
        val gravity: Int = textView.gravity and Gravity.VERTICAL_GRAVITY_MASK
        val l: Layout = textView.layout
        if (gravity != Gravity.TOP) {
            val boxht: Int = 0
            val textht = l.height
            if (textht < boxht) {
                voffset = if (gravity == Gravity.BOTTOM) {
                    boxht - textht
                } else { // (gravity == Gravity.CENTER_VERTICAL)
                    boxht - textht shr 1
                }
            }
        }
        return voffset
    }

    internal fun drawBackgroundStyle(canvas: Canvas) {
        val bgColor = backgroundSpan.getBackgroundColor()
        if (bgColor == Color.TRANSPARENT) {
            return
        }
        val compoundPaddingLeft: Int = textView.compoundPaddingLeft
//        val compoundPaddingTop: Int = textView.compoundPaddingTop
//        val compoundPaddingRight: Int = textView.compoundPaddingRight
//        val compoundPaddingBottom: Int = textView.compoundPaddingBottom
        var voffsetText = 0
//        var voffsetCursor = 0
        if (textView.gravity and Gravity.VERTICAL_GRAVITY_MASK != Gravity.TOP) {
            voffsetText = getVerticalOffset()
//            voffsetCursor = getVerticalOffset()
        }
        canvas.save()
        val extendedPaddingTop: Int = textView.extendedPaddingTop
//        val extendedPaddingBottom: Int = textView.extendedPaddingBottom
        canvas.translate(
            compoundPaddingLeft.toFloat(),
            (extendedPaddingTop + voffsetText).toFloat()
        )
        val text = textView.text
        backgroundPaint.color = bgColor
        for (lineNumber in 0 until textView.lineCount) {
            val layout = textView.layout ?: break
            val start = layout.getLineStart(lineNumber)
            val end = layout.getLineEnd(lineNumber)
            backgroundSpan.styleBackground?.draw(
                textView,
                canvas,
                backgroundPaint,
                text,
                start,
                end,
                lineNumber
            )
        }
        canvas.restore()
    }

    @SuppressLint("RtlHardcoded")
    internal fun prepareDraw() {
        val text = textView.text
        val textViewAvailableWidth =
            (textView.width.toFloat() - textView.compoundPaddingLeft.toFloat()
                    - textView.compoundPaddingRight.toFloat()
                    - 2f * roundRectRadius)

        val layout: Layout =
            textView.layout ?: buildCalculateLayout(text, textView, textViewAvailableWidth)

        clearCache(clearAll = true)
        val lineCount = layout.lineCount

        var allowDrawEmptyWord = false

        // 预判是否需要绘制空格或者换行符
        for (lineNumber in 0 until lineCount) {
            val start = layout.getLineStart(lineNumber)
            val end = layout.getLineEnd(lineNumber)
            val lineText: CharSequence = text.subSequence(start, end)
            if (lineText.trim().isNotEmpty() && lineText.trim().toString() != "\n") {
                allowDrawEmptyWord = true
                break
            }
        }
        if (!allowDrawEmptyWord) {
            lineTexts.clear()
            return
        }

        for (lineNumber in 0 until lineCount) {
            val start = layout.getLineStart(lineNumber)
            val end = layout.getLineEnd(lineNumber)
            val lineText: CharSequence = text.subSequence(start, end)
            // 计算当前文字的边框
            val textBound = EditTextUtils.computeLinePosition(
                textView,
                start,
                lineText,
                RoundRectBackgroundDrawStyle.getMinTextBoundWidth(textView, roundRectRadius)
            )
            val textRule = this.textRule
            var offsetY = 0f
            var offsetX = 0f
            textRule.shadows?.let {
                if (it.isNotEmpty()) {
                    offsetX = it.first().dx
                    offsetY = it.first().dy
                }
            }
            textRule.stroke?.let {
                val strokeWidth = it.width ?: 0f
                if (strokeWidth > offsetY) {
                    offsetY = strokeWidth
                }
                if (strokeWidth > offsetX) {
                    offsetX = strokeWidth
                }
            }
            textBound.right += offsetX
            textBound.bottom += offsetY

            val textWidth = textBound.width()
            textBound.set(
                max(
                    -textView.compoundPaddingLeft.toFloat(),
                    textBound.left - textView.compoundPaddingLeft
                ),
                textBound.top - textView.compoundPaddingTop,//-4dp
                min(
                    textBound.right + textView.compoundPaddingRight,
                    (textView.width.toFloat())
                ),
                textBound.bottom
            )
            val baseline = RoundRectBackgroundDrawStyle.getBaseLine(textView.paint)
            val currentLineText = recycledObjPool.pollFirst()?.let {
                it.textWidth = textWidth
                it.textBound = textBound
                it.baseline = baseline
                it.textLength = lineText.length
                it.chars = lineText
                it.preLineText = lineTexts[lineNumber - 1]
                it.drawHalfArcBitmap = false
                it.leftTopArcRadius = roundRectRadius
                it.rightTopArcRadius = roundRectRadius
                it.leftBottomArcRadius = roundRectRadius
                it.rightBottomArcRadius = roundRectRadius
                it
            } ?: LineText(
                textWidth = textWidth,
                textBound = textBound,
                baseline = baseline,
                textLength = lineText.length,
                chars = lineText,
                preLineText = lineTexts[lineNumber - 1],
                leftTopArcRadius = roundRectRadius,
                rightTopArcRadius = roundRectRadius,
                leftBottomArcRadius = roundRectRadius,
                rightBottomArcRadius = roundRectRadius
            )
            lineTexts.put(lineNumber, currentLineText)
            val gravity = RoundRectBackgroundDrawStyle.getGravity(textView)
            currentLineText.preLineText?.let { p ->
                val preTextLineWidth = p.textBound.width()
                val dw = (currentLineText.textBound.width() - preTextLineWidth).div(2f)
                if (abs(dw) < roundRectRadius) {
                    when (gravity) {
                        Gravity.START, Gravity.LEFT -> {
                            if (dw < 0) {
                                currentLineText.textBound.right = p.textBound.right
                            }
                        }
                        Gravity.END, Gravity.RIGHT -> {
                            if (dw < 0) {
                                currentLineText.textBound.left = p.textBound.left
                            }
                        }
                        else -> {
                            if (dw < 0) {
                                currentLineText.textBound.left = p.textBound.left
                                currentLineText.textBound.right = p.textBound.right
                            } else {
                                increaseWidthLoop(dw, currentLineText)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun increaseWidthLoop(delta: Float, current: LineText) {
        var preItem: LineText? = current.preLineText
        while (preItem != null) {
            val item = preItem
            item.textBound.inset(-delta, 0f)
            preItem = item.preLineText
        }
    }

    override fun getTextView(): TextView = textView

    override fun setTextAlign(align: Int) {
        textView.gravity = align
    }

    final override fun setTextFontPxSize(px: Float) {
        if (getTextFontPxSize() != px) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, px)
        }
    }

    override fun setRadius(radius: Float) {
        roundRectRadius = radius
        maxRadius = radius
    }

    override fun setColors(foregroundColor: Int, backgroundColor: Int) {
        if (backgroundSpan.getBackgroundColor() != backgroundColor || textView.currentTextColor != foregroundColor) {
            textView.setTextColor(foregroundColor)
            backgroundSpan.updateBackgroundColor(backgroundColor)
            convertToSpannableString(textView.text)
        }
    }

    override fun setBackgroundStyle(
        style: BackgroundStyle
    ) {
        when (style) {
            BackgroundStyle.NONE -> {
                backgroundSpan.updateStyleBackground(emptyBackgroundDrawStyle)
            }
            BackgroundStyle.ROUND_RECT -> {
                backgroundSpan.updateStyleBackground(
                    RoundRectBackgroundDrawStyle(
                        radius = roundRectRadius,
                        lineTexts = lineTexts
                    )
                )
            }
        }
    }

    override fun getContentText(): CharSequence? = textView.text

    override fun setFontTypeFace(
        zh: Typeface,
        en: Typeface
    ) {
        currentFontStyle = STYLE_NONE
        if (en.isBold || zh.isBold) {
            currentFontStyle = STYLE_BOLD
        }
        if (en.isItalic || zh.isItalic) {
            currentFontStyle = currentFontStyle.or(STYLE_ITALIC)
        }
        if (currentEnTypeFace != en || currentZhTypeface != zh) {
            currentEnTypeFace = en
            currentZhTypeface = zh
            convertToSpannableString(textView.text)
        }
    }

    internal abstract fun afterConvertToSpannableString(text: CharSequence)

    private fun <T> removeSpan(ssb: SpannableStringBuilder, spans: Array<T>) {
        if (spans.isNotEmpty()) {
            for (i in spans.indices) {
                ssb.removeSpan(spans[i])
            }
        }
    }

    private fun clearMySpans(ssb: SpannableStringBuilder, deleteStartIndex: Int = 0) {
        if (ssb.isEmpty()) {
            return
        }
        // 字体
        val typeFaceSpanArray: Array<CustomTypeFaceSpan> =
            ssb.getSpans(
                deleteStartIndex, ssb.length,
                CustomTypeFaceSpan::class.java
            )
        removeSpan(ssb, typeFaceSpanArray)
        // 背景
        val backgroundSpanArray: Array<FreeStyleBackgroundSpan> =
            ssb.getSpans(
                deleteStartIndex, ssb.length,
                FreeStyleBackgroundSpan::class.java
            )
        removeSpan(ssb, backgroundSpanArray)
    }

    internal fun convertToSpannableString(text: CharSequence?) {
        val notNullText: CharSequence = text ?: ""
        val toSaveText = notNullText.subSequence(0, notNullText.length)
        val ssb: SpannableStringBuilder =
            if (textView is EditText) {
                if (textView.text is SpannableStringBuilder) {
                    (textView.text as SpannableStringBuilder).apply {
                        clearMySpans(this)
                    }
                } else {
                    SpannableStringBuilder(text)
                }
            } else {
                SpannableStringBuilder(text)
            }
        if (notNullText.isNotEmpty() && notNullText.toString() != "\n") {
            val ts = System.currentTimeMillis()
            var t1 = 0L
            var t2 = 0L
            var t3 = 0L
            try {
                if (currentEnTypeFace != null && null != currentZhTypeface) {
                    // 替换字体
                    EditTextUtils.replaceTypeFaces(
                        ssb,
                        notNullText,
                        currentZhTypeface!!,
                        currentEnTypeFace!!,
                        0
                    )
                }
                val s1 = System.currentTimeMillis()
                t1 = s1 - ts
                val s2 = System.currentTimeMillis()
                t2 = s2 - s1
                backgroundSpan.ruleHelper.zhTypeFace = this.getCurrentZhTypeFace()
                backgroundSpan.ruleHelper.enTypeFace = getCurrentEnTypeFace()
                backgroundSpan.ruleHelper.textRule = this.textRule
                ssb.setSpan(
                    backgroundSpan,
                    0,
                    toSaveText.length,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
                t3 = System.currentTimeMillis() - s2
                if (textView !is EditText) {
                    textView.text = ssb
                }
            } finally {
                val t = System.currentTimeMillis()
                if (FreeLogUtils.enableLog && t - ts > 16) {
                    FreeLogUtils.w("convertToSpannableString trace:${t - ts}ms (t1:${t1},t2:${t2},t3:${t3})")
                }
            }
        } else if (textView !is EditText) {
            textView.text = notNullText
        }
        textView.invalidate()
        lastString = toSaveText
    }

    override fun getTextAlign(): Int = textView.gravity
    override fun getTextFontPxSize(): Float = textView.textSize
    override fun getTextPaint(): TextPaint? = textView.paint
    override fun getForegroundColor(): Int = textView.paint.color
    override fun getBackgroundColor(): Int = backgroundSpan.getBackgroundColor()
    override fun getCurrentZhTypeFace(): Typeface? = currentZhTypeface
    override fun getCurrentEnTypeFace(): Typeface? = currentEnTypeFace
    override fun isBold(): Boolean = currentFontStyle.and(STYLE_BOLD) != 0
    override fun isItalic(): Boolean = currentFontStyle.and(STYLE_ITALIC) != 0

    override fun setTextRule(style: TextRuleEntity) {
        if (textRule.stroke != style.stroke || textRule.shadows != style.shadows) {
            textRule.stroke = style.stroke
            textRule.shadows = style.shadows
            convertToSpannableString(textView.text)
        }
    }

    override fun getFontStyle(): TextRuleEntity? = null


    override fun reDraw() {
        convertToSpannableString(textView.text)
    }
}