package noid.simplify.ui.components.addtionalitem

import android.os.Bundle
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.R
import noid.simplify.data.dto.AdditionalService
import noid.simplify.data.dto.ServiceItem
import noid.simplify.data.dto.ServiceItemTemplate
import noid.simplify.data.network.Resource
import noid.simplify.databinding.SheetAdditionalItemsBinding
import noid.simplify.interfaces.OnLostConnection
import noid.simplify.ui.base.BaseSheetDialog
import noid.simplify.ui.components.jobdetail.ShareAdditionalItemViewModel
import noid.simplify.utils.RecyclerUtil
import noid.simplify.utils.extensions.observeData
import noid.simplify.utils.extensions.observeError
import noid.simplify.utils.extensions.showModal

@AndroidEntryPoint
class AdditionalItemsSheet: BaseSheetDialog<SheetAdditionalItemsBinding>({ SheetAdditionalItemsBinding.inflate(it) }), OnLostConnection {

    private val adapter by lazy { AdditionalItemsAdapter() }
    private val shareViewModel: ShareAdditionalItemViewModel by activityViewModels()
    private val viewModel: AdditionalItemsViewModel by viewModels()
    private val itemTemplates = mutableListOf<ServiceItemTemplate>()
    private var additionalService: AdditionalService? = null
    private val additionalItems = mutableListOf<ServiceItem>()
    private var oldTitle = ""

    var listener: (() -> Unit)? = null

    override fun SheetAdditionalItemsBinding.onViewCreated(savedInstanceState: Bundle?) {
        RecyclerUtil.setRecyclerLinear(context, binding.listAdditional, adapter, isHasFixedSize = false)
        adapter.setItems(additionalItems)
        observeViewModel()
        setWatchListener()

        binding.close.setOnClickListener {
            additionalService?.serviceTitle = oldTitle
            dismiss()
        }
        binding.serviceTitle.apply {
            this.editText?.doOnTextChanged { text, _, _, _ ->
                if (text.isNullOrBlank()) {
                    this.error = getString(R.string.field_required)
                    binding.save.isEnabled = false
                    return@doOnTextChanged
                }
                this.error = null
                additionalService?.serviceTitle = text.toString()
                binding.save.isEnabled = adapter.itemCount > 0
            }
        }
    }

    override fun onRetry(url: String?) {
        viewModel.saveAdditionalService(getAdditionServiceFormatted())
    }

    private fun observeViewModel() {
        observeData(shareViewModel.itemTemplatesLiveData, ::handleItemTemplatesData)
        observeData(shareViewModel.additionalServiceLiveData, ::handleAdditionalService)
        observeData(viewModel.responseSaveAdditionalLiveData, ::handleResponse)
        observeError(viewModel.errorLiveData, childFragmentManager, this)
    }

    private fun handleItemTemplatesData(list: List<ServiceItemTemplate>) {
        this.itemTemplates.clear()
        this.itemTemplates.addAll(list)
    }

    private fun handleAdditionalService(service: AdditionalService?) {
        oldTitle = service?.serviceTitle ?: ""
        additionalService = service
        binding.serviceTitle.editText?.setText(additionalService?.serviceTitle)
        binding.serviceTitle.editText?.isEnabled = additionalService?.serviceTitle.isNullOrBlank()
    }

    private fun setWatchListener() {
        this.adapter.editListener = { position, servItem ->
            val modalInput = InputAdditionalItemDialog.newInstance(
                    item = servItem,
                    itemTemplates = itemTemplates
            )
            modalInput.listener = { newItem ->
                adapter.items[position] = newItem
                adapter.notifyItemChanged(position)
            }
            modalInput.showModal(childFragmentManager)
        }

        this.adapter.deleteListener = { position ->
            adapter.deleteItem(position)
            binding.save.isEnabled = adapter.itemCount > 0
                    && !additionalService?.serviceTitle.isNullOrBlank()
        }

        this.binding.addItem.setOnClickListener {
            val modal = InputAdditionalItemDialog.newInstance(
                    itemTemplates = itemTemplates
            )
            modal.listener = {
                adapter.addItem(it)
                binding.save.isEnabled = adapter.itemCount > 0
                        && !additionalService?.serviceTitle.isNullOrBlank()
            }
            modal.showModal(childFragmentManager)
        }

        this.binding.save.setOnClickListener {
            viewModel.saveAdditionalService(getAdditionServiceFormatted())
        }

    }

    private fun getAdditionServiceFormatted(): AdditionalService? {
        additionalService?.serviceItems = adapter.items
        additionalService?.gstAmount = additionalService?.getGstAmountAdditionalItemsCalculated() ?: 0.0
        additionalService?.originalAmount = additionalService?.getTotalAdditionalItemsPrice() ?: 0.0
        additionalService?.totalAmount = additionalService?.getTotalAdditionalItemsPriceWithGst() ?: 0.0

        return additionalService
    }

    private fun handleResponse(response: Resource<String>) {
        when (response) {
            is Resource.Loading -> this.showLoading()
            is Resource.Success -> this.setAdditionaServiceAndDismiss()
            is Resource.DataError -> {
                this.hideLoading()
                binding.save.isEnabled = true
                viewModel.setErrorResponse(response.error)
            }
        }
    }

    private fun setAdditionaServiceAndDismiss() {
        this.hideLoading()
        this.listener?.invoke()
        dismiss()
    }

    companion object {
        fun newInstance(additionalItems: List<ServiceItem>) = AdditionalItemsSheet().apply {
            this.additionalItems.clear()
            this.additionalItems.addAll(additionalItems)
        }
    }
}