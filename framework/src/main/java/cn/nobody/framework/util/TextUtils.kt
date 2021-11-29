package cn.nobody.framework.util

/**
 * Created by zpf on 2021/6/22.
 */
object TextUtils {

    private const val REGEX_EN_CHARS = "[a-zA-Z0-9,.?/;:~`!@#$%^&*() \\n\\-_\\+=<>\\[\\]{}|\\\\]+"
    val regex = Regex(REGEX_EN_CHARS, RegexOption.IGNORE_CASE)

}