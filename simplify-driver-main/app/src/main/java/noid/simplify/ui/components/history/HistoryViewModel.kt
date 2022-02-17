package noid.simplify.ui.components.history

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import noid.simplify.constants.General
import noid.simplify.constants.Url
import noid.simplify.data.MainRepository
import noid.simplify.data.UserPreferences
import noid.simplify.data.dto.User
import noid.simplify.data.network.DataUtil
import noid.simplify.data.network.Resource
import noid.simplify.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
        private val repository: MainRepository,
        private val userPreferences: UserPreferences
) : BaseViewModel() {

    val historyLiveData = MutableLiveData<Resource<String>>()

    fun fetchHistory(offset: Int = 0) {
        val userId = DataUtil.toModel<User>(userPreferences.currentUser)?.id
        val query = HashMap<String, String>()
        query["ei"] = userId.toString()
        query["s"] = offset.toString()
        query["l"] = General.LIMIT_PER_LOAD.toString()
        query["j"] = "4"
        query["ob"] = "startDateTime"
        query["ot"] = "desc"

        viewModelScope.launch {
            historyLiveData.value = Resource.Loading()
            repository.fetch(Url.JOBS, query)
                    .collect { historyLiveData.value = it }
        }
    }

}