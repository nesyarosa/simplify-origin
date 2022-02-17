package noid.simplify.ui.components.equipment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import noid.simplify.constants.Url
import noid.simplify.data.MainRepository
import noid.simplify.data.UserPreferences
import noid.simplify.data.dto.Equipment
import noid.simplify.data.dto.User
import noid.simplify.data.network.DataUtil
import noid.simplify.data.network.Resource
import noid.simplify.ui.base.BaseViewModel
import noid.simplify.utils.extensions.isNull
import javax.inject.Inject

@HiltViewModel
class EquipmentDetailViewModel @Inject constructor(

    private val repository: MainRepository
) : BaseViewModel() {

    val equipmentLiveData = MutableLiveData<Resource<String>>()

    fun fetchEquipment(idEquip: Int){
        viewModelScope.launch {
            equipmentLiveData.value = Resource.Loading()
            repository.fetch(url= "${Url.EQUIPMENT}/$idEquip")
                .collect { equipmentLiveData.value = it }
        }
    }
}