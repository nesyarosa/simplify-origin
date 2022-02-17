package noid.simplify.data.dto

import android.os.Parcelable
import androidx.core.content.ContextCompat
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize
import noid.simplify.App
import noid.simplify.R
import noid.simplify.constants.DateFormat
import noid.simplify.constants.General
import noid.simplify.utils.extensions.*
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class JobDetail(

    @Json(name="startDateTime")
    val startDateTime : String? = "",

    @Json(name="endDateTime")
    val endDateTime : String? = "",

    @Json(name="userProfileId")
    val userProfileId : Int? = 0,

    @Json(name="remarks")
    val remarks : String? = null,

    @Json(name="contactPerson")
    val contactPerson : String? = "",

    @Json(name="contactEmail")
    val contactEmail : String? ="",

    @Json(name="secondaryContactPerson")
    val secondaryContactPerson : String? = "",

    @Json(name="secondaryContactNumber")
    val secondaryContactNumber : String? ="",

    @Json(name="secondaryContactEmail")
    val secondaryContactEmail : String? ="",

    @Json(name="thirdContactPerson")
    val thirdContactPerson : String? = "",

    @Json(name="thirdContactNumber")
    val thirdContactNumber : String? = "",

    @Json(name="thirdContactEmail")
    val thirdContactEmail : String? = "",

    @Json(name="fourthContactPerson")
    val fourthContactPerson : String? = "",

    @Json(name="fourthContactNumber")
    val fourthContactNumber : String? = "",

    @Json(name="fourthContactEmail")
    val fourthContactEmail : String? = "",

    @Json(name="gstAmountService")
    val gstAmountService : Double? = 0.0,

    @Json(name="discountAmount")
    val discountAmount : Int? = 0,

    @Json(name="entityLogo")
    val entityLogo : String? = "",

    @Json(name="displayName")
    val displayName : String? = "",

    @Json(name="ChecklistJob")
    val ChecklistJob : List<CheckListJob>? = emptyList(),

    @Json(name="country")
    val country: String? = null,

    @Json(name="jobStatus")
    var jobStatus: String? = "",

    @Json(name="clientName")
    val clientName: String? = null,

    @Json(name="clientRemarks")
    var clientRemarks: String? = null,

    @Json(name="postalCode")
    val postalCode: String? = null,

    @Json(name="jobDate")
    val jobDate: String? = null,

    @Json(name="needGST")
    val needGST: Boolean? = null,

    @Json(name="contactNumber")
    val contactNumber: String? = null,

    @Json(name="startTime")
    val startTime: String? = null,

    @Json(name="vehicleJobs")
    val vehicleJobs: List<Vehicle>? = emptyList(),

    @Json(name="serviceAddressId")
    val serviceAddressId: Int? = null,

    @Json(name="clientId")
    val clientId: Int? = null,

    @Json(name="serviceAddress")
    val serviceAddress: String? = null,

    @Json(name="serviceFloorNo")
    val serviceFloorNo: String? = null,

    @Json(name="serviceUnitNo")
    val serviceUnitNo: String? = null,

    @Json(name="entityId")
    val entityId: Int? = null,

    @Json(name = "doneJob")
    val doneJob : String? = "",

    @Json(name = "serviceJobCount")
    val serviceJobCount: String? = "0",

    @Json(name="entityName")
    val entityName: String? = null,

    @Json(name="entityAddress")
    val entityAddress: String? = null,

    @Json(name="entityContactNumber")
    val entityContactNumber: String? = null,

    @Json(name="entityEmail")
    val entityEmail: String? = null,

    @Json(name="jobId")
    val jobId: Int? = null,

    @Json(name="vehicleNo")
    val vehicleNo: String? = null,

    @Json(name="endTime")
    val endTime: String? = null,

    @Json(name="ServiceItem")
    val serviceItem: List<ServiceItem>? = emptyList(),

    @Json(name="jobNotes")
    val jobNotes: List<Note>? = null,

    @Json(name="collectedAmount")
    var collectedAmount: Double? = 0.0,

    @Json(name="paymentMethod")
    val paymentMethod: String? = General.METHOD__CASH,

    @Json(name="signatureUrl")
    var signatureUrl: String? = null,

    @Json(name="contractBalance")
    val contractBalance: Double? = 0.0,

    @Json(name="employee")
    val employee: List<Employee>? = emptyList(),

    @Json(name="serviceId")
    val serviceId: String? = null,

    @Json(name="serviceName")
    val serviceName: String? = null,

    @Json(name="additionalServiceId")
    val additionalServiceId: Int? = null,

    @Json(name="additionalServiceTitle")
    val additionalServiceTitle: String? = null,

    @Json(name="AdditionalServiceItem")
    var additionalServiceItem: List<ServiceItem>? = emptyList(),

    @Json(name="serviceType")
    val serviceType: String? = null,

    @Json(name="totalAmountService")
    var totalAmountService: Double? = 0.0,

    @Json(name="invoiceNumber")
    val invoiceNumber: String? = null,

    @Json(name="paynowQrcode")
    val paynowQrcode: String? = null,

    @Json(name = "startDateTimeMobile")
    val startDateTimeMobile: String? = "",

    @Json(name = "endDateTimeMobile")
    val endDateTimeMobile: String? = "",

    @Json(name = "jobDocuments")
    val jobDocuments: List<Documents>? = emptyList(),

    @Json(name = "ServiceSkills")
    val ServiceSkills: List<Skill>? = emptyList()

): Parcelable {

    fun getChecklistJobDone() : Boolean = ChecklistJob?.find { !it.getStatus() } == null

    fun getStatusFormatted() : String? {
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

    fun getDateTimeFormatted(): String {
        val startDate = "$startDateTimeMobile".toDateWithTimezone(dateFormat = DateFormat.FORMAT_DATETIME_FULL_LENGTH)
        val endDate ="$endDateTimeMobile".toDateWithTimezone(dateFormat = DateFormat.FORMAT_DATETIME_FULL_LENGTH)
        return "$startDate - $endDate"
    }

    fun getDestination(): String {
        return "$serviceAddress"
    }

    fun gstAmount() : String = getGstAmountForAdditional().toPriceAmount()

    fun balanceAmount(): String = contractBalance.toPriceAmount()

    fun notesEmpty(): Boolean = jobNotes == null || (jobNotes.isEmpty() )

    fun grandTotal(): String {
        val contractBalance = contractBalance ?: 0.0
        val balance = if (contractBalance < 0) 0.0 else contractBalance
        val grandTotal = balance + (getGrandTotalAdditionalItems())
        return grandTotal.toPriceAmount()
    }

    fun getSubTotalAdditionalItems(): Double {
        var total = 0.0
        additionalServiceItem?.forEach { total += it.totalPrice }
        return total
    }

    private fun getSubTotalServiceItems(): Double {
        var total = 0.0
        serviceItem?.forEach { total += it.totalPrice }
        return total
    }

    private fun getAmount(): Double {
        return getSubTotalServiceItems()+getSubTotalAdditionalItems()
    }

    fun getGstAmountForAdditional(): Double {
        val isNeedGst = needGST ?: false
        return if (isNeedGst) {
            General.AMOUNT_GST * getSubTotalAdditionalItems()
        } else {
            0.0
        }
    }

    fun getGrandTotalAdditionalItems(): Double {
        return getAmount()+ getGstAmountForAdditional()
    }

    fun getAmountDue(): Double {
        val contractBalance = contractBalance ?: 0.0
        val balance = if (contractBalance < 0) 0.0 else contractBalance
        return balance + totalAmount()
    }


    fun totalAmount(): Double {
        return getGstAmountForAdditional()+getSubTotalAdditionalItems()
    }

    fun isCanEditServiceItems(): Boolean {
        return jobStatus == General.STATUS__IN_PROGRESS &&
                serviceType == General.SERVICE_TYPE__ADHOC &&
                invoiceNumber.isNullOrBlank()
    }

    fun jobProgress() : String {
        return "$doneJob / $serviceJobCount"
    }

    fun progressPercentageInt(): Int = progressPercentageFloat().toInt()

    fun doneSubtask() : Int {
        var subtaskTot = 0
        ChecklistJob?.forEach { list ->
           list.ChecklistJobItems?.let {  subtaskTot += it.count { item -> item.status } }
        }
        return subtaskTot
    }

    fun totalSubtask() : Int {
        var subtaskTot = 0
            ChecklistJob?.forEach { list ->
                list.ChecklistJobItems?.let{
                    subtaskTot += it.count()
                }
            }
        return subtaskTot
    }

    private fun progressPercentageFloat(): Float =
        (doneSubtask().toFloat().times(100f)).div(totalSubtask().toFloat()).toRoundUp()
}