package noid.simplify.ui.components.clientremarks

import android.os.Bundle
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.databinding.SheetJobRemarksBinding
import noid.simplify.ui.base.BaseSheetDialog
import noid.simplify.utils.extensions.observeData

@AndroidEntryPoint
class JobRemarkSheet : BaseSheetDialog<SheetJobRemarksBinding>({ SheetJobRemarksBinding.inflate(it) }) {

    var listener: ((remarks: String) -> Unit)? = null

    override fun SheetJobRemarksBinding.onViewCreated(savedInstanceState: Bundle?) {
        val viewModel: JobRemarksViewModel by viewModels()
        binding.viewModel = viewModel
        binding.close.setOnClickListener { dismiss() }
        binding.save.setOnClickListener {
            listener?.invoke(binding.viewModel?.remarks ?: "")
            dismiss()
        }
        observeViewModel()
    }

    private fun observeViewModel() {
        observeData(binding.viewModel?.remarksMediator) { binding.save.isEnabled = it }

    }

    companion object {

        fun newInstance() = JobRemarkSheet().apply {
        }
    }
}