package com.example.app

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import cn.nobody.framework.view.FreestyleLayoutView

/**
 * Created by zpf on 2020/12/9.
 */
data class EditTextBean(
    var text: String? = "",
    var align: Int = FreestyleLayoutView.ALIGN_CENTER,
    var color: Int = Color.BLACK,
    var backgroundColor: Int = Color.TRANSPARENT,
    var textSize: Float = 0f,
    var isBold: Boolean = false,
    var isItalic: Boolean = false,
    var selectColorPosition: Int = 0,
    var widgetId: Int = View.NO_ID
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        parcel.writeInt(align)
        parcel.writeInt(color)
        parcel.writeInt(backgroundColor)
        parcel.writeFloat(textSize)
        parcel.writeByte(if (isBold) 1 else 0)
        parcel.writeByte(if (isItalic) 1 else 0)
        parcel.writeInt(selectColorPosition)
        parcel.writeInt(widgetId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<EditTextBean> {
        override fun createFromParcel(parcel: Parcel): EditTextBean {
            return EditTextBean(parcel)
        }

        override fun newArray(size: Int): Array<EditTextBean?> {
            return arrayOfNulls(size)
        }
    }

}