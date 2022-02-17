package noid.simplify.ui.components.payment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import noid.simplify.constants.Url
import noid.simplify.data.MainRepository
import noid.simplify.data.dto.JobToComplete
import noid.simplify.data.dto.JobUpdate
import noid.simplify.data.network.Resource
import noid.simplify.ui.base.BaseViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
        private val repository: MainRepository
) : BaseViewModel() {

    val jobUpdateLiveData = MutableLiveData<Resource<String>>()
    val getSignatureUrlLiveData = MutableLiveData<Resource<String>>()
    val uploadSignatureLiveData = MutableLiveData<Resource<String>>()

    fun getUrlUploadSignature(jobId: Int?, fileName: String) {
        if (jobId == null) return
        val job = JobUpdate(jobId = jobId)
        viewModelScope.launch {
            getSignatureUrlLiveData.value = Resource.Loading()
            repository.put("${Url.GET_URL_UPLOAD_SIGNATURE}/$fileName", job)
                    .collect { getSignatureUrlLiveData.value = it }
        }
    }

    fun uploadSignature(imageUrl: String?, file: File?) {
        if (imageUrl == null) return
        if (file == null) return
        viewModelScope.launch {
            uploadSignatureLiveData.value = Resource.Loading()
            repository.uploadImage(imageUrl, file)
                    .collect { uploadSignatureLiveData.value = it }
        }
    }

    fun doUpdateJob(jobId: Int, job: JobToComplete?) {
        if (job == null) return
        viewModelScope.launch {
            jobUpdateLiveData.value = Resource.Loading()
            repository.put("${Url.JOBS}/$jobId", job)
                    .collect { jobUpdateLiveData.value = it }
        }
    }
}