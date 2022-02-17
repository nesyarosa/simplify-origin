package noid.simplify.ui.components.forgot

import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import noid.simplify.BR
import noid.simplify.constants.Url
import noid.simplify.data.MainRepository
import noid.simplify.data.dto.ChangePassword
import noid.simplify.data.network.Resource
import noid.simplify.ui.base.BaseViewModel
import noid.simplify.utils.extensions.isValidPassword
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val repository: MainRepository
) : BaseViewModel() {

    private var password = ""
    private var retypePassword = ""


    val isPasswordMatching = MutableLiveData<Boolean>()
    val doChangeLiveData = MutableLiveData<Resource<String>>()

    private val changePassword = ChangePassword()

    @Bindable
    fun getPassword(): String {
        return password
    }

    fun setPassword(value: String) {
        if (password != value) {
            password = value
            if (password.isValidPassword() && retypePassword.isNotBlank()) {
                isPasswordMatching.postValue(password == retypePassword)
                changePassword.newPassword = password
            }
            notifyPropertyChanged(BR.password)
        }
    }

    @Bindable
    fun getRetypePassword(): String {
        return retypePassword
    }

    fun setRetypePassword(value: String) {
        if (retypePassword != value) {
            retypePassword = value
            if (retypePassword.isValidPassword()) {
                isPasswordMatching.postValue(password == retypePassword)
                changePassword.newPassword = password
            } else {
                isPasswordMatching.postValue(false)
            }
            notifyPropertyChanged(BR.retypePassword)
        }
    }

    fun doChangePassword(){
        viewModelScope.launch {
            doChangeLiveData.value = Resource.Loading()
            repository.post(Url.CHANGE_PASSWORD, changePassword).collect {
                doChangeLiveData.value = it}
            }
        }
    }
