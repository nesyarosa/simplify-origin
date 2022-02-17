package noid.simplify.data.dto

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize
import noid.simplify.constants.General

@Parcelize
data class JobToComplete(

	@Json(name="jobStatus")
	var jobStatus: String = General.STATUS__IN_PROGRESS,

	@Json(name="collectedAmount")
	var collectedAmount: Double = 0.0,

	@Json(name="signature")
	var signature: String = "",

	@Json(name="paymentMethod")
	var paymentMethod: String? = General.METHOD__CASH,

	@Json(name="startDateTime")
	val startTime: String? = "",

	@Json(name="endDateTime")
	val endTime: String? = "",

	@Json(name="vehicle")
	val vehicle: List<Vehicle>? = emptyList(),

	@Json(name="remarks")
	var remarks: String? = null,

	@Json(name="employee")
	var employee: List<Employee>? = emptyList(),

	@Json(name="ChecklistJob")
	var CheckListJob : List<CheckListJob>? = emptyList(),

	@Json(name="location")
	var location: String = ""

) : Parcelable
