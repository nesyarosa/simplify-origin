package noid.simplify.ui.components.main

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.constants.General
import noid.simplify.data.dto.JobList
import noid.simplify.data.network.DataUtil
import noid.simplify.data.network.Resource
import noid.simplify.databinding.FragmentJobBinding
import noid.simplify.interfaces.OnLostConnection
import noid.simplify.ui.base.BaseFragment
import noid.simplify.ui.base.ToolbarBuilder
import noid.simplify.ui.components.jobdetail.JobDetailActivity
import noid.simplify.utils.Tools
import noid.simplify.utils.extensions.observeData
import noid.simplify.utils.extensions.observeError
import noid.simplify.utils.extensions.toLinearVertical
import noid.simplify.utils.extensions.visible

@AndroidEntryPoint
class JobFragment : BaseFragment<FragmentJobBinding>
    ({ FragmentJobBinding.inflate(it) }), OnLostConnection {

    private var indexTab = 0
    private var startDate: String? = null
    private var endDate: String? = null
    private var searchText = ""
    private var jobList: List<JobList> = emptyList()

    private val adapter by lazy { JobAdapter() }
    private val viewModel: JobViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun buildToolbar(): ToolbarBuilder {
        return ToolbarBuilder.Builder().build()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun FragmentJobBinding.onViewCreated(savedInstanceState: Bundle?) {
        arguments?.let {
            indexTab = it.getInt(EXTRA_INDEX_TAB)
            startDate = it.getString(EXTRA_START_DATE)
            endDate = it.getString(EXTRA_END_DATE)
        }

        binding.swipeRefresh.setOnRefreshListener {
            adapter.items.clear()
            adapter.notifyDataSetChanged()
            viewModel.fetchJobs(indexTab = indexTab, startDate, endDate)
        }

        adapter.apply {
            this.listener = { _, _, job ->
                job.jobId?.let {
                    openRequestStatusActivity.launch(JobDetailActivity.getIntent(requireContext(),
                        it)) }
            }
            binding.listJob.toLinearVertical(viewAdapter = this)
        }

        viewModel.fetchJobs(indexTab = indexTab, startDate, endDate)
        observeViewModel()

    }

    private val openRequestStatusActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.fetchJobs(indexTab = indexTab, startDate, endDate)
            }
        }

    override fun onRetry(url: String?) {
        viewModel.fetchJobs(indexTab = indexTab, startDate, endDate)
    }

    private fun observeViewModel() {
        observeData(mainViewModel.valueToFilterLiveData, ::setValueTextToFilter)
        observeData(viewModel.jobsLiveData, ::handleResponseJobs)
        observeError(viewModel.errorLiveData, childFragmentManager, this)
    }

    private fun setValueTextToFilter(text: String) {
        this.searchText = text
        this.setFilterList()
    }

    private fun setFilterList() {
        val filterList = mutableListOf<JobList>()
        if (searchText.isEmpty()) {
            filterList.addAll(jobList)
        } else {
            jobList.forEach { job ->
                if (job.clientName?.contains(searchText, ignoreCase = true) == true ||
                    job.serviceAddress?.contains(searchText, ignoreCase = true) == true ||
                    job.contactNumber?.contains(searchText, ignoreCase = true) == true
                ) {
                    filterList.add(job)
                }
            }
        }
        var jobActive: JobList? = null
        for ((index, value) in filterList.withIndex()) {
            if (value.jobStatus == General.STATUS__IN_PROGRESS) {
                filterList.removeAt(index)
                jobActive = value
                adapter.setJobActive(true)
                break
            }
        }
        jobActive?.let { filterList.add(0, jobActive) }
        if (jobActive == null) adapter.setJobActive(false)
        this.adapter.setItems(filterList)
        this.setListViewAndState()
    }

    private fun handleResponseJobs(response: Resource<String>) {
        when (response) {
            is Resource.Loading -> {
                binding.swipeRefresh.isRefreshing = true
            }
            is Resource.Success -> {
                binding.swipeRefresh.isRefreshing = false
                //disini edit untuk mengambil data dari json dan di parse jadi objek agar dimasukkan dalam list dan ditampilkan
                response.data?.let {
                    val result = Tools.getResponseStringByKey(data = it, key = "jobs")
                    val list = DataUtil.toList<JobList>(result)

                    // set list
                    this.jobList = list
                    this.setFilterList()
                }
            }
            is Resource.DataError -> {
                binding.swipeRefresh.isRefreshing = false
                viewModel.setErrorResponse(response.error)
                this.setListViewAndState()
            }
        }
    }

    private fun setListViewAndState() {
        binding.emptyState.visible(isVisible = adapter.items.isEmpty())
        binding.listJob.visible(isVisible = adapter.items.isNotEmpty())
    }

    companion object {
        private const val EXTRA_INDEX_TAB = "key.EXTRA_INDEX_TAB"
        private const val EXTRA_START_DATE = "key.EXTRA_START_DATE"
        private const val EXTRA_END_DATE = "key.EXTRA_END_DATE"

        fun newInstance(indexTab: Int, startDate: String? = null, endDate: String? = null): JobFragment {
            return JobFragment().apply {
                arguments = bundleOf(
                        EXTRA_INDEX_TAB to indexTab,
                        EXTRA_START_DATE to startDate,
                        EXTRA_END_DATE to endDate
                )
            }
        }
    }
}