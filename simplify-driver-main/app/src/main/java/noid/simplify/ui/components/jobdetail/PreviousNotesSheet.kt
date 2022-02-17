package noid.simplify.ui.components.jobdetail

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.data.dto.Note
import noid.simplify.data.network.DataUtil
import noid.simplify.data.network.Resource
import noid.simplify.databinding.SheetPreviousNotesBinding
import noid.simplify.interfaces.OnLostConnection
import noid.simplify.ui.base.BaseSheetDialog
import noid.simplify.utils.RecyclerUtil
import noid.simplify.utils.Tools
import noid.simplify.utils.extensions.observeData
import noid.simplify.utils.extensions.observeError
import noid.simplify.utils.extensions.visible

@AndroidEntryPoint
class PreviousNotesSheet: BaseSheetDialog<SheetPreviousNotesBinding>({ SheetPreviousNotesBinding.inflate(it) }), OnLostConnection {

    private var jobId = -1
    private val noteAdapter by lazy { JobNoteAdapter() }
    private val viewModel: PreviousNotesViewModel by viewModels()

    override fun SheetPreviousNotesBinding.onViewCreated(savedInstanceState: Bundle?) {
        jobId = arguments?.getInt(JobDetailActivity.EXTRA_JOB_ID) ?: -1
        RecyclerUtil.setRecyclerLinear(requireContext(), binding.listNotes, noteAdapter, isHasFixedSize = false)
        viewModel.fetchNotes(jobId)
        observeViewModel()
    }

    override fun onRetry(url: String?) {
        viewModel.fetchNotes(jobId)
    }

    private fun observeViewModel() {
        observeData(viewModel.previousNotesLiveData, ::handleResponse)
        observeError(viewModel.errorLiveData, childFragmentManager, this)
    }

    private fun handleResponse(response: Resource<String>) {
        when (response) {
            is Resource.Loading -> binding.progress.visible(true)
            is Resource.Success -> {
                binding.progress.visible(false)
                response.data?.let {
                    val result = Tools.getResponseStringByKey(data = it, key = "jobNotes")
                    val list = DataUtil.toList<Note>(result)
                    noteAdapter.apply {
                        this.setItems(list)
                        binding.listNotes.visible(this.itemCount > 0)
                        binding.emptyState.visible(this.itemCount == 0)
                    }
                }
            }
            is Resource.DataError -> {
                binding.progress.visible(false)
                viewModel.setErrorResponse(response.error)
            }
        }
    }

    companion object {
        fun newInstance(jobId: Int) = PreviousNotesSheet().apply {
            this.arguments = bundleOf(JobDetailActivity.EXTRA_JOB_ID to jobId)
        }
    }
}