package noid.simplify.ui.components.jobdetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import noid.simplify.constants.Url
import noid.simplify.data.MainRepository
import noid.simplify.data.network.Resource
import noid.simplify.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class PreviousNotesViewModel @Inject constructor(
        private val repository: MainRepository,
) : BaseViewModel() {

    val previousNotesLiveData = MutableLiveData<Resource<String>>()

    fun fetchNotes(jobId: Int) {
        viewModelScope.launch {
            previousNotesLiveData.value = Resource.Loading()
            repository.fetch(url = "${Url.PREVIOUS_NOTES}/$jobId")
                    .collect { previousNotesLiveData.value = it }
        }
    }

}