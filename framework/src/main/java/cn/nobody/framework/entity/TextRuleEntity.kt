package cn.nobody.framework.entity

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by zpf on 2020/12/10.
 */
data class TextRuleEntity(
    var stroke: StrokeEntity? = null,
    var shadows: List<ShadowEntity>? = null
) : Parcelable {

    override fun toString(): String {
        return "stroke:$stroke , shadows:$shadows "
    }

    constructor(parcel: Parcel) : this(
        parcel.readParcelable(StrokeEntity::class.java.classLoader),
        parcel.createTypedArrayList(ShadowEntity)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(stroke, flags)
        parcel.writeTypedList(shadows)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TextRuleEntity> {
        override fun createFromParcel(parcel: Parcel): TextRuleEntity {
            return TextRuleEntity(parcel)
        }

        override fun newArray(size: Int): Array<TextRuleEntity?> {
            return arrayOfNulls(size)
        }
    }

}