package noid.simplify.data.dto

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
data class Vehicle (
    @Json(name="id")
    val id: Int? = 0,

    @Json(name="carplateNumber")
    val carplateNumber: String? = null,

    ) : Parcelable
