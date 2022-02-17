package noid.simplify.ui.components.profile

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
import noid.simplify.data.UserPreferences
import noid.simplify.data.dto.User
import noid.simplify.data.network.DataUtil
import noid.simplify.data.network.Resource
import noid.simplify.ui.base.BaseViewModel
import noid.simplify.utils.extensions.isNotNull
import noid.simplify.utils.extensions.isNull
import noid.simplify.utils.extensions.isValidContactNumber
import noid.simplify.utils.extensions.toJson
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: MainRepository,
    private val userPreferences: UserPreferences
) : BaseViewModel() {

    val userMediator = MediatorLiveData<Boolean>()
    val updateProfileLiveData = MutableLiveData<Resource<String>>()

    private var user: User? = null

    init {
        user = DataUtil.toModel<User>(userPreferences.currentUser)
    }

    @get:Bindable
    var displayName: String
        get() = user?.displayName ?: ""
        set(value) {
            user?.displayName = value
            notifyPropertyChanged(BR.displayName)
            validateForm()
        }

    @get:Bindable
    var contactNumber: String
        get() = user?.contactNumber ?: ""
        set(value) {
            user?.contactNumber = value
            notifyPropertyChanged(BR.contactNumber)
            validateForm()
        }

    @get:Bindable
    var email: String
        get() = user?.email ?: ""
        set(value) {
            user?.email = value
            notifyPropertyChanged(BR.email)
        }


    private fun validateForm() {
        userMediator.value = user.isNotNull() &&
                user!!.displayName.isNotBlank() &&
                user!!.contactNumber.isValidContactNumber()
    }

    fun doUpdateProfile() {
        if (user.isNull()) return

        viewModelScope.launch {
            updateProfileLiveData.value = Resource.Loading()
            repository.put("${Url.USERS}/${user?.id}", user!!)
                    .collect { updateProfileLiveData.value = it }
        }
    }

    fun saveUserToLocal() {
        val userStr = user.toJson()
        userPreferences.currentUser = userStr
    }
}