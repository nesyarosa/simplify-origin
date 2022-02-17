package noid.simplify.ui.components.jobdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import noid.simplify.data.dto.AdditionalService
import noid.simplify.data.dto.ServiceItemTemplate
import noid.simplify.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class ShareAdditionalItemViewModel @Inject constructor() : BaseViewModel() {

    private val _itemTemplatesLiveData = MutableLiveData<List<ServiceItemTemplate>>()
    private val _additionalServiceLiveData = MutableLiveData<AdditionalService>()

    val itemTemplatesLiveData: LiveData<List<ServiceItemTemplate>>
        get() = _itemTemplatesLiveData

    val additionalServiceLiveData: LiveData<AdditionalService>
        get() = _additionalServiceLiveData

    fun setItemTemplates(list: List<ServiceItemTemplate>) {
        this._itemTemplatesLiveData.value = list
    }

    fun setAdditionalService(service: AdditionalService?) {
        this._additionalServiceLiveData.value = service
    }
}