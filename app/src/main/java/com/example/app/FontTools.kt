package com.example.app

import android.content.Context
import android.graphics.Typeface


/**
 * Created by zpf on 2020/12/8.
 */
object FontTools {

    private val fontMap: HashMap<String, Typeface> by lazy { HashMap<String, Typeface>() }
    private const val KEY_RUBIK_BOLD_ITALIC = "fonts/Rubik-BoldItalic.ttf"
    private const val KEY_RUBIK_BOLD = "fonts/Rubik-Bold.ttf"

    fun getRubikItalic(context: Context): Typeface {
        var font: Typeface? = fontMap[KEY_RUBIK_BOLD_ITALIC]
        val typeface: Typeface =
            font ?: Typeface.createFromAsset(context.assets, KEY_RUBIK_BOLD_ITALIC)
        if (null == font) {
            font = typeface
            fontMap[KEY_RUBIK_BOLD_ITALIC] = font
        }
        return font
    }

    fun getRubikBold(context: Context): Typeface {
        var font: Typeface? = fontMap[KEY_RUBIK_BOLD]
        val typeface: Typeface =
            font ?: Typeface.createFromAsset(context.assets, KEY_RUBIK_BOLD)
        if (null == font) {
            font = typeface
            fontMap[KEY_RUBIK_BOLD] = font
        }
        return font
    }

    fun getDefaultFontItalic(): Typeface {
        return Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    fun getDefaultFont(): Typeface {
        return Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
}