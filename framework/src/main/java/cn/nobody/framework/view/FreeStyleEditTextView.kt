package cn.nobody.framework.view

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.text.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import androidx.collection.SparseArrayCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.nobody.framework.core.RoundRectBackgroundDrawStyle
import cn.nobody.framework.util.FreeLogUtils
import kotlin.math.roundToInt


/**
 * Created by zpf on 2020/12/3.
 */
internal class FreeStyleEditTextView(
    context: Context,
    attrs: AttributeSet?
) : androidx.appcompat.widget.AppCompatEditText(context, attrs) {

    companion object {
        private const val NO_LINE_LIMIT = -1
        private const val USE_CACHE = false
    }

    private var _spacingAdd: Float = 0f
    private var _spacingMult: Float = 1.0f
    private var isDrawing = false
    private var initialized = false
    private var maxViewSize: IntArray = IntArray(2)

    private var _maxLines = NO_LINE_LIMIT
    private val availableViewBound = RectF()

    private var maxTextSize: Float = 0f
    private var bestTextSize = maxTextSize

    private val textBound = RectF()
    private val bestTextSizeCache = SparseArrayCompat<Float>()
    private var isKeyBoardOpen = true

    override fun isSuggestionsEnabled(): Boolean = false

    private val navigationBarHeight: Int by lazy {
        val resources = context.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

    init {
        isCursorVisible = true
        post {
            if (context is Activity) {
                val decorView = context.window?.decorView
                decorView?.let {
                    ViewCompat.setOnApplyWindowInsetsListener(
                        it
                    ) { _: View?, insets: WindowInsetsCompat ->
                        val bottom =
                            if (insets.systemWindowInsetBottom == navigationBarHeight) 0 else insets.systemWindowInsetBottom
                        isKeyBoardOpen = bottom != 0
                        if (FreeLogUtils.enableLog) {
                            FreeLogUtils.i("keyboard open?$isKeyBoardOpen")
                        }
                        insets
                    }
                }
            }
        }
    }

    override fun invalidate() {
        if (isDrawing) {
            return
        }
        super.invalidate()
    }

    override fun setGravity(gravity: Int) {
        val newGravity = when (gravity) {
            Gravity.LEFT -> Gravity.START
            Gravity.RIGHT -> Gravity.END
            else -> gravity
        }
        super.setGravity(newGravity)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var offsetY = 0f
        var offsetX = 0f
        freeStyleView.textRule.shadows?.let {
            it.forEach { e ->
                offsetX += (e.dx * (1.0f + paint.textSkewX))
                offsetY += e.dy
            }
        }
        freeStyleView.textRule.stroke?.let {
            val strokeWidth = it.width ?: 0f
            if (strokeWidth > offsetY) {
                offsetY += strokeWidth
            }
            if (strokeWidth > offsetX) {
                offsetX += strokeWidth
            }
        }
        val width = measuredWidth + offsetX.roundToInt()
        val height = measuredHeight + offsetY.roundToInt()
        setMeasuredDimension(width, height)

        maxViewSize[0] = MeasureSpec.getSize(widthMeasureSpec)
        maxViewSize[1] = MeasureSpec.getSize(heightMeasureSpec)
    }

    fun create2Image(): Bitmap? {
        clearComposingText()
        clearFocus()
        val bound = freeStyleView.getTextRect()
        if (bound.isEmpty || bound.width() < 1f || bound.height() < 1f) {
            return null
        }
        isCursorVisible = false
        val snapshotBitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )
        isDrawing = false
        val canvas = Canvas(snapshotBitmap)
        canvas.drawFilter = FreeStyleAppCompatTextView.drawFilter
        canvas.save()
        this.draw(canvas)
        canvas.restore()

        val clipBitmap = Bitmap.createBitmap(
            bound.width().roundToInt(),
            bound.height().roundToInt(),
            Bitmap.Config.ARGB_8888
        )
        val clipCanvas = Canvas(clipBitmap)
        val textRule = freeStyleView.textRule
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
        val srcLeft =
            when (RoundRectBackgroundDrawStyle.getGravity(this)) {
                Gravity.START -> {
                    0
                }
                Gravity.END -> {
                    width - bound.width().toInt()
                }
                else -> {
                    width.shr(1) - bound.width().div(2f).toInt()
                }
            }

        val srcRight =
            (offsetX.roundToInt() + when (RoundRectBackgroundDrawStyle.getGravity(this)) {
                Gravity.START, Gravity.END -> {
                    srcLeft + bound.width().roundToInt()
                }
                else -> {
                    width.shr(1) + bound.width().div(2f).toInt()
                }
            })

        val srcTop =
            height.shr(1) - bound.height().div(2f)
                .roundToInt() + bound.top.toInt() + offsetY.roundToInt()
        val srcBottom = srcTop + bound.height().toInt() - bound.top.toInt()

        clipCanvas.drawBitmap(
            snapshotBitmap,
            Rect(
                srcLeft, srcTop,
                srcRight, srcBottom
            ),
            Rect(
                0, 0,
                clipBitmap.width, clipBitmap.height
            ), null
        )
        snapshotBitmap.recycle()
        return clipBitmap
    }

    override fun onDraw(canvas: Canvas?) {
        if (null == canvas) {
            super.onDraw(canvas)
            return
        }
        canvas.drawFilter = FreeStyleAppCompatTextView.drawFilter
        isDrawing = true
        val p = paint
        p.style = Paint.Style.FILL
        p.setShadowLayer(0f, 0f, 0f, 0)
        freeStyleView.prepareDraw()
        freeStyleView.drawBackgroundStyle(canvas)
        super.onDraw(canvas)
        isDrawing = false
    }

    override fun setTextSize(unit: Int, size: Float) {
        val c = context
        val r: Resources
        r = if (c == null) Resources.getSystem() else c.resources
        this.textSize = TypedValue.applyDimension(
            unit, size,
            r.displayMetrics
        )
    }

    override fun setLineSpacing(add: Float, mult: Float) {
        super.setLineSpacing(add, mult)
        _spacingMult = mult
        _spacingAdd = add
    }

    private fun checkCanClearCache() {
        if (USE_CACHE) {
            bestTextSizeCache.clear()
        }
        if (initialized) {
            freeStyleView.clearCache()
        }
    }

    override fun setTextSize(size: Float) {
        maxTextSize = size
        paint.textSize = size
        invalidate()
    }

    override fun getMaxLines(): Int {
        return _maxLines
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        setShadowLayer(left.toFloat(), 0f, 0f, 0)
        super.setPadding(left, top, right, bottom)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= oldw && h <= oldh) {
            adjustTextSize()
        }
    }

    private fun findBestFontSize(): Float {
        if (text.isNullOrEmpty()) {
            return maxTextSize
        }
        val preLayout = StaticLayout(
            text!!, paint, maxViewSize[0],
            Layout.Alignment.ALIGN_NORMAL, _spacingMult, _spacingAdd, true
        )
        val lineCount = preLayout.lineCount
        if (lineCount <= 0) {
            return maxTextSize
        }
        val textPaint = TextPaint(paint)
        var size: Float = maxTextSize
        textPaint.textSize = size
        textBound.setEmpty()
        val newLayout = StaticLayout(
            text!!, textPaint, maxViewSize[0],
            Layout.Alignment.ALIGN_NORMAL, _spacingMult, _spacingAdd, true
        )
        textBound.bottom = newLayout.height.toFloat().roundToInt().toFloat()
        textBound.offsetTo(0f, 0f)

        if (textBound.height() > 0f) {
            var scale = availableViewBound.height().div(textBound.height())
            if (scale < 1f) {
                scale *= 0.9f
            }
            size = Math.min(maxTextSize, maxTextSize * scale)
        }
        return size
    }

    private fun adjustTextSize() {
        if (!isKeyBoardOpen) {
            return
        }
        if (measuredWidth <= 0 || measuredHeight <= 0) {
            return
        }
        val leftPd = if (compoundPaddingStart != 0) {
            compoundPaddingStart
        } else {
            compoundPaddingLeft
        }
        val rightPd = if (compoundPaddingEnd != 0) {
            compoundPaddingEnd
        } else {
            compoundPaddingRight
        }
        availableViewBound.top = (compoundPaddingTop.shl(1)).toFloat()
        availableViewBound.right = maxViewSize[0].toFloat() - leftPd - rightPd
        availableViewBound.bottom = maxViewSize[1].toFloat() - (compoundPaddingBottom.shl(1))

        val t1 = System.currentTimeMillis()
        bestTextSize = findBestFontSize()
        val t2 = System.currentTimeMillis()
        super.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            bestTextSize
        )
        val t3 = System.currentTimeMillis()
        if (FreeLogUtils.enableLog) {
            FreeLogUtils.d(
                "findBestFontSize " +
                        " trace:${t2 - t1}ms, setTextSize trace:${t3 - t2}ms"
            )
        }
    }

    override fun requestLayout() {
        super.requestLayout()
        adjustTextSize()
    }

    internal val freeStyleView: BaseFreeStyle =
        object : BaseFreeStyle(this@FreeStyleEditTextView) {

            private var lastText: String? = null

            override fun doTextAfterChanged(s: Editable?) {
            }

            override fun doTextBeforeChange() {
            }

            override fun doTextChanged(s: CharSequence?) {
                if (lastText == s?.toString()) {
                    return
                }
                lastText = s?.toString()
                convertToSpannableString(text)
                adjustTextSize()
            }

            override fun onInitialized() {
                super.onInitialized()
                initialized = true
            }

            override fun afterConvertToSpannableString(
                text: CharSequence
            ) {
            }

            override fun setContentText(text: CharSequence?) {
                setText(text)
            }
        }

}