package noid.simplify.ui.components.addtionalitem

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import noid.simplify.constants.Url
import noid.simplify.data.MainRepository
import noid.simplify.data.dto.AdditionalService
import noid.simplify.data.network.Resource
import noid.simplify.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class AdditionalItemsViewModel @Inject constructor(
    private val repository: MainRepository,
) : BaseViewModel() {

    private val _responseSaveAdditionalLiveData = MutableLiveData<Resource<String>>()

    val responseSaveAdditionalLiveData: LiveData<Resource<String>>
        get() = _responseSaveAdditionalLiveData

    fun saveAdditionalService(service: AdditionalService?) {
        if (service == null) return
        viewModelScope.launch {
            _responseSaveAdditionalLiveData.value = Resource.Loading()
            repository.post(Url.ADDITIONAL_SERVICE, service)
                .collect { _responseSaveAdditionalLiveData.value = it }
        }
    }
}