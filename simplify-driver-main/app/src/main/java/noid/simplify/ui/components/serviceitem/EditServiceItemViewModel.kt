package noid.simplify.ui.components.serviceitem

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import noid.simplify.constants.Url
import noid.simplify.data.MainRepository
import noid.simplify.data.dto.ServiceItem
import noid.simplify.data.network.Resource
import noid.simplify.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class EditServiceItemViewModel @Inject constructor(
        private val repository: MainRepository,
) : BaseViewModel() {

    val updateLiveData = MutableLiveData<Resource<String>>()

    fun doUpdate(item: ServiceItem) {
        viewModelScope.launch {
            updateLiveData.value = Resource.Loading()
            repository.put("${Url.SERVICE_ITEMS}/${item.id}", item)
                    .collect { updateLiveData.value = it }
        }
    }
}