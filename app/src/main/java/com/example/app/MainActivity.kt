package com.example.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import cn.nobody.freestyletext.R
import cn.nobody.library.entity.ShadowEntity
import cn.nobody.library.entity.TextRuleEntity
import cn.nobody.library.util.FreeLogUtils
import cn.nobody.library.view.FreestyleLayoutView


class MainActivity : AppCompatActivity() {

    companion object {
        const val RC = 999
        const val KEY_DATA = "data"
    }

    private lateinit var freestyleLayoutView: FreestyleLayoutView
    private var editTextBean: EditTextBean? = null
    private var scale = 1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FreeLogUtils.enableLog = true
        findViewById<View>(R.id.btn_to_edit).setOnClickListener {
            go()
        }
        findViewById<View>(R.id.btn_scale_up).setOnClickListener {
            scale += 0.5f
            freestyleLayoutView.scaleX = scale
            freestyleLayoutView.scaleY = scale
        }
        findViewById<View>(R.id.btn_scale_down).setOnClickListener {
            scale -= 0.5f
            freestyleLayoutView.scaleX = scale
            freestyleLayoutView.scaleY = scale
        }
        findViewById<Button>(R.id.btn_align_left).setOnClickListener {
            freestyleLayoutView.setTextAlign(FreestyleLayoutView.ALIGN_LEFT)
        }
        findViewById<Button>(R.id.btn_align_middle).setOnClickListener {
            freestyleLayoutView.setTextAlign(FreestyleLayoutView.ALIGN_CENTER)
        }
        findViewById<Button>(R.id.btn_align_right).setOnClickListener {
            freestyleLayoutView.setTextAlign(FreestyleLayoutView.ALIGN_RIGHT)
        }
        freestyleLayoutView = findViewById(R.id.free_style_layout_view)
        freestyleLayoutView.setColors(Color.WHITE, Color.BLACK)

        freestyleLayoutView.keepScreenOn = true
        freestyleLayoutView.setFontTypeFace(
            zh = FontTools.getDefaultFontItalic(),
            en = FontTools.getRubikBold(applicationContext)
        )

        initShadow()
    }

    /**
     * {
    "opaque": 100,
    "angle": 144,
    "dx": 3,
    "dy": 3,
    "radius": 4
    },
    {
    "opaque": 100,
    "angle": 144,
    "dx": 6,
    "dy": 6,
    "radius": 4
    }
     */
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

    private fun go() {
        editTextBean?.let {
            it.align = freestyleLayoutView.getTextAlign()
            it.text = freestyleLayoutView.getContentText()?.toString() ?: ""
            it.textSize = freestyleLayoutView.getTextFontPxSize()
        }
        startActivityForResult(Intent(applicationContext, EditTextActivity::class.java).apply {
            val bundle = Bundle()
            bundle.putParcelable(KEY_DATA, editTextBean)
            this.putExtras(bundle)
        }, RC)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC) {
            data?.extras?.let { bundle ->
                editTextBean = bundle.getParcelable(KEY_DATA)
                editTextBean?.let {
                    freestyleLayoutView.newBuild(
                        viewType = FreestyleLayoutView.TEXT_VIEW,
                        textColor = it.color,
                        radius = applicationContext.resources.getDimension(R.dimen.common_fs_radius),
                        align = it.align,
                        backgroundStyle = if (it.backgroundColor != 0) {
                            FreestyleLayoutView.LINEAR_BACKGROUND
                        } else {
                            FreestyleLayoutView.NO_BACKGROUND
                        },
                        textBackgroundColor = it.backgroundColor,
                        text = it.text,
                        fsPaddingLeft = applicationContext.resources.getDimensionPixelOffset(R.dimen.common_fs_padding_left),
                        fsPaddingTop = applicationContext.resources.getDimensionPixelOffset(R.dimen.common_fs_padding_top),
                        fsPaddingRight = applicationContext.resources.getDimensionPixelOffset(R.dimen.common_fs_padding_right),
                        fsPaddingBottom = applicationContext.resources.getDimensionPixelOffset(R.dimen.common_fs_padding_bottom)
                    )
                    it.textSize.let { size ->
                        if (size > 0f) {
                            freestyleLayoutView.setTextFontPxSize(size)
                        }
                    }
                    freestyleLayoutView.reDraw()
                }
            }
        }
    }
}