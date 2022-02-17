package noid.simplify.ui.components.clientremarks

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.data.network.Resource
import noid.simplify.databinding.SheetRemarksBinding
import noid.simplify.interfaces.OnLostConnection
import noid.simplify.ui.base.BaseSheetDialog
import noid.simplify.utils.extensions.observeData
import noid.simplify.utils.extensions.observeError

@AndroidEntryPoint
class ClientRemarksSheet: BaseSheetDialog<SheetRemarksBinding>({ SheetRemarksBinding.inflate(it) }), OnLostConnection {

    private var clientId = -1

    var listener: ((remarks: String) -> Unit)? = null

    override fun SheetRemarksBinding.onViewCreated(savedInstanceState: Bundle?) {
        clientId = arguments?.getInt(CLIENT_ID) ?: -1
        val remarks = arguments?.getString(CLIENT_REMARKS)
        val viewModel: ClientRemarksViewModel by viewModels()
        binding.viewModel = viewModel
        binding.viewModel?.remarks = remarks
        binding.id = clientId

        binding.close.setOnClickListener { dismiss() }
        observeViewModel()
    }

    override fun onRetry(url: String?) {
        binding.viewModel?.doSaveChanges(clientId)
    }

    private fun observeViewModel() {
        observeData(binding.viewModel?.remarksMediator) { binding.save.isEnabled = it }
        observeData(binding.viewModel?.updateLiveData, ::handleResponse)
        observeError(binding.viewModel?.errorLiveData, childFragmentManager, this)
    }

    private fun handleResponse(response: Resource<String>) {
        when (response) {
            is Resource.Loading -> this.showLoading()
            is Resource.Success -> {
                this.hideLoading()
                this.listener?.invoke(binding.viewModel?.remarks ?: "")
                dismiss()
            }
            is Resource.DataError -> {
                this.hideLoading()
                binding.viewModel?.setErrorResponse(response.error)
            }
        }
    }

    companion object {
        private const val CLIENT_ID = "key.CLIENT_ID"
        private const val CLIENT_REMARKS = "key.CLIENT_REMARKS"

        fun newInstance(id: Int?, remarks: String?) = ClientRemarksSheet().apply {
            this.arguments = bundleOf(
                CLIENT_ID to id,
                CLIENT_REMARKS to remarks
            )
        }
    }
}