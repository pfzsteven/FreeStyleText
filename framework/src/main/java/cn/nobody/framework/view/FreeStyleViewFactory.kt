package cn.nobody.framework.view

import android.content.Context
import android.util.AttributeSet

/**
 * Created by zpf on 2020/12/3.
 */
object FreeStyleViewFactory {

    fun buildView(
        viewType: Int,
        context: Context,
        attrs: AttributeSet?
    ): FreeStyleView {
        return when (viewType) {
            FreestyleLayoutView.EDIT_VIEW -> {
                FreeStyleEditTextView(context, attrs).freeStyleView
            }
            else -> {
                FreeStyleAppCompatTextView(context, attrs).freeStyleView
            }
        }
    }
}