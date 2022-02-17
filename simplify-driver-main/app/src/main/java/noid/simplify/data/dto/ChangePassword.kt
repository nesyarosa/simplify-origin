package noid.simplify.data.dto

import com.squareup.moshi.Json

data class ChangePassword(
    @Json(name = "newPassword")
    var newPassword: String = ""
)