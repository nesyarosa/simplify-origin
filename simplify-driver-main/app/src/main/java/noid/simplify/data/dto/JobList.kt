package noid.simplify.data.dto

import androidx.core.content.ContextCompat
import com.squareup.moshi.Json
import noid.simplify.App
import noid.simplify.R
import noid.simplify.constants.DateFormat
import noid.simplify.constants.General
import noid.simplify.utils.extensions.toDateWithTimezone
import noid.simplify.utils.extensions.toRoundUp
import java.text.SimpleDateFormat
import java.util.*

data class JobList(

	@Json(name = "jobId")
	val jobId: Int? = 0,

	@Json(name = "startDateTime")
	val startDateTime: String? = "",

	@Json(name = "endDateTime")
	val endDateTime: String? = "",

	@Json(name = "startDateTimeMobile")
	val startDateTimeMobile: String? = "",

	@Json(name = "endDateTimeMobile")
	val endDateTimeMobile: String? = "",

	@Json(name = "jobStatus")
	val jobStatus: String? = "",

	@Json(name = "assignedBy")
	val assignedBy: Int? = 0,

	@Json(name = "additionalServiceId")
	val additionalServiceId: Int? = 0,

	@Json(name = "collectedAmount")
	val collectedAmount: Double? = 0.0,

	@Json(name = "serviceId")
	val serviceId: Int? = 0,

	@Json(name = "serviceType")
	val serviceType: String? = "",

	@Json(name = "serviceName")
	val serviceName: String? = "",

	@Json(name = "totalJob")
	val totalJob: Int? = 0,

	@Json(name = "needGST")
	val needGST: Boolean? = false ,

	@Json(name = "discountAmount")
	val discountAmount: Int? = 0 ,

	@Json(name = "totalServiceAmount")
	val totalServiceAmount: Double? = 0.0 ,

	@Json(name = "invoiceCollectedAmount")
	val invoiceCollectedAmount: Int? = 0 ,

	@Json(name = "invoiceNumber")
	val invoiceNumber: String? = "" ,

	@Json(name = "invoiceStatus")
	val invoiceStatus: String? = "" ,

	@Json(name = "clientId")
	val clientId: Int? = 0 ,

	@Json(name = "clientName")
	val clientName: String? = "",

	@Json(name = "clientRemarks")
	val clientRemarks: String? = "",

	@Json(name = "billingAddress")
	val billingAddress: String? = "",

	@Json(name = "billingFloorNo")
	val billingFloorNo: String? = "",

	@Json(name = "billingUnitNo")
	val billingUnitNo: String? = "",

	@Json(name = "serviceAddress")
	val serviceAddress: String? = "",

	@Json(name = "serviceFloorNo")
	val serviceFloorNo: String? = "",

	@Json(name = "serviceUnitNo")
	val serviceUnitNo: String? = "",

	@Json(name = "postalCode")
	val postalCode: String? = "",

	@Json(name = "jobCountry")
	val jobCountry: String? = "",

	@Json(name = "contactPerson")
	val contactPerson: String? = "",

	@Json(name = "UserProfileId")
	val UserProfileId: Int? = 0,

	@Json(name = "contactNumber")
	val contactNumber: String? = "",

	@Json(name = "employee")
	val employee: List<String>? = emptyList(),

	@Json(name = "employeeId")
	val employeeId: List<String>? = emptyList(),

	@Json(name = "vehicles")
	val vehicles: List<String>? = emptyList(),

	@Json(name = "vehiclesId")
	val vehiclesId: List<String>? = emptyList(),

	@Json(name = "serviceJobCount")
	val serviceJobCount : String? = "0",

	@Json(name = "doneJob")
	val doneJob : String? = "",

	@Json(name = "ServiceItem")
	val ServiceItem : List<ServiceItem>? = emptyList(),

	@Json(name = "assginedBy")
	val assginedBy : Int? = 0,

	@Json(name = "vehicleId")
	val vehicleId : List<Int>? = emptyList(),

	@Json(name = "serviceSkills")
	val serviceSkills : String? = "",

	@Json(name="ChecklistJob")
	val ChecklistJob : List<CheckListJob>? = emptyList(),

	@Json(name = "isLastJob")
	val isLastJob : Boolean? = false
) {

	fun getStatusFormatted(): String? {
		val context = App.context.get()

		return if (jobStatus == General.STATUS__IN_PROGRESS) {
			context?.getString(R.string.in_progress)
		} else if (jobStatus == General.STATUS__COMPLETED) {
			context?.getString(R.string.completed)
		} else if (jobStatus == General.STATUS__ASSIGNED) {
			if (isOverdueJob) {
				context?.getString(R.string.overdue_job)
			} else {
				context?.getString(R.string.upcoming_job)
			}
		}else if (jobStatus == General.STATUS__CANCELLED){
			context?.getString(R.string.cancelled)
		}else if(jobStatus == General.STATUS__PAUSED){
			context?.getString(R.string.paused)
		}
		else{
			context?.getString(R.string.un_assigned)
		}
	}


	fun getStatusIndicator(): Int {
		val context = App.context.get() ?: return -1

		return if (jobStatus == General.STATUS__IN_PROGRESS) {
			ContextCompat.getColor(context, android.R.color.holo_orange_light)
		} else if (jobStatus == General.STATUS__COMPLETED) {
			ContextCompat.getColor(context, android.R.color.holo_green_dark)
		} else if (jobStatus == General.STATUS__CANCELLED) {
			ContextCompat.getColor(context, android.R.color.holo_red_dark)
		} else if (jobStatus == General.STATUS__ASSIGNED){
			if (isOverdueJob) {
				ContextCompat.getColor(context, android.R.color.holo_red_dark)
			} else {
				ContextCompat.getColor(context, R.color.primary)
			}
		} else if(jobStatus == General.STATUS__PAUSED){
			ContextCompat.getColor(context, android.R.color.darker_gray)
		}
		else{
			ContextCompat.getColor(context, android.R.color.darker_gray)
		}
	}

	fun getAddressFormatted(): String {
		return "$serviceAddress"
	}

	fun startDateToShow() : String? {
		return "$startDateTimeMobile".toDateWithTimezone(dateFormatShow = DateFormat.FORMAT_DATE_LONG)
	}
	fun startTimeToShow() : String? {
		return "$startDateTimeMobile".toDateWithTimezone(dateFormatShow = DateFormat.FORMAT_TIME)
	}

	private val isOverdueJob: Boolean
		get() {
			val dateTime = "$startDateTimeMobile".toDateWithTimezone(dateFormatShow = DateFormat.FORMAT_DATETIME_FULL_LENGTH)
			val sdf = SimpleDateFormat(DateFormat.FORMAT_DATETIME_FULL_LENGTH, Locale.getDefault())
			return if(dateTime != null){
				val strDate = sdf.parse(dateTime)
				Date().after(strDate)
			}else{
				val strDate = sdf.parse("$startDateTimeMobile")
				Date().after(strDate)
			}
		}

	fun progressPercentageInt(): Int = progressPercentageFloat().toInt()

	private fun doneJob() : Int = doneJob?.toInt() ?: 0

	private fun totalJob() : Int = serviceJobCount?.toInt() ?: 0

	private fun progressPercentageFloat(): Float =
		(doneJob().toFloat().times(100f)).div(totalJob().toFloat()).toRoundUp()
}
