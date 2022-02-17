package noid.simplify.data.dto

import com.squareup.moshi.Json
import noid.simplify.utils.extensions.isValidEmail

data class Login(
    @Json(name="username")
    var email: String,

    @Json(name="password")
    var password: String
) {
    fun isValid(): Boolean {
        return (email.isValidEmail() && password.isNotBlank())
    }
}

data class ForgotPassword(
    @Json(name="username")
    var username: String
){
    fun isValid(): Boolean {
        return (username.isValidEmail())
    }}