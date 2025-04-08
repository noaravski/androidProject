package com.example.androidproject.model

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import com.google.gson.annotations.SerializedName

class Coin : Parcelable {

    @JvmField
    @SerializedName("currencyCode")
    var currencyCode: String? = null

    @JvmField
    @SerializedName("conversionValue")
    var conversionValue: Double? = null

    constructor(currencyCode: String?, conversionValue: Double?) {
        this.currencyCode = currencyCode
        this.conversionValue = conversionValue
    }

    protected constructor(`in`: Parcel) {
        currencyCode = `in`.readString()
        conversionValue = `in`.readValue(Double::class.java.classLoader) as Double?
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, i: Int) {
        dest.writeString(currencyCode)
        dest.writeValue(conversionValue)
    }

    companion object CREATOR : Creator<Coin> {
        override fun createFromParcel(parcel: Parcel): Coin {
            return Coin(parcel)
        }

        override fun newArray(size: Int): Array<Coin?> {
            return arrayOfNulls(size)
        }
    }

}