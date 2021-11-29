package cn.nobody.framework.core

import android.graphics.*
import android.text.StaticLayout
import android.view.Gravity
import android.widget.TextView
import androidx.collection.SparseArrayCompat
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign


/**
 * Created by zpf on 2020/12/1.
 */
class RoundRectBackgroundDrawStyle(
    radius: Float,
    private val lineTexts: SparseArrayCompat<LineText>
) :
    BackgroundDrawStyle {

    private val path = Path()
    private var backgroundRadius = radius
    private var arcBitmapHeight: Int = backgroundRadius.roundToInt()
    private var drawBackgroundColor: Int = Color.TRANSPARENT

    private val defaultRadiusArray = floatArrayOf(
        backgroundRadius,
        backgroundRadius,
        backgroundRadius,
        backgroundRadius,
        backgroundRadius,
        backgroundRadius,
        backgroundRadius,
        backgroundRadius
    )

    fun updateRadius(newRadius: Float) {
        if (backgroundRadius != newRadius) {
            backgroundRadius = newRadius
            arcBitmapHeight = backgroundRadius.roundToInt()
            defaultRadiusArray.fill(backgroundRadius)
        }
    }

    companion object {

        @JvmStatic
        fun getBaseLine(paint: Paint): Float {
            return (paint.fontMetrics.descent - paint.fontMetrics.ascent).div(2f) - paint.fontMetrics.descent
        }

        @JvmStatic
        fun getGravity(textView: TextView): Int {
            return if (textView.gravity.and(Gravity.TOP) != 0) {
                textView.gravity.and(Gravity.TOP.inv())
            } else {
                textView.gravity
            }
        }

        @JvmStatic
        fun getMinTextBoundWidth(textView: TextView, radius: Float): Float {
            var minWidth = StaticLayout.getDesiredWidth("a", textView.paint)
            if (radius != 0f && minWidth % radius != 0f) {
                minWidth = (minWidth / radius) * radius // 折算成整数倍
            }
            return minWidth
        }
    }

    private fun addRoundRect(
        textBound: RectF,
        radius: FloatArray,
        canvas: Canvas,
        path: Path,
        paint: Paint
    ) {
        path.addRoundRect(
            textBound,
            radius,
            Path.Direction.CW
        )
        canvas.drawPath(path, paint)
    }

    override fun setBackgroundColor(color: Int) {
        drawBackgroundColor = color
    }

    override fun getBackgroundColor(): Int = drawBackgroundColor
    override fun getBackgroundRadius(): Float = backgroundRadius

    override fun draw(
        textView: TextView,
        canvas: Canvas,
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        lineNumber: Int
    ) {
        lineTexts[lineNumber]?.let { lineText ->
            path.reset()
            val str = lineText.chars.toString().trim()
            if (str.isNotEmpty() && str != "\n") {
                drawWithPath(lineNumber, lineText, textView, canvas, paint)
            }
        }
    }

    private fun limitRadius(distance: Float): Float {
        return if (abs(distance) < backgroundRadius) {
            val valueInt = distance.toInt()
            if (valueInt != 0) {
                if (valueInt < 4) {
                    return 0f
                }
                valueInt.toFloat()
            } else {
                0f
            }
        } else {
            backgroundRadius * sign(distance)
        }
    }

    private fun drawWithPath(
        lineNumber: Int,
        currentLine: LineText,
        textView: TextView,
        canvas: Canvas,
        paint: Paint
    ) {
        val previewLine: LineText? = currentLine.preLineText
        val nextLine: LineText? = lineTexts[lineNumber + 1]
        val currentTextBound = currentLine.textBound
        val arcRectF = RectF()

        if (lineNumber == 0 && lineTexts.size() == 1) {
            addRoundRect(
                currentTextBound, defaultRadiusArray, canvas, path, paint
            )
            return
        } else {
            var leftTopArcRadius = backgroundRadius
            var rightTopArcRadius = backgroundRadius
            var leftBottomArcRadius = backgroundRadius
            var rightBottomArcRadius = backgroundRadius

            if (null != previewLine && (previewLine.chars.toString() != "\n" && previewLine.chars.isNotEmpty())) {
                val lastTextBound = previewLine.textBound
                leftTopArcRadius = limitRadius(lastTextBound.left - currentTextBound.left)
                rightTopArcRadius = limitRadius(currentTextBound.right - lastTextBound.right)
            }
            if (null != nextLine && (nextLine.chars.toString() != "\n" && nextLine.chars.isNotEmpty())) {
                leftBottomArcRadius = limitRadius(nextLine.textBound.left - currentTextBound.left)
                rightBottomArcRadius =
                    limitRadius(currentTextBound.right - nextLine.textBound.right)
            }
            currentLine.leftTopArcRadius = leftTopArcRadius
            currentLine.rightTopArcRadius = rightTopArcRadius
            currentLine.leftBottomArcRadius = leftBottomArcRadius
            currentLine.rightBottomArcRadius = rightBottomArcRadius

            path.moveTo(
                currentTextBound.left,
                currentTextBound.top + abs(leftTopArcRadius)
            )
            drawLeftTopArc(arcRectF, currentTextBound, leftTopArcRadius)
            path.lineTo(currentTextBound.right - abs(rightTopArcRadius), currentTextBound.top)
            drawRightTopArc(arcRectF, currentTextBound, rightTopArcRadius)
            path.lineTo(currentTextBound.right, currentTextBound.bottom - abs(rightBottomArcRadius))
            drawRightBottomArc(arcRectF, currentTextBound, rightBottomArcRadius)
            path.lineTo(currentTextBound.left, currentTextBound.bottom)
            drawLeftBottomArc(arcRectF, currentTextBound, leftTopArcRadius, leftBottomArcRadius)
            path.close()
        }
        canvas.drawPath(path, paint)
    }

    private fun drawLeftTopArc(
        arcRectF: RectF,
        currentTextBound: RectF,
        leftTopArcRadius: Float
    ) {
        if (leftTopArcRadius.toInt() == 0) {
            return
        }
        // draw left top arc
        val left = currentTextBound.left
        val top = currentTextBound.top
        val right = left + 2 * abs(leftTopArcRadius)
        val bottom = top + 2 * abs(leftTopArcRadius)
        arcRectF.set(left, top, right, bottom)

        if (leftTopArcRadius > 0f) {
            path.arcTo(arcRectF, 180f, 90f)
        } else {
            arcRectF.left = left - 2 * abs(leftTopArcRadius)
            arcRectF.right = left
            arcRectF.top = top
            arcRectF.bottom = arcRectF.top + 2 * abs(leftTopArcRadius)
            path.arcTo(arcRectF, 0f, -90f)
        }
    }

    private fun drawRightTopArc(
        arcRectF: RectF,
        currentTextBound: RectF,
        rightTopArcRadius: Float
    ) {
        if (rightTopArcRadius.toInt() == 0) {
            return
        }
        // draw right top arc
        val right = currentTextBound.right
        val top = currentTextBound.top
        val left = right - 2 * abs(rightTopArcRadius)
        val bottom = top + 2 * abs(rightTopArcRadius)
        arcRectF.set(left, top, right, bottom)
        if (rightTopArcRadius > 0f) {
            path.arcTo(arcRectF, 270f, 90f)
        } else {
            arcRectF.left = right
            arcRectF.right = right + 2 * abs(rightTopArcRadius)
            arcRectF.top = top
            arcRectF.bottom = arcRectF.top + 2 * abs(rightTopArcRadius)
            path.arcTo(arcRectF, 270f, -90f)
        }
    }

    private fun drawLeftBottomArc(
        arcRectF: RectF,
        currentTextBound: RectF,
        leftTopArcRadius: Float,
        leftBottomArcRadius: Float
    ) {
        // draw left bottom arc
        var left = currentTextBound.left
        val top = currentTextBound.bottom - 2 * abs(leftBottomArcRadius)
        var right = left + 2 * abs(leftBottomArcRadius)
        val bottom = currentTextBound.bottom

        if (leftBottomArcRadius >= 0f) {
            arcRectF.set(left, top, right, bottom)
            path.arcTo(arcRectF, 90f, 90f)
            path.lineTo(currentTextBound.left, currentTextBound.top + abs(leftTopArcRadius))
        } else if (leftBottomArcRadius < 0f) {
            right = currentTextBound.left
            left -= 2 * abs(leftBottomArcRadius)
            arcRectF.set(left, top, right, bottom)
            path.moveTo(currentTextBound.left, currentTextBound.bottom)
            path.arcTo(arcRectF, 0f, 90f)
            path.moveTo(
                currentTextBound.left,
                currentTextBound.top + abs(leftTopArcRadius)
            )
            path.lineTo(
                currentTextBound.left,
                currentTextBound.bottom - abs(leftBottomArcRadius)
            )
        }
    }

    private fun drawRightBottomArc(
        arcRectF: RectF,
        currentTextBound: RectF,
        rightBottomArcRadius: Float
    ) {
        // draw right bottom arc
        val top = currentTextBound.bottom - 2 * abs(rightBottomArcRadius)
        val right = currentTextBound.right
        val left = right - 2 * abs(rightBottomArcRadius)
        val bottom = currentTextBound.bottom
        arcRectF.set(left, top, right, bottom)
        if (rightBottomArcRadius >= 0f) {
            path.arcTo(arcRectF, 0f, 90f)
        } else {
            arcRectF.left = right
            arcRectF.right = right + 2 * abs(rightBottomArcRadius)
            arcRectF.top = bottom - 2 * abs(rightBottomArcRadius)
            arcRectF.bottom = bottom
            path.arcTo(arcRectF, 180f, -90f)
        }
    }
}