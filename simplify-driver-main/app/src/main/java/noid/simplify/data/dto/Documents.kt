package noid.simplify.data.dto

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
data class Documents(

    @Json(name="notes")
    val notes: String? = "",

    @Json(name="documentUrl")
    val documentUrl: String? = ""

    ) : Parcelable