package cn.nobody.framework.entity

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by zpf on 2020/12/10.
 */
data class StrokeEntity(val width: Float?, val strokeColor: Int?) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Float::class.java.classLoader) as? Float,
        parcel.readValue(Int::class.java.classLoader) as? Int
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(width)
        parcel.writeValue(strokeColor)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StrokeEntity> {
        override fun createFromParcel(parcel: Parcel): StrokeEntity {
            return StrokeEntity(parcel)
        }

        override fun newArray(size: Int): Array<StrokeEntity?> {
            return arrayOfNulls(size)
        }
    }
}