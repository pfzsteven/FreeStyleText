package com.example.app

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cn.nobody.freestyletext.R
import cn.nobody.framework.entity.ShadowEntity
import cn.nobody.framework.entity.StrokeEntity
import cn.nobody.framework.entity.TextRuleEntity
import cn.nobody.framework.view.FreestyleLayoutView

/**
 *
 * Created by zpf on 3/31/21.
 */
class EditTextActivity : AppCompatActivity() {

    private lateinit var freestyleLayoutView: FreestyleLayoutView
    private var revertColorIndex = 0
    private val backgroundColors: IntArray = intArrayOf(Color.BLACK, Color.WHITE, Color.TRANSPARENT)
    private val colors: IntArray =
        intArrayOf(Color.WHITE, Color.BLACK, Color.BLACK)
    private var currentTextAlign = FreestyleLayoutView.ALIGN_CENTER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_text)
        freestyleLayoutView = findViewById(R.id.free_style_layout_view)
        freestyleLayoutView.keepScreenOn = true
        findViewById<View>(R.id.btn_not_ite).setOnClickListener {
            freestyleLayoutView.setFontTypeFace(
                zh = Typeface.create(
                    FontTools.getDefaultFont(),
                    Typeface.BOLD
                ),
                en = Typeface.create(
                    FontTools.getRubikBold(applicationContext),
                    Typeface.BOLD
                )
            )
        }
        findViewById<View>(R.id.btn_ite).setOnClickListener {
            freestyleLayoutView.setFontTypeFace(
                zh = Typeface.create(
                    FontTools.getDefaultFont(),
                    Typeface.BOLD_ITALIC
                ),
                en = Typeface.create(
                    FontTools.getRubikBold(applicationContext),
                    Typeface.BOLD_ITALIC
                )
            )
        }
        findViewById<View>(R.id.btn_align).setOnClickListener {
            val align = when (currentTextAlign) {
                FreestyleLayoutView.ALIGN_CENTER -> {
                    FreestyleLayoutView.ALIGN_LEFT
                }
                FreestyleLayoutView.ALIGN_LEFT -> {
                    FreestyleLayoutView.ALIGN_RIGHT
                }
                else -> {
                    FreestyleLayoutView.ALIGN_CENTER
                }
            }
            currentTextAlign = align
            freestyleLayoutView.setTextAlign(align)
        }
        findViewById<View>(R.id.btn_apply).setOnClickListener {
            val intent = Intent()
            val bundle = Bundle()
            bundle.putParcelable(
                MainActivity.KEY_DATA, EditTextBean(
                    text = freestyleLayoutView.getContentText()?.toString() ?: "",
                    align = freestyleLayoutView.getTextAlign(),
                    textSize = freestyleLayoutView.getTextFontPxSize(),
                    color = freestyleLayoutView.getForegroundColor(),
                    backgroundColor = freestyleLayoutView.getBackgroundColor()
                )
            )
            intent.putExtras(bundle)
            setResult(RESULT_OK, intent)
            finish()
        }
        freestyleLayoutView.setFontTypeFace(
            FontTools.getDefaultFont(),
            FontTools.getRubikItalic(applicationContext)
        )
        if (revertColorIndex < 0) {
            revertColorIndex = 0
        }
        val foregroundColor = colors[revertColorIndex]
        val backgroundColor = backgroundColors[revertColorIndex]
        freestyleLayoutView.setColors(Color.WHITE, Color.BLACK)
        revertColorIndex++
        freestyleLayoutView.setTextAlign(currentTextAlign)

        initShadow()
    }

    private fun initShadow() {
        val shadowList: ArrayList<ShadowEntity> = ArrayList()
        shadowList.add(
            ShadowEntity(
                opaque = 100,
                angle = 144,
                dx = 3f,
                dy = 3f,
                radius = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    2f,
                    applicationContext.resources.displayMetrics
                ),
                shadowColor = Color.RED
            )
        )
        shadowList.add(
            ShadowEntity(
                opaque = 100,
                angle = 144,
                dx = 6f,
                dy = 6f,
                radius = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    2f,
                    applicationContext.resources.displayMetrics
                ),
                shadowColor = Color.WHITE
            )
        )
        freestyleLayoutView.setTextRule(TextRuleEntity(shadows = shadowList))
    }

    private fun initStrokeAndShadow(freestyleLayoutView: FreestyleLayoutView) {
        val shadowList: ArrayList<ShadowEntity> = ArrayList()
        shadowList.add(
            ShadowEntity(
                opaque = 100,
                angle = 144,
                dx = 8f,
                dy = 8f,
                radius = 4f,
                shadowColor = Color.GREEN
            )
        )
        freestyleLayoutView.setTextRule(
            TextRuleEntity(
                stroke = StrokeEntity(
                    width = 2f,
                    strokeColor = Color.RED
                ),
                shadows = shadowList
            )
        )
    }

}