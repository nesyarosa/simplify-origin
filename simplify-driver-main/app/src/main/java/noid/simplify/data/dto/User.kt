package noid.simplify.data.dto

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

data class User(

    @Json(name="id")
    val id: Int,

    @Json(name="displayName")
    var displayName: String,

    @Json(name="contactNumber")
    var contactNumber: String,

    @Json(name="email")
    var email: String
)

@Parcelize
data class Skill(
    @Json(name="skill")
    val skill : String
) : Parcelable