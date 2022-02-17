package noid.simplify.data.dto

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize
import noid.simplify.constants.General
import noid.simplify.utils.extensions.toFormattedString
import java.util.*

@Parcelize
data class AdditionalService(

	@Json(name="additionalServiceId")
	val additionalServiceId: Int = 0,

	@Json(name="serviceType")
	val serviceType: String = "ADDITIONAL",

	@Json(name="serviceNumber")
	val serviceNumber: String = "123",

	@Json(name="repeatEndType")
	val repeatEndType: String = "AFTER",

	@Json(name="repeatEvery")
	val repeatEvery: Int = 1,

	@Json(name="needGST")
	val needGST: Boolean,

	@Json(name="serviceStatus")
	val serviceStatus: String = "PENDING",

	@Json(name="repeatEndOnDate")
	val repeatEndOnDate: String = Date().toFormattedString(),

	@Json(name="serviceAddressId")
	val serviceAddressId: Int,

	@Json(name="ServiceItems")
	var serviceItems: List<ServiceItem>?,

	@Json(name="gstAmount")
	var gstAmount: Double?,

	@Json(name="clientId")
	val clientId: Int,

	@Json(name="selectedDay")
	val selectedDay: String = "Day",

	@Json(name="entityId")
	val entityId: Int,

	@Json(name="termEnd")
	val termEnd: String = Date().toFormattedString(),

	@Json(name="jobId")
	val jobId: Int,

	@Json(name="termStart")
	val termStart: String = Date().toFormattedString(),

	@Json(name="totalAmount")
	var totalAmount: Double,

	@Json(name="originalAmount")
	var originalAmount: Double,

	@Json(name="serviceTitle")
	var serviceTitle: String = "",

	@Json(name="repeatEndAfter")
	val repeatEndAfter: Int = 1,

) : Parcelable {

	fun getTotalAdditionalItemsPrice(): Double {
		var total = 0.0
		serviceItems?.forEach { total += it.totalPrice }
		return total
	}

	fun getTotalAdditionalItemsPriceWithGst(): Double {
		val total = getTotalAdditionalItemsPrice()
		val gstAmount = getGstAmountAdditionalItemsCalculated()
		return total + gstAmount
	}

	fun getGstAmountAdditionalItemsCalculated(): Double {
		val isNeedGst = needGST
		return if (isNeedGst) {
			General.AMOUNT_GST * getTotalAdditionalItemsPrice()
		} else {
			0.0
		}
	}
}
