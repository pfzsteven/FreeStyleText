package cn.nobody.framework.span

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.style.LineBackgroundSpan
import android.widget.TextView
import cn.nobody.framework.core.BackgroundDrawStyle
import cn.nobody.framework.core.RuleHelper
import cn.nobody.framework.util.FreeLogUtils


/**
 * Created by zpf on 2020/11/30.
 */
class FreeStyleBackgroundSpan(
    private val textView: TextView
) : LineBackgroundSpan {

    val ruleHelper = RuleHelper(textView)
    var styleBackground: BackgroundDrawStyle? = null
        private set
    private var backgroundColor: Int = Color.TRANSPARENT

    fun updateStyleBackground(newStyle: BackgroundDrawStyle) {
        styleBackground = newStyle
        updateBackgroundColor(backgroundColor, invalid = true)
    }

    fun updateBackgroundColor(newColor: Int, invalid: Boolean = false) {
        if (backgroundColor != newColor || invalid) {
            backgroundColor = newColor
            styleBackground?.run {
                setBackgroundColor(newColor)
                if (textView.text.isNotEmpty()) {
                    textView.invalidate()
                }
            }
        }
    }

    fun getBackgroundColor(): Int = backgroundColor

    override fun drawBackground(
        canvas: Canvas,
        paint: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lineNumber: Int
    ) {
        if (null == styleBackground) {
            return
        }
        val ts = System.currentTimeMillis()
        paint.isDither = true
        paint.isAntiAlias = true
        ruleHelper.draw(
            canvas,
            paint,
            baseline,
            text,
            start,
            end
        )
        val te = System.currentTimeMillis()
        if (te - ts > 16 && FreeLogUtils.enableLog) {
            FreeLogUtils.w("drawBackground $lineNumber : trace time:${te - ts}ms")
        }
    }
}