package cn.nobody.framework.core

import android.graphics.RectF

/**
 * Created by zpf on 2020/12/2.
 */
data class LineText(
    var textWidth: Float,
    var textBound: RectF,
    var baseline: Float,
    var textLength: Int,
    var chars: CharSequence,
    var preLineText: LineText? = null,
    var drawHalfArcBitmap: Boolean = false,
    var leftTopArcRadius: Float = 0f,
    var rightTopArcRadius: Float = 0f,
    var leftBottomArcRadius: Float = 0f,
    var rightBottomArcRadius: Float = 0f
)