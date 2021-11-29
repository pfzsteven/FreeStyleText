package cn.nobody.framework.view

import android.graphics.Typeface
import android.text.TextPaint
import android.widget.TextView
import cn.nobody.framework.core.BackgroundStyle
import cn.nobody.framework.entity.TextRuleEntity

/**
 * Created by zpf on 2020/12/3.
 */
interface FreeStyleView {

    fun getTextView(): TextView

    fun setContentText(text: CharSequence?)

    fun setTextAlign(align: Int)

    fun setTextFontPxSize(px: Float)

    fun setColors(foregroundColor: Int, backgroundColor: Int)

    fun setRadius(radius: Float)

    fun setBackgroundStyle(style: BackgroundStyle)

    fun getContentText(): CharSequence?

    fun setFontTypeFace(zh: Typeface, en: Typeface)

    fun getTextAlign(): Int

    fun getTextFontPxSize(): Float

    fun getTextPaint(): TextPaint?

    fun getForegroundColor(): Int

    fun getBackgroundColor(): Int

    fun getCurrentZhTypeFace(): Typeface?

    fun getCurrentEnTypeFace(): Typeface?

    fun isBold(): Boolean

    fun isItalic(): Boolean

    fun setTextRule(style: TextRuleEntity)

    fun getFontStyle(): TextRuleEntity?

    fun reDraw()
}