package noid.simplify.utils.extensions

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.ColorInt
import androidx.fragment.app.FragmentManager
import noid.simplify.App
import noid.simplify.constants.DateFormat
import noid.simplify.constants.ErrorCode
import noid.simplify.constants.General
import noid.simplify.data.error.ErrorManager
import noid.simplify.data.error.ErrorResponse
import noid.simplify.data.error.mapper.ErrorMapper
import noid.simplify.data.network.DataUtil
import noid.simplify.interfaces.OnLostConnection
import noid.simplify.ui.components.others.LostConnectionSheet
import noid.simplify.utils.RegexUtil
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun CharSequence.isValidEmail() = !isNullOrBlank() && RegexUtil.EMAIL_PATTERN.matcher(this).matches()

fun CharSequence.isValidPassword() = !isNullOrBlank() && RegexUtil.PASSWORD_PATTERN.matcher(this).matches()

fun CharSequence.isValidContactNumber() = !isNullOrBlank() && length >= 8

fun String.isHasHTMLTags() = RegexUtil.HTML_PATTERN.matcher(this).find()

fun Float?.toRoundUp(): Float {
    val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.US)
    val formatter = numberFormat as DecimalFormat
    formatter.applyPattern("##0.00")
    return if (this != null || this != 0f) {
        formatter.format(this?.div(0.05f)?.let { kotlin.math.ceil(it) }?.times(0.05f))
            .toFloat()
    } else {
        0f
    }
}

fun String.toUrlWithQuery(params: List<Pair<String, Any?>>?): String {
    if (params == null) return this
    var query = ""
    for (paramPair in params) {
        if (paramPair.second == null) continue
        query += if (query.isEmpty()) {
            "?" + paramPair.first + "=" + paramPair.second.toString()
        } else {
            "&" + paramPair.first + "=" + paramPair.second.toString()
        }
    }
    return this + query
}


fun String?.toFormattedDate(dateFormat: String = DateFormat.FORMAT_DATE): Date? {
    return if (this == null) {
        null
    } else {
        try {
            val sdf = SimpleDateFormat(dateFormat, General.LOCALE_SINGAPORE)
            sdf.parse(this)
        } catch (e: Exception) {
            null
        }
    }
}

fun Date?.toFormattedString(dateFormat: String = DateFormat.FORMAT_DATE): String {
    return if (this == null) {
        "N/A"
    } else {
        val sdf = SimpleDateFormat(dateFormat, General.LOCALE_SINGAPORE)
        sdf.format(this)
    }
}

fun String?.toDateWithTimezone(dateFormat: String = DateFormat.FORMAT_DATETIME_FULL_LENGTH, dateFormatShow: String =DateFormat.FORMAT_DATE_TIME_LONG): String? {
    if (this == null) return null
    try {
        val read = SimpleDateFormat(dateFormat, Locale.getDefault())
        read.timeZone = TimeZone.getTimeZone("GMT")
        val date = read.parse(this) ?: return null
        val write = SimpleDateFormat(dateFormatShow, Locale.getDefault())
        write.timeZone = TimeZone.getTimeZone("Asia/Singapore")
        return write.format(date)
    } catch (e: ParseException) {
        return null
    }
}

fun Long?.getDateInString(dateFormat: String = DateFormat.FORMAT_DATE): String {
    if (this == null) return ""
    val date = Date(this)
    val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
    simpleDateFormat.timeZone = TimeZone.getDefault()
    return simpleDateFormat.format(date)
}

fun Any?.isNull(): Boolean {
    return this == null
}

fun Any?.isNotNull(): Boolean {
    return this != null
}

fun Any?.toJson(): String {
    return DataUtil.toJson(this)
}

fun ErrorResponse.show(fm: FragmentManager, onLostConnection: OnLostConnection? = null) {
    if (code == ErrorCode.NO_INTERNET_CONNECTION && onLostConnection != null) {
        val sheet = LostConnectionSheet.newInstance(url, onLostConnection)
        sheet.show(fm = fm, isCancelable = false)
        return
    }
    val err = ErrorManager(ErrorMapper())
    val msg = err.getError(code ?: ErrorCode.RUNTIME_ERROR_CODE).message
    val ctx = App.context.get()
    ctx.showToast(msg)
    if (code == ErrorCode.EXPIRED_TOKEN_AUTH) ctx.logout()
}

fun Double?.toPriceAmount(): String {
    return if (this == null || this <= 0f) {
        General.ZERO_PRICE
    } else {
        val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
        val formatter = numberFormat as DecimalFormat
        formatter.applyPattern("###,###,###.00")
        "$${(if (this < 1f) "0" else "")}${formatter.format(this)}"
    }
}

fun Double?.toPriceWithoutCurrency(): String {
    return if (this == null || this <= 0f) {
        "0.00"
    } else {
        val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
        val formatter = numberFormat as DecimalFormat
        formatter.applyPattern("###,###,###.00")
        "${(if (this < 1f) "0" else "")}${formatter.format(this)}"
    }
}

@Suppress("DEPRECATION")
fun Drawable?.setColorFilter(@ColorInt color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        this?.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
    } else {
        this?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }
}

fun String?.toDecimalFormatted(): String {
    if (this == null) return ""
    var str = this
    if (str[0] == '.') str = "0$str"
    val max = str.length
    var final = ""
    var after = false
    var i = 0
    var up = 0
    var decimal = 0
    var t: Char
    while (i < max) {
        t = str[i]
        if (t != '.' && !after) {
            up++
            if (up > 15) return final
        } else if (t == '.') {
            after = true
        } else {
            decimal++
            if (decimal > 2) return final
        }
        final += t
        i++
    }
    return final
}

fun String?.toDoubleOrZero(): Double {
    if (this == null) return 0.0

    return try {
        this.toDouble()
    } catch (e: Exception) {
        0.0
    }
}