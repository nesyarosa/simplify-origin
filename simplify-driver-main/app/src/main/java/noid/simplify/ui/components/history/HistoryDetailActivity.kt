package noid.simplify.ui.components.history

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.R
import noid.simplify.data.dto.JobDetail
import noid.simplify.data.network.DataUtil
import noid.simplify.data.network.Resource
import noid.simplify.databinding.ActivityHistoryDetailBinding
import noid.simplify.interfaces.OnLostConnection
import noid.simplify.ui.base.BaseActivity
import noid.simplify.ui.base.ToolbarBuilder
import noid.simplify.ui.components.jobdetail.JobDetailActivity
import noid.simplify.ui.components.jobdetail.JobNoteAdapter
import noid.simplify.ui.components.jobdetail.JobSubtaskAdapter
import noid.simplify.ui.components.jobdetail.PreviousNotesSheet
import noid.simplify.utils.Receipt
import noid.simplify.utils.RecyclerUtil
import noid.simplify.utils.Tools
import noid.simplify.utils.extensions.observeData
import noid.simplify.utils.extensions.observeError
import noid.simplify.utils.extensions.show
import noid.simplify.utils.extensions.visible

@AndroidEntryPoint
class HistoryDetailActivity : BaseActivity<ActivityHistoryDetailBinding>({ ActivityHistoryDetailBinding.inflate(it) }), OnLostConnection {

    private var jobId = 0
    private var jobDetail: JobDetail? = null
    private val noteAdapter by lazy { JobNoteAdapter() }
    private val checklistJobAdapter by lazy { JobSubtaskAdapter(isEnable = false) }

    override fun buildToolbar(): ToolbarBuilder {
        return ToolbarBuilder.Builder()
                .withToolbar(binding.appbar.toolbar)
                .withTitle(getString(R.string.history_detail))
                .withMenu(R.menu.history_menu)
                .withActionGoBack(true)
                .build()
    }

    override fun ActivityHistoryDetailBinding.onCreate(savedInstanceState: Bundle?) {
        this@HistoryDetailActivity.apply {
            this.jobId = intent.getIntExtra(JobDetailActivity.EXTRA_JOB_ID, 0)
            RecyclerUtil.setRecyclerLinear(this, listNotes, noteAdapter, isHasFixedSize = false)
            RecyclerUtil.setRecyclerLinear(this, listCheckListJob, checklistJobAdapter)
        }

        val viewModel: HistoryDetailViewModel by viewModels()
        binding.viewModel = viewModel
        binding.viewModel?.apply {
            this.fetchDetail(jobId)
        }

        binding.showPreviousNotes.setOnClickListener {
            PreviousNotesSheet.newInstance(jobId).show(supportFragmentManager)
        }

        observeViewModel()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.print -> {
                jobDetail?.let { Receipt.doPrint(this, it) }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRetry(url: String?) {
        binding.viewModel?.fetchDetail(jobId)
    }

    private fun observeViewModel() {
        observeData(binding.viewModel?.jobLiveData, ::handleResponseJob)
        observeError(binding.viewModel?.errorLiveData, supportFragmentManager, this)
    }

    private fun handleResponseJob(response: Resource<String>) {
        when (response) {
            is Resource.Loading -> {
                this.showLoading()
            }
            is Resource.Success -> {
                this.hideLoading()
                response.data?.let {
                    val result = Tools.getResponseStringByKey(data = it, key = "job")
                    jobDetail = DataUtil.toModel<JobDetail>(result)
                    noteAdapter.setJobStatus(jobDetail?.jobStatus)
                    binding.viewModel?.setJob(jobDetail)
                    jobDetail?.jobNotes?.let { notes -> noteAdapter. setItems(notes) }
                    checklistJobAdapter.setItems(jobDetail?.ChecklistJob)
                    binding.progressLayout.visible(checklistJobAdapter.items.isNotEmpty())
                    binding.textSubtaskMessage.visible(checklistJobAdapter.items.isEmpty())
                }
            }
            is Resource.DataError -> {
                this.hideLoading()
                binding.viewModel?.setErrorResponse(response.error)
            }
        }
    }

    companion object {
        fun open(context: Context, jobId: Int) {
            Intent(context, HistoryDetailActivity::class.java).also {
                it.putExtra(JobDetailActivity.EXTRA_JOB_ID, jobId)
                context.startActivity(it)
            }
        }
    }

}