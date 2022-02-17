package noid.simplify.ui.components.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import noid.simplify.constants.FilterBy
import noid.simplify.constants.Url
import noid.simplify.data.MainRepository
import noid.simplify.data.UserPreferences
import noid.simplify.data.dto.User
import noid.simplify.data.network.DataUtil
import noid.simplify.data.network.Resource
import noid.simplify.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class JobViewModel @Inject constructor(
    private val repository: MainRepository,
    private val userPreferences: UserPreferences
) : BaseViewModel() {

    val jobsLiveData = MutableLiveData<Resource<String>>()

    fun fetchJobs(indexTab: Int, startDate: String?, endDate: String?) {
        val userId = DataUtil.toModel<User>(userPreferences.currentUser)?.id
        val query = FilterBy.getQuery(indexTab, userId, startDate, endDate)

        viewModelScope.launch {
            jobsLiveData.value = Resource.Loading()
            repository.fetch(Url.JOBS, query)
                .collect { jobsLiveData.value = it }
        }
    }

}