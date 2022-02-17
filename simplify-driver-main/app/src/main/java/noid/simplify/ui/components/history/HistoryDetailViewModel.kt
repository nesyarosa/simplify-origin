package noid.simplify.ui.components.history

import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import noid.simplify.BR
import noid.simplify.R
import noid.simplify.constants.Url
import noid.simplify.data.MainRepository
import noid.simplify.data.dto.JobDetail
import noid.simplify.data.dto.ServiceItem
import noid.simplify.data.network.Resource
import noid.simplify.ui.base.BaseViewModel
import noid.simplify.utils.Tools
import noid.simplify.utils.extensions.isNull
import noid.simplify.utils.extensions.toPriceAmount
import javax.inject.Inject

@HiltViewModel
class HistoryDetailViewModel @Inject constructor(
        private val repository: MainRepository
) : BaseViewModel() {

    private var job: JobDetail? = null

    val jobLiveData = MutableLiveData<Resource<String>>()

    fun fetchDetail(jobId: Int) {
        viewModelScope.launch {
            jobLiveData.value = Resource.Loading()
            repository.fetch(url = "${Url.JOBS}/$jobId")
                    .collect { jobLiveData.value = it }
        }
    }

    fun setJob(job: JobDetail?) {
        if (job.isNull()) return
        this.job = job
        this.notifyPropertyChanged(BR.type)
        this.notifyPropertyChanged(BR.clientName)
        this.notifyPropertyChanged(BR.remarks)
        this.notifyPropertyChanged(BR.serviceName)
        this.notifyPropertyChanged(BR.dateTime)
        this.notifyPropertyChanged(BR.serviceAddress)
        this.notifyPropertyChanged(BR.vehicleNo)
        this.notifyPropertyChanged(BR.serviceItems)
        this.notifyPropertyChanged(BR.additionalItems)
        this.notifyPropertyChanged(BR.jobStatus)
        this.notifyPropertyChanged(BR.gstAmount)
        this.notifyPropertyChanged(BR.totalAmount)
        this.notifyPropertyChanged(BR.itemsEmpty)
        this.notifyPropertyChanged(BR.balanceAmount)
        this.notifyPropertyChanged(BR.grandTotal)
        this.notifyPropertyChanged(BR.clientRemarks)
        this.notifyPropertyChanged(BR.progressPercentageInt)
        this.notifyPropertyChanged(BR.doneSubtask)
        this.notifyPropertyChanged(BR.totalSubtask)
        this.notifyPropertyChanged(BR.statusFormatted)
        this.notifyPropertyChanged(BR.statusIndicator)
        this.notifyPropertyChanged(BR.serviceType)
        this.notifyPropertyChanged(BR.notesEmpty)
        this.notifyPropertyChanged(BR.jobProgress)
        this.notifyPropertyChanged(BR.skill)
    }

    @get:Bindable
    val skill : String?
        get() = job?.ServiceSkills?.joinToString (separator = "•",transform = { it.skill })

    @get:Bindable
    val jobProgress: String
        get() = job?.jobProgress() ?: "NA"

    @get:Bindable
    val serviceType : String
        get() = job?.serviceType ?: ""

    @get:Bindable
    val clientRemarks : String?
        get() {
            return if(job?.clientRemarks.isNullOrEmpty()) Tools.getString(R.string.no_remarks_available)
            else job?.clientRemarks
        }


    @get:Bindable
    val remarks: String?
        get()  {
            return if(job?.remarks.isNullOrEmpty()) Tools.getString(R.string.no_remarks_available)
            else job?.remarks
        }

    @get:Bindable
    val doneSubtask : Int
        get() = job?.doneSubtask() ?: 0

    @get:Bindable
    val totalSubtask : Int
        get() = job?.totalSubtask() ?: 0

    @get:Bindable
    val progressPercentageInt : Int
        get() = job?.progressPercentageInt() ?: 0

    @get:Bindable
    val notesEmpty : Boolean
        get() = job?.notesEmpty() ?: true

    @get:Bindable
    val grandTotal: String
        get() {
            val contractBalance = job?.contractBalance ?: 0.0
            val balance = if (contractBalance < 0) 0.0 else contractBalance
            val grandTotal = balance + (job?.totalAmount() ?: 0.0)
            return grandTotal.toPriceAmount()
        }

    @get:Bindable
    val balanceAmount: String
        get() = job?.contractBalance.toPriceAmount()

    @get:Bindable
    val type: String
        get() = job?.serviceType ?: ""

    @get:Bindable
    val clientName: String
        get() = job?.clientName ?: ""

    @get:Bindable
    val serviceName: String
        get() = job?.serviceName ?: "NA"


    @get:Bindable
    val dateTime: String
        get() = job?.getDateTimeFormatted() ?: "-"

    @get:Bindable
    val serviceAddress: String
        get() = job?.getDestination() ?: ""

    @get:Bindable
    val vehicleNo: String
        get() = job?.vehicleJobs?.first()?.carplateNumber ?: "NA"

    @get:Bindable
    val serviceItems: List<ServiceItem>
        get() = job?.serviceItem ?: emptyList()

    @get:Bindable
    val additionalItems: List<ServiceItem>
        get() = job?.additionalServiceItem ?: emptyList()

    @get:Bindable
    val itemsEmpty: Boolean
        get() = job?.additionalServiceItem == null || (job?.additionalServiceItem?.isEmpty() ?: true)

    @get:Bindable
    val jobStatus: String
        get() = job?.jobStatus ?: ""

    @get:Bindable
    val gstAmount: String
        get() = getGstAmountCalculated().toPriceAmount()

    @get:Bindable
    val totalAmount: String
        get() = getTotalAmountWithGst().toPriceAmount()


    private fun getTotalAmountWithoutGst(): Double {
        var total = 0.0
        serviceItems.forEach { total += it.totalPrice }
        additionalItems.forEach { total += it.totalPrice }
        return total
    }

    private fun getGstAmountCalculated(): Double {
        val isNeedGst = job?.needGST ?: false
        return if (isNeedGst) {
            (7.0/100.0) * getTotalAmountWithoutGst()
        } else {
            0.0
        }
    }

    private fun getTotalAmountWithGst(): Double {
        return getTotalAmountWithoutGst() + getGstAmountCalculated()
    }

}