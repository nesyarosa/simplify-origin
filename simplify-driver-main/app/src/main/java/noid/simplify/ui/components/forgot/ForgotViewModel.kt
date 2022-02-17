package noid.simplify.ui.components.forgot

import androidx.databinding.Bindable
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import noid.simplify.BR
import noid.simplify.constants.Url
import noid.simplify.data.MainRepository
import noid.simplify.data.dto.ForgotPassword
import noid.simplify.data.network.Resource
import noid.simplify.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class ForgotViewModel  @Inject constructor(
    private val repository: MainRepository
) : BaseViewModel() {

    private var forgotPassword = ForgotPassword(username = "")
    val doEmailLiveData = MutableLiveData<Resource<String>>()
    val emailMediator = MediatorLiveData<Boolean>()


    @Bindable
    fun getUsername(): String {
        return forgotPassword.username
    }
    @Bindable
    fun setUsername(value: String) {
        if (forgotPassword.username!= value) {
            forgotPassword.username = value
            notifyPropertyChanged(BR.username)
            validateForm()
        }
    }


    fun doSendEmail(){
        viewModelScope.launch {
            doEmailLiveData.value = Resource.Loading()
            repository.post(Url.FORGOT_PASSWORD, forgotPassword).collect {
                doEmailLiveData.value = it}
        }
    }

    private fun validateForm() {
        emailMediator.value = forgotPassword.isValid()
    }

}