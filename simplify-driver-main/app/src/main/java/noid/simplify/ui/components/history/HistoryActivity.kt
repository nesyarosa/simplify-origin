package noid.simplify.ui.components.history

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.R
import noid.simplify.constants.General
import noid.simplify.data.dto.JobList
import noid.simplify.data.network.DataUtil
import noid.simplify.data.network.Resource
import noid.simplify.databinding.ActivityHistoryBinding
import noid.simplify.interfaces.OnLostConnection
import noid.simplify.ui.base.BaseActivity
import noid.simplify.ui.base.ToolbarBuilder
import noid.simplify.ui.components.main.JobAdapter
import noid.simplify.utils.Tools
import noid.simplify.utils.extensions.observeData
import noid.simplify.utils.extensions.observeError
import noid.simplify.utils.extensions.toLinearVertical
import noid.simplify.utils.extensions.visible

@AndroidEntryPoint
class HistoryActivity : BaseActivity<ActivityHistoryBinding>({ ActivityHistoryBinding.inflate(it) }), OnLostConnection {

    private val adapter by lazy { JobAdapter() }
    private val viewModel: HistoryViewModel by viewModels()

    override fun buildToolbar(): ToolbarBuilder {
        return ToolbarBuilder.Builder()
            .withToolbar(binding.appbar.toolbar)
            .withTitle(getString(R.string.title_history))
            .withActionGoBack(true)
            .build()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun ActivityHistoryBinding.onCreate(savedInstanceState: Bundle?) {
        binding.swipeRefresh.setOnRefreshListener {
            adapter.items.clear()
            adapter.notifyDataSetChanged()
            viewModel.fetchHistory()
        }

        adapter.apply {
            this.listener = { _, _, job ->
                job.jobId?.let { HistoryDetailActivity.open(this@HistoryActivity, it) }
            }
            binding.listJob.toLinearVertical(viewAdapter = this)
        }

        viewModel.fetchHistory()
        observeViewModel()
    }

    override fun onRetry(url: String?) {
        viewModel.fetchHistory()
    }

    private fun observeViewModel() {
        observeData(viewModel.historyLiveData, ::handleResponseHistory)
        observeError(viewModel.errorLiveData, supportFragmentManager, this)
    }

    private fun handleResponseHistory(response: Resource<String>) {
        when (response) {
            is Resource.Loading -> {
                if (adapter.itemCount == 0)
                    binding.swipeRefresh.isRefreshing = true
            }
            is Resource.Success -> {
                binding.swipeRefresh.isRefreshing = false
                response.data?.let {
                    val result = Tools.getResponseStringByKey(data = it, key = "jobs")
                    val list = DataUtil.toList<JobList>(result)
                    this.adapter.addItems(list)
                    if (list.size == General.LIMIT_PER_LOAD) {
                        viewModel.fetchHistory(offset = adapter.itemCount)
                    }
                    this.setListViewAndState()
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

}