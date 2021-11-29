package cn.nobody.framework.core

import android.graphics.Canvas
import android.graphics.Paint
import android.widget.TextView

/**
 * Created by zpf on 2020/12/1.
 */
interface BackgroundDrawStyle {

    fun setBackgroundColor(color: Int)

    fun getBackgroundColor(): Int

    fun getBackgroundRadius(): Float

    fun draw(
        textView: TextView,
        canvas: Canvas,
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        lineNumber: Int
    )
}