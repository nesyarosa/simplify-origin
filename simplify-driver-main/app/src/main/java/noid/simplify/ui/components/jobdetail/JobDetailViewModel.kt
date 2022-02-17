package noid.simplify.ui.components.jobdetail

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.Bindable
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import noid.simplify.BR
import noid.simplify.R
import noid.simplify.constants.DateFormat
import noid.simplify.constants.Url
import noid.simplify.data.MainRepository
import noid.simplify.data.dto.*
import noid.simplify.data.network.Resource
import noid.simplify.ui.base.BaseViewModel
import noid.simplify.ui.components.intro.IntroActivity
import noid.simplify.utils.Tools
import noid.simplify.utils.extensions.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@HiltViewModel
class JobDetailViewModel @Inject constructor(
    private val repository: MainRepository,
) : BaseViewModel() {

    private var job: JobDetail? = null
    private val note = Note()

    val jobDetailLiveData = MutableLiveData<Resource<String>>()
    val itemTemplatesLiveData = MutableLiveData<Resource<String>>()
    val addNoteLiveData = MutableLiveData<Resource<String>>()
    val uploadNoteImgLiveData = MutableLiveData<Resource<String>>()
    val updateJobStatusLiveData = MutableLiveData<Resource<String>>()
    val deleteNoteLiveData = MutableLiveData<Resource<String>>()
    val getSignatureUrlLiveData = MutableLiveData<Resource<String>>()
    val uploadSignatureLiveData = MutableLiveData<Resource<String>>()
    val noteMediator = MediatorLiveData<Boolean>()

    fun fetchJob(jobId: Int) {
        viewModelScope.launch {
            jobDetailLiveData.value = Resource.Loading()
            repository.fetch(url = "${Url.JOBS}/$jobId")
                .collect { jobDetailLiveData.value = it }
        }
    }

    fun fetchServiceItemTemplates() {
        viewModelScope.launch {
            itemTemplatesLiveData.value = Resource.Loading()
            repository.fetch(Url.SERVICE_ITEM_TEMPLATES)
                .collect { itemTemplatesLiveData.value = it }
        }
    }

    fun setJob(job: JobDetail?) {
        if (job.isNull()) return
        this.job = job
        this.notifyPropertyChanged(BR.type)
        this.notifyPropertyChanged(BR.clientName)
        this.notifyPropertyChanged(BR.serviceName)
        this.notifyPropertyChanged(BR.dateTime)
        this.notifyPropertyChanged(BR.serviceAddress)
        this.notifyPropertyChanged(BR.vehicleNo)
        this.notifyPropertyChanged(BR.jobStatus)
        this.notifyPropertyChanged(BR.serviceItems)
        this.notifyPropertyChanged(BR.additionalItems)
        this.notifyPropertyChanged(BR.itemsEmpty)
        this.notifyPropertyChanged(BR.gstAmount)
        this.notifyPropertyChanged(BR.jobProgress)
        this.notifyPropertyChanged(BR.balanceAmount)
        this.notifyPropertyChanged(BR.totalAmount)
        this.notifyPropertyChanged(BR.grandTotal)
        this.notifyPropertyChanged(BR.subtaskEmpty)
        this.notifyPropertyChanged(BR.subTotalAdditionalItems)
        this.notifyPropertyChanged(BR.clientRemarks)
        this.notifyPropertyChanged(BR.progressPercentageInt)
        this.notifyPropertyChanged(BR.doneSubtask)
        this.notifyPropertyChanged(BR.totalSubtask)
        this.notifyPropertyChanged(BR.statusFormatted)
        this.notifyPropertyChanged(BR.statusIndicator)
        this.notifyPropertyChanged(BR.serviceType)
        this.notifyPropertyChanged(BR.notesEmpty)
        this.notifyPropertyChanged(BR.skill)
    }

    fun callCustomer(
        context: Context
    ) {
        val dialPhoneIntent = Intent(
            Intent.ACTION_DIAL,
            Uri.parse("tel:${job?.contactNumber}")
        )

        try {
            context.startActivity(dialPhoneIntent)
        } catch (ex: ActivityNotFoundException) {
            context.showInformationDialog(
                R.string.customer_phone_number,
                job?.contactNumber
            )
        }
    }

    private fun getImageName(): String {
        val timeStamp = SimpleDateFormat(DateFormat.FORMAT_DATE_FOR_IMAGE, Locale.getDefault()).format(Date())
        return "$timeStamp.jpg"
    }

    private fun validateFormNote() {
        noteMediator.value = !note.notes.isNullOrBlank()
    }

    fun doAddNote() {
        note.jobId = job?.jobId
        if (note.imageUrl.isNotNull()){
            note.image = getImageName()
            note.imageUrl = null
        } else {
            note.image = null
        }
        viewModelScope.launch {
            addNoteLiveData.value = Resource.Loading()
            repository.post(Url.JOB_NOTES, note)
                .collect { addNoteLiveData.value = it }
        }
    }

    @get:Bindable
    var notes: String
        get() = note.notes ?: ""
        set(value) {
            note.notes = value
            notifyPropertyChanged(BR.notes)
            validateFormNote()
        }

    @get:Bindable
    val statusIndicator : Int
    get() = job?.getStatusIndicator() ?: 0

    @get:Bindable
    val statusFormatted : String
    get() = job?.getStatusFormatted() ?: ""

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
    val skill : String?
    get() = job?.ServiceSkills?.joinToString (separator = "•",transform = { it.skill })

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
    val itemsEmpty: Boolean
        get() = job?.additionalServiceItem == null || (job?.additionalServiceItem?.isEmpty() ?: true)

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
        get() {
            return if(job?.vehicleJobs?.isNullOrEmpty() == true) "NA"
            else job?.vehicleJobs?.first()?.carplateNumber ?: "NA"
        }

    @get:Bindable
    val jobProgress: String
        get() = job?.jobProgress() ?: "NA"

    @get:Bindable
    val serviceItems: List<ServiceItem>
        get() = job?.serviceItem ?: emptyList()

    @get:Bindable
    val jobStatus: String
        get() = job?.jobStatus?.replace("_", " ") ?: ""


    @get:Bindable
    val subTotalAdditionalItems: String
        get() = job?.getSubTotalAdditionalItems().toPriceAmount()

    @get:Bindable
    val gstAmount: String
        get() = job?.getGstAmountForAdditional().toPriceAmount()

    @get:Bindable
    val totalAmount: String
        get() = job?.totalAmount().toPriceAmount()

    @get:Bindable
    val balanceAmount: String
        get() = job?.contractBalance.toPriceAmount()

    @get:Bindable
    val canEditServiceItems: Boolean
        get() = job?.isCanEditServiceItems() ?: false

    @get:Bindable
    val grandTotal: String
        get() {
            val contractBalance = job?.contractBalance ?: 0.0
            val balance = if (contractBalance < 0) 0.0 else contractBalance
            val grandTotal = balance + (job?.totalAmount() ?: 0.0)
            return grandTotal.toPriceAmount()
        }

    @get:Bindable
    val isSubtaskEmpty: Boolean
        get() {
            return job?.ChecklistJob==null
        }

    fun setNewNoteImage(str: String?) {
        this.note.imageUrl = str
    }

    fun setNoteId(id: Int) {
        this.note.id = id
    }

    fun uploadNoteImage(imageUrl: String, imagePath: String?) {
        if (imagePath == null) return
        val imageFile = File(imagePath)
        viewModelScope.launch {
            uploadNoteImgLiveData.value = Resource.Loading()
            repository.uploadImage(imageUrl, imageFile)
                .collect { uploadNoteImgLiveData.value = it }
        }
    }

    fun getUrlUploadSignature(jobId: Int?, fileName: String) {
        if (jobId == null) return
        val job = JobUpdate(jobId = jobId)
        viewModelScope.launch {
            getSignatureUrlLiveData.value = Resource.Loading()
            repository.put("${Url.GET_URL_UPLOAD_SIGNATURE}/$fileName", job)
                .collect { getSignatureUrlLiveData.value = it }
        }
    }

    fun doCompleteJob(jobId: Int, job: JobToComplete?) {
        if (job == null) return
        viewModelScope.launch {
            updateJobStatusLiveData.value = Resource.Loading()
            repository.put("${Url.JOBS}/$jobId", job)
                .collect { updateJobStatusLiveData.value = it }
        }
    }

    fun doUpdateJob(jobId: Int, job: JobToPaused?) {
        if (job == null) return
        viewModelScope.launch {
            updateJobStatusLiveData.value = Resource.Loading()
            repository.put("${Url.JOBS}/$jobId", job)
                .collect { updateJobStatusLiveData.value = it }
        }
    }

    fun doDeleteNote(noteDelete: NoteDelete) {
        viewModelScope.launch {
            deleteNoteLiveData.value = Resource.Loading()
            repository.delete("${Url.JOB_NOTES}/${noteDelete.id}")
                .collect { deleteNoteLiveData.value = it }
        }
    }

    fun uploadSignature(imageUrl: String, file: File) {
        viewModelScope.launch {
            uploadSignatureLiveData.value = Resource.Loading()
            repository.uploadImage(imageUrl, file)
                .collect { uploadSignatureLiveData.value = it }
        }
    }
}