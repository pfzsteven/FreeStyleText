package cn.nobody.framework.entity

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable

/**
 * Created by zpf on 2020/12/10.
 */
data class ShadowEntity(
    val opaque: Int = 100,
    val angle: Int = 180,
    val dx: Float = 0f,
    val dy: Float = 0f,
    val radius: Float = 0f,
    val shadowColor: Int = Color.TRANSPARENT
) : Parcelable {

    override fun toString(): String {
        return "opaque:$opaque,angle:$angle,dx:$dx,dy:$dy,radius:$radius,shadowColor:$shadowColor"
    }

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(opaque)
        parcel.writeInt(angle)
        parcel.writeFloat(dx)
        parcel.writeFloat(dy)
        parcel.writeFloat(radius)
        parcel.writeInt(shadowColor)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShadowEntity> {
        override fun createFromParcel(parcel: Parcel): ShadowEntity {
            return ShadowEntity(parcel)
        }

        override fun newArray(size: Int): Array<ShadowEntity?> {
            return arrayOfNulls(size)
        }
    }
}