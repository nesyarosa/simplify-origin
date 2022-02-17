package noid.simplify.ui.components.main

import android.content.Context
import androidx.databinding.Bindable
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
import noid.simplify.ui.components.history.HistoryActivity
import noid.simplify.utils.extensions.isNull
import noid.simplify.utils.extensions.startNewActivity
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val repository: MainRepository
) : BaseViewModel() {

    val valueToFilterLiveData = MutableLiveData<String>()

    val logoutLiveData = MutableLiveData<Resource<String>>()

    private var user: User? = null


    fun loadProfile() {
        user = DataUtil.toModel<User>(userPreferences.currentUser)
        notifyPropertyChanged(BR.initialName)
        notifyPropertyChanged(BR.displayName)
        notifyPropertyChanged(BR.contactNumber)
        notifyPropertyChanged(BR.email)
    }

    @get:Bindable
    var text: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.text)
            valueToFilterLiveData.value = value
        }

    @get:Bindable
    val displayName: String
        get() = user?.displayName ?: ""


    @get:Bindable
    val contactNumber: String
        get() = user?.contactNumber ?: ""

    @get:Bindable
    val email: String
        get() = user?.email ?: ""

    @Bindable
    fun getInitialName(): String {
        if (user.isNull()) return "NA"
        if (user!!.displayName.isBlank()) return "NA"
        return user!!.displayName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .reduce { acc, s -> acc + s }
    }

    fun logout() {
        viewModelScope.launch {
            logoutLiveData.value = Resource.Loading()
            repository.post(Url.LOGOUT)
                .collect { logoutLiveData.value = it }
        }
    }

    fun openHistoryActivity(context: Context) {
        context.startNewActivity(HistoryActivity::class.java)
    }

}