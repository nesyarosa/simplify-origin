package noid.simplify.ui.components.login

import android.view.View
import androidx.databinding.Bindable
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import noid.simplify.BR
import noid.simplify.constants.ErrorCode
import noid.simplify.constants.Url
import noid.simplify.data.MainRepository
import noid.simplify.data.dto.Login
import noid.simplify.data.dto.Notification
import noid.simplify.data.network.Resource
import noid.simplify.ui.base.BaseViewModel
import noid.simplify.ui.components.forgot.ForgotActivity
import noid.simplify.utils.extensions.startNewActivity
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
        private val repository: MainRepository
) : BaseViewModel() {

    val loginLiveData = MutableLiveData<Resource<String>>()
    val updateTokenLiveData = MutableLiveData<Resource<String>>()
    val loginMediator = MediatorLiveData<Boolean>()

    private val login = Login(email = "", password = "")

    @Bindable
    fun getEmail(): String {
        return login.email
    }

    fun setEmail(value: String) {
        if (login.email != value) {
            login.email = value
            notifyPropertyChanged(BR.email)
            validateForm()
        }
    }

    @Bindable
    fun getPassword(): String {
        return login.password
    }

    fun setPassword(value: String) {
        if (login.password != value) {
            login.password = value
            notifyPropertyChanged(BR.password)
            validateForm()
        }
    }

    fun doLogin() {
        viewModelScope.launch {
            loginLiveData.value = Resource.Loading()
            repository.post(Url.LOGIN, login)
                .collect { loginLiveData.value = it }
        }
    }

    fun doUpdateToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                updateTokenLiveData.value = Resource.DataError(url = null, errorCode = ErrorCode.RUNTIME_ERROR_CODE)
                return@OnCompleteListener
            }

            val token = task.result
            viewModelScope.launch {
                updateTokenLiveData.value = Resource.Loading()
                repository.put(Url.UPDATE_TOKEN, Notification(token = token ?: ""))
                        .collect { updateTokenLiveData.value = it }
            }
        })
    }

    fun openForgotActivity(view: View) {
        view.context.startNewActivity(ForgotActivity::class.java)
    }

    private fun validateForm() {
        loginMediator.value = login.isValid()
    }
}