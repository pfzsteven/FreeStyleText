package cn.nobody.framework.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import cn.nobody.library.R
import cn.nobody.framework.core.BackgroundStyle
import cn.nobody.framework.entity.TextRuleEntity
import kotlin.math.abs

/**
 * Created by zpf on 2020/11/30.
 */
@SuppressLint("ViewConstructor")
class FreestyleLayoutView constructor(
    context: Context, attrs: AttributeSet? = null
) :
    FrameLayout(context, attrs),
    FreeStyleView {

    private var freeStyleView: FreeStyleView? = null
    private var viewType: Int? = TEXT_VIEW
    private var roundRectRadius: Float = 0f
    private var textColor: Int? = Color.BLACK
    private var align: Int? = ALIGN_LEFT
    private var textFontSize: Float? = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        12f,
        context.resources.displayMetrics
    )
    private var textBackgroundColor: Int? = Color.TRANSPARENT
    private var initText: CharSequence? = null

    // 当前背景色
    private var currentBackgroundStyle: BackgroundStyle = BackgroundStyle.NONE

    var fsPaddingLeft: Int = 0
        private set
    var fsPaddingTop: Int = 0
        private set
    var fsPaddingRight: Int = 0
        private set
    var fsPaddingBottom: Int = 0
        private set

    companion object {
        const val ALIGN_LEFT = Gravity.START.or(Gravity.CENTER_VERTICAL)
        const val ALIGN_CENTER = Gravity.CENTER
        const val ALIGN_RIGHT = Gravity.END.or(Gravity.CENTER_VERTICAL)

        const val TEXT_VIEW = 0
        const val EDIT_VIEW = 1

        const val NO_BACKGROUND = 0
        const val LINEAR_BACKGROUND = 1

        const val INVALID_VALUE_INT: Int = -1
        const val INVALID_VALUE_FLOAT: Float = (-1).toFloat()
    }

    init {
        init(attrs, false)
    }

    fun newBuild(
        viewType: Int? = TEXT_VIEW,
        radius: Float? = 0f,
        textColor: Int? = Color.BLACK,
        align: Int? = ALIGN_LEFT,
        textSize: Float? = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            12f,
            context.resources.displayMetrics
        ),
        textBackgroundColor: Int? = Color.TRANSPARENT,
        backgroundStyle: Int? = NO_BACKGROUND,
        text: CharSequence? = null,
        fsPaddingLeft: Int = 0,
        fsPaddingTop: Int = 0,
        fsPaddingRight: Int = 0,
        fsPaddingBottom: Int = 0
    ) {
        this.fsPaddingBottom = fsPaddingBottom
        this.fsPaddingLeft = fsPaddingLeft
        this.fsPaddingRight = fsPaddingRight
        this.fsPaddingTop = fsPaddingTop
        this.viewType = viewType
        this.roundRectRadius = radius ?: 0f

        this.textColor = textColor
        this.align = align
        this.textFontSize = textSize
        this.textBackgroundColor = textBackgroundColor
        this.currentBackgroundStyle = when (backgroundStyle) {
            LINEAR_BACKGROUND -> {
                BackgroundStyle.ROUND_RECT
            }
            else -> {
                BackgroundStyle.NONE
            }
        }
        this.initText = text
        init(null, viewType != this.viewType, false)
    }

    constructor(
        context: Context,
        viewType: Int = TEXT_VIEW,
        radius: Float = 0f,
        textColor: Int = Color.BLACK,
        align: Int = ALIGN_LEFT,
        textSize: Float = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            12f,
            context.resources.displayMetrics
        ),
        textBackgroundColor: Int = Color.TRANSPARENT,
        backgroundStyle: Int = NO_BACKGROUND,
        text: CharSequence? = null,
        fsPaddingLeft: Int = 0,
        fsPaddingTop: Int = 0,
        fsPaddingRight: Int = 0,
        fsPaddingBottom: Int = 0
    ) : this(context, null) {
        newBuild(
            viewType,
            radius,
            textColor,
            align,
            textSize,
            textBackgroundColor,
            backgroundStyle,
            text,
            fsPaddingLeft,
            fsPaddingTop,
            fsPaddingRight,
            fsPaddingBottom
        )
    }

    @SuppressLint("CustomViewStyleable")
    private fun parseAttrs(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.FreestyleEditTextView)
            roundRectRadius = abs(
                typedArray.getDimension(
                    R.styleable.FreestyleEditTextView_fs_radius, 0f
                )
            )
            textFontSize = typedArray.getDimension(
                R.styleable.FreestyleEditTextView_fs_text_size,
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    1f,
                    context.resources.displayMetrics
                )
            )
            textColor =
                typedArray.getColor(R.styleable.FreestyleEditTextView_fs_text_color, Color.BLACK)
            textBackgroundColor =
                typedArray.getColor(
                    R.styleable.FreestyleEditTextView_fs_text_background_color,
                    Color.TRANSPARENT
                )
            val backgroundStyle: Int =
                typedArray.getInt(
                    R.styleable.FreestyleEditTextView_fs_background_style,
                    NO_BACKGROUND
                )
            this.currentBackgroundStyle = when (backgroundStyle) {
                LINEAR_BACKGROUND -> {
                    BackgroundStyle.ROUND_RECT
                }
                else -> {
                    BackgroundStyle.NONE
                }
            }
            viewType =
                typedArray.getInt(
                    R.styleable.FreestyleEditTextView_fs_view_type,
                    TEXT_VIEW
                )
            initText = typedArray.getString(R.styleable.FreestyleEditTextView_fs_text)
            val textAlign =
                typedArray.getInt(
                    R.styleable.FreestyleEditTextView_fs_text_align, 0
                )
            align = when (textAlign) {
                1 -> {
                    // align_center
                    ALIGN_CENTER
                }
                2 -> {
                    // align_right
                    ALIGN_RIGHT
                }
                else -> {
                    // align_left
                    ALIGN_LEFT
                }
            }
            this.fsPaddingTop = typedArray.getDimensionPixelOffset(
                R.styleable.FreestyleEditTextView_fs_paddingTop, 0
            )
            this.fsPaddingLeft = typedArray.getDimensionPixelOffset(
                R.styleable.FreestyleEditTextView_fs_paddingStart, 0
            )
            this.fsPaddingRight = typedArray.getDimensionPixelOffset(
                R.styleable.FreestyleEditTextView_fs_paddingEnd, 0
            )
            this.fsPaddingBottom = typedArray.getDimensionPixelOffset(
                R.styleable.FreestyleEditTextView_fs_paddingBottom, 0
            )
            typedArray.recycle()
        }
    }

    private fun init(attrs: AttributeSet?, buildNew: Boolean = false, initByXml: Boolean = true) {
        if (attrs == null && initByXml) {
            return
        }
        parseAttrs(attrs)
        if (null == freeStyleView || buildNew) {
            freeStyleView =
                FreeStyleViewFactory.buildView(viewType ?: TEXT_VIEW, context, attrs)
        }
        freeStyleView?.setRadius(this.roundRectRadius)
        freeStyleView?.setTextFontPxSize(textFontSize ?: 0f)
        freeStyleView?.setColors(
            textColor ?: Color.BLACK,
            textBackgroundColor ?: Color.TRANSPARENT
        )
        val textAlign = when (align) {
            ALIGN_CENTER -> {
                Gravity.CENTER
            }
            ALIGN_RIGHT -> {
                Gravity.END
            }
            else -> {
                Gravity.START
            }
        }
        freeStyleView?.setTextAlign(textAlign)
        freeStyleView?.setBackgroundStyle(currentBackgroundStyle)
        freeStyleView?.setContentText(initText ?: "")
        freeStyleView?.getTextView()?.invalidate()
        freeStyleView?.let { childView ->
            if (childView.getTextView().parent == null) {
                val params = LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.gravity = Gravity.CENTER
                addView(childView.getTextView(), params)
                childView.getTextView().setPadding(
                    this.fsPaddingLeft,
                    this.fsPaddingTop,
                    this.fsPaddingRight,
                    this.fsPaddingBottom
                )
            }
        }
    }

    override fun reDraw() {
        freeStyleView?.reDraw()
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
    }

    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
    }

    fun text2Bitmap(): Bitmap? {
        return if (null != freeStyleView && freeStyleView!!.getTextView() is FreeStyleEditTextView) {
            (freeStyleView!!.getTextView() as FreeStyleEditTextView).create2Image()
        } else {
            null
        }
    }

    fun getBackgroundStyle(): BackgroundStyle = currentBackgroundStyle

    override fun getTextView(): TextView {
        return freeStyleView?.getTextView()!!
    }

    override fun setContentText(text: CharSequence?) {
        freeStyleView?.setContentText(text)
    }

    override fun setTextAlign(align: Int) {
        freeStyleView?.setTextAlign(align)
    }

    override fun setTextFontPxSize(px: Float) {
        freeStyleView?.setTextFontPxSize(px)
    }

    override fun setColors(foregroundColor: Int, backgroundColor: Int) {
        freeStyleView?.setColors(foregroundColor, backgroundColor)
    }

    override fun setRadius(radius: Float) {
        freeStyleView?.setRadius(radius)
    }

    override fun setBackgroundStyle(
        style: BackgroundStyle
    ) {
        if (currentBackgroundStyle == style) {
            return
        }
        freeStyleView?.getTextView()?.setPadding(
            this.fsPaddingLeft,
            this.fsPaddingTop,
            this.fsPaddingRight,
            this.fsPaddingBottom
        )
        freeStyleView?.setBackgroundStyle(style)
    }

    override fun getContentText(): CharSequence? = freeStyleView?.getContentText()

    override fun setFontTypeFace(
        zh: Typeface,
        en: Typeface
    ) {
        freeStyleView?.setFontTypeFace(zh, en)
    }

    override fun getTextAlign(): Int = freeStyleView?.getTextAlign() ?: INVALID_VALUE_INT
    override fun getTextFontPxSize(): Float =
        freeStyleView?.getTextFontPxSize() ?: INVALID_VALUE_FLOAT

    override fun getTextPaint(): TextPaint? = freeStyleView?.getTextPaint()
    override fun getForegroundColor(): Int =
        freeStyleView?.getForegroundColor() ?: INVALID_VALUE_INT

    override fun getBackgroundColor(): Int =
        freeStyleView?.getBackgroundColor() ?: INVALID_VALUE_INT

    override fun getCurrentZhTypeFace(): Typeface? = freeStyleView?.getCurrentZhTypeFace()
    override fun getCurrentEnTypeFace(): Typeface? = freeStyleView?.getCurrentEnTypeFace()
    override fun isBold(): Boolean = freeStyleView?.isBold() ?: false
    override fun isItalic(): Boolean = freeStyleView?.isItalic() ?: false
    override fun setTextRule(style: TextRuleEntity) {
        freeStyleView?.setTextRule(style)
    }

    override fun getFontStyle(): TextRuleEntity? = freeStyleView?.getFontStyle()
}