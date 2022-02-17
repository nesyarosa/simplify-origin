package noid.simplify.data.dto

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize
import noid.simplify.utils.extensions.toPriceAmount

@Parcelize
data class ServiceItem(

    @Json(name="id")
    var id: Int? = 0,

    @Json(name="unitPrice")
    var unitPrice: Double = 0.0,

    @Json(name="quantity")
    var quantity: Int = 0,

    @Json(name="totalPrice")
    var totalPrice: Double = 0.0,

    @Json(name="name")
    var name: String = "",

    @Json(name="description")
    var description: String? = "",

) : Parcelable {

    fun isDescriptionNull(): Boolean {
        return description.isNullOrBlank()
    }

    fun getTotalPriceFormatted(): String {
        return totalPrice.toPriceAmount()
    }

    fun setTotalAmount() {
        val subTotal = quantity.toDouble() * unitPrice
        val discount = 0.0
        this.totalPrice = subTotal - discount
    }

    fun getItemWithServiceTypeChunked(): String {
        val sb = StringBuilder()
        val itemChunked = name.chunked(23)
        for (i in itemChunked.indices) {
            sb.append(itemChunked[i].trim())
            if (i != itemChunked.lastIndex) {
                sb.append("\n")
            }
        }
        return sb.toString()
    }

    fun getQty(): String {
        return "$quantity x ${this.unitPrice.toPriceAmount()}"
    }
}
