package noid.simplify.ui.components.serviceitem

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.data.dto.ServiceItem
import noid.simplify.data.network.Resource
import noid.simplify.databinding.DialogEditServiceItemBinding
import noid.simplify.interfaces.OnLostConnection
import noid.simplify.ui.base.BaseDialog
import noid.simplify.ui.base.ToolbarBuilder
import noid.simplify.utils.extensions.observeData
import noid.simplify.utils.extensions.observeError
import noid.simplify.utils.extensions.toDecimalFormatted
import noid.simplify.utils.extensions.toPriceWithoutCurrency

@AndroidEntryPoint
class EditServiceItemDialog: BaseDialog<DialogEditServiceItemBinding>({ DialogEditServiceItemBinding.inflate(it) }), OnLostConnection {

    private var onDismissListener: OnDismissInputAdditionalListener? = null
    private var item: ServiceItem? = null
    private val viewModel: EditServiceItemViewModel by viewModels()

    override fun buildToolbar(): ToolbarBuilder {
        return ToolbarBuilder.Builder().build()
    }

    override fun DialogEditServiceItemBinding.onViewCreated(savedInstanceState: Bundle?) {
        item = requireArguments().getParcelable(EXTRA_SERVICE_ITEM)
        setValueView()
        setListenerOnView()

        // observeViewModel
        observeData(viewModel.updateLiveData, ::responseUpdate)
        observeError(
                event = viewModel.errorLiveData,
                fragmentManager = childFragmentManager,
                onLostConnection = this@EditServiceItemDialog
        )
    }

    override fun onRetry(url: String?) {
        item?.let { viewModel.doUpdate(item = it) }
    }

    private fun setValueView() {
        binding.itemName.editText?.setText(item?.name)
        binding.description.editText?.setText(item?.description)
        binding.itemQty.editText?.setText(item?.quantity.toString())
        binding.itemCost.editText?.setText(item?.unitPrice.toPriceWithoutCurrency())
        binding.totalAmount.editText?.setText(item?.totalPrice.toPriceWithoutCurrency())
    }

    private fun setListenerOnView() {
        binding.cancel.setOnClickListener {
            dismiss()
        }
        binding.save.setOnClickListener {
            item?.let {
                viewModel.doUpdate(item = it)
            }
        }
        binding.description.apply {
            this.editText?.doAfterTextChanged {
                item?.description = it.toString()
                binding.save.isEnabled = (item?.quantity ?: 0) > 0
            }
        }
        binding.itemCost.apply {
            this.editText?.doAfterTextChanged {
                val str = it.toString()
                if (str.isNotBlank()) {
                    val str2 = str.toDecimalFormatted()
                    if (str2 != str) {
                        this.editText?.setText(str2)
                        this.editText?.setSelection(str2.length)
                    }
                    item?.unitPrice = try {
                        str2.toDouble()
                    } catch (e: Exception) {
                        0.0
                    }
                    setTotalAmount()
                    binding.save.isEnabled = (item?.quantity ?: 0) > 0
                }
            }
        }
        binding.itemQty.apply {
            this.editText?.doAfterTextChanged {
                item?.quantity = it.toString().toIntOrNull() ?: 0
                setTotalAmount()
                binding.save.isEnabled = (item?.quantity ?: 0) > 0
            }
        }
    }

    private fun setTotalAmount() {
        item?.setTotalAmount()
        binding.totalAmount.editText?.setText(item?.totalPrice.toPriceWithoutCurrency())
    }

    private fun responseUpdate(response: Resource<String>) {
        when (response) {
            is Resource.Loading -> this.showLoading()
            is Resource.Success -> {
                this.hideLoading()
                item?.let { onDismissListener?.onDismiss(item = it) }
            }
            is Resource.DataError -> {
                this.hideLoading()
                viewModel.setErrorResponse(response.error)
            }
        }
    }

    interface OnDismissInputAdditionalListener {
        fun onDismiss(item: ServiceItem)
    }

    companion object {
        private const val EXTRA_SERVICE_ITEM = "key.EXTRA_SERVICE_ITEM"

        fun newInstance(item: ServiceItem? = null,
                        onDismissListener: OnDismissInputAdditionalListener? = null) = EditServiceItemDialog().apply {
            this.arguments = bundleOf(EXTRA_SERVICE_ITEM to item)
            this.onDismissListener = onDismissListener
        }
    }
}