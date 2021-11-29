package cn.nobody.framework.util

import android.util.Log

/**
 * Created by zpf on 3/12/21.
 */
object FreeLogUtils {

    private const val LOG_TAG = "FreeStyleTextView"
    var enableLog = true

    @JvmStatic
    fun v(msg: String) {
        if (enableLog) {
            Log.v(LOG_TAG, msg)
        }
    }

    @JvmStatic
    fun d(msg: String) {
        if (enableLog) {
            Log.d(LOG_TAG, msg)
        }
    }

    @JvmStatic
    fun i(msg: String) {
        if (enableLog) {
            Log.i(LOG_TAG, msg)
        }
    }

    @JvmStatic
    fun w(msg: String) {
        if (enableLog) {
            Log.w(LOG_TAG, msg)
        }
    }

    @JvmStatic
    fun e(msg: String) {
        if (enableLog) {
            Log.e(LOG_TAG, msg)
        }
    }
}