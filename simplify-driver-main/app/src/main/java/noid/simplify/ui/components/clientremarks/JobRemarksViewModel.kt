package noid.simplify.ui.components.clientremarks

import androidx.databinding.Bindable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import noid.simplify.BR
import noid.simplify.data.dto.RemarksUpdate
import noid.simplify.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class JobRemarksViewModel @Inject constructor() : BaseViewModel() {

    private val _remarksMediator = MediatorLiveData<Boolean>()
    private val remarksUpdate = RemarksUpdate()

    val remarksMediator: LiveData<Boolean>
        get() =  _remarksMediator


    @get:Bindable
    var remarks: String?
        get() = remarksUpdate.remarks
        set(value) {
            remarksUpdate.remarks = value
            notifyPropertyChanged(BR.remarks)
            validateForm()
        }


    private fun validateForm() {
        _remarksMediator.value = !remarksUpdate.remarks.isNullOrBlank()
    }

}