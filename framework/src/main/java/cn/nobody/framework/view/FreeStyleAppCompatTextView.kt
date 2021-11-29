package cn.nobody.framework.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PaintFlagsDrawFilter
import android.text.Editable
import android.util.AttributeSet
import android.view.Gravity
import kotlin.math.roundToInt

/**
 * Created by zpf on 2020/12/3.
 */
internal class FreeStyleAppCompatTextView(
    context: Context,
    attrs: AttributeSet?
) : androidx.appcompat.widget.AppCompatTextView(context, attrs) {

    private var showText: CharSequence = ""
    private var lastTextLength: Int = 0
    private var isDrawing = false

    companion object {
        val drawFilter = PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG)
    }

    init {
        isSingleLine = false
        isCursorVisible = false
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
//        Log.d("zpf", "size:$width x $height , offset:$offsetX , $offsetY ")
        setMeasuredDimension(width, height)
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        setShadowLayer(left.toFloat(), 0f, 0f, 0)
        super.setPadding(left, top, right, bottom)
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

    override fun onDraw(canvas: Canvas?) {
        if (null == canvas) {
            super.onDraw(canvas)
            return
        }
        canvas.drawFilter = drawFilter
        isDrawing = true
        val p = paint
        p.style = Paint.Style.FILL
        p.setShadowLayer(0f, 0f, 0f, 0)
        freeStyleView.prepareDraw()
        freeStyleView.drawBackgroundStyle(canvas)
        super.onDraw(canvas)
        isDrawing = false
    }

    internal val freeStyleView: BaseFreeStyle =
        object : BaseFreeStyle(this@FreeStyleAppCompatTextView) {

            override fun afterConvertToSpannableString(
                text: CharSequence
            ) {
            }

            override fun setContentText(text: CharSequence?) {
                setText(text)
            }

            override fun doTextAfterChanged(s: Editable?) {
                lastTextLength = s?.length ?: 0
            }

            override fun doTextBeforeChange() {
            }

            override fun doTextChanged(s: CharSequence?) {
                if (showText.toString() == s!!.toString()) {
                    return
                }
                showText = text
                convertToSpannableString(text)
            }


        }

}