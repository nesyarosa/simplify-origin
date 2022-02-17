package noid.simplify.ui.components.clientremarks

import androidx.databinding.Bindable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import noid.simplify.BR
import noid.simplify.constants.Url
import noid.simplify.data.MainRepository
import noid.simplify.data.dto.RemarksUpdate
import noid.simplify.data.network.Resource
import noid.simplify.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class ClientRemarksViewModel @Inject constructor(
    private val repository: MainRepository
) : BaseViewModel() {

    private val _remarksMediator = MediatorLiveData<Boolean>()
    private val _updateLiveData = MutableLiveData<Resource<String>>()
    private val remarksUpdate = RemarksUpdate()

    val remarksMediator: LiveData<Boolean>
        get() =  _remarksMediator

    val updateLiveData: LiveData<Resource<String>>
        get() = _updateLiveData


    @get:Bindable
    var remarks: String?
        get() = remarksUpdate.remarks
        set(value) {
            remarksUpdate.remarks = value
            notifyPropertyChanged(BR.remarks)
            validateForm()
        }

    fun doSaveChanges(id: Int) {
        if (id == -1) return

        viewModelScope.launch {
            _updateLiveData.value = Resource.Loading()
            repository.put("${Url.CLIENT}/${id}", remarksUpdate)
                .collect { _updateLiveData.value = it }
        }
    }

    private fun validateForm() {
        _remarksMediator.value = !remarksUpdate.remarks.isNullOrBlank()
    }

}