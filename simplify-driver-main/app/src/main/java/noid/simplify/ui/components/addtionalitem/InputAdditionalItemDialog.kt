package noid.simplify.ui.components.addtionalitem

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.widget.doAfterTextChanged
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.R
import noid.simplify.data.dto.ServiceItem
import noid.simplify.data.dto.ServiceItemTemplate
import noid.simplify.databinding.DialogInputAdditionalItemBinding
import noid.simplify.ui.base.BaseDialog
import noid.simplify.ui.base.ToolbarBuilder
import noid.simplify.utils.extensions.toDecimalFormatted
import noid.simplify.utils.extensions.toDoubleOrZero
import noid.simplify.utils.extensions.toPriceWithoutCurrency

@AndroidEntryPoint
class InputAdditionalItemDialog: BaseDialog<DialogInputAdditionalItemBinding>({ DialogInputAdditionalItemBinding.inflate(it) }) {

    private var itemTemplates = mutableListOf<ServiceItemTemplate>()
    private var servItem: ServiceItem? = null
    private var oldItem: ServiceItem? = null
    private var isEdit = false

    var listener: ((item: ServiceItem) -> Unit)? = null

    override fun buildToolbar(): ToolbarBuilder {
        return ToolbarBuilder.Builder().build()
    }

    override fun DialogInputAdditionalItemBinding.onViewCreated(savedInstanceState: Bundle?) {
        isEdit = servItem != null

        if (servItem == null) {
            servItem = ServiceItem()
            binding.text.text = getString(R.string.add_additional_items)
            binding.add.text = getString(R.string.add)
        } else {
            binding.text.text = getString(R.string.edit_additional_items)
            binding.itemName.editText?.setText(servItem?.name)
            if (servItem?.id != 0) {
                binding.itemName.isEnabled = false
            }
            binding.description.editText?.setText(servItem?.description)
            binding.itemCost.editText?.setText(servItem?.unitPrice.toPriceWithoutCurrency())
            binding.itemQty.editText?.setText(servItem?.quantity.toString())
            binding.totalAmount.editText?.setText(servItem?.totalPrice.toPriceWithoutCurrency())
            binding.add.text = getString(R.string.save)
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, itemTemplates)

        (binding.itemName.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        (binding.itemName.editText as? AutoCompleteTextView)?.setOnItemClickListener { _, _, position, _ ->
            val price = itemTemplates[position].unitPrice.toDouble()
            servItem?.name = itemTemplates[position].name
            servItem?.unitPrice = price
            servItem?.quantity = 1
            binding.description.editText?.setText(servItem?.description)
            binding.itemCost.editText?.setText(servItem?.unitPrice.toPriceWithoutCurrency())
            binding.itemQty.editText?.setText(servItem?.quantity.toString())
            setViewValue()
        }

        binding.cancel.setOnClickListener {
            if (isEdit && oldItem != null) listener?.invoke(oldItem!!)
            dismiss()
        }
        binding.add.setOnClickListener {
            if (servItem != null) listener?.invoke(servItem!!)
            dismiss()
        }
        watchTextForm()
    }

    private fun watchTextForm() {
        binding.itemName.editText?.doAfterTextChanged {
            servItem?.name = it.toString()
        }

        binding.itemCost.apply {
            val price = servItem?.unitPrice ?: 0.0
            val text = if (price == 0.0) "" else price.toPriceWithoutCurrency()
            this.editText?.setText(text)
            this.editText?.doAfterTextChanged {
                val str = it.toString()
                if (str.isNotBlank()) {
                    val str2 = str.toDecimalFormatted()
                    if (str2 != str) {
                        this.editText?.setText(str2)
                        this.editText?.setSelection(str2.length)
                    }
                    servItem?.unitPrice = str2.toDoubleOrZero()
                }
                setViewValue()
            }
        }

        binding.description.apply {
            this.editText?.doAfterTextChanged {
                servItem?.description = it.toString()
                setViewValue()
            }
        }

        binding.itemQty.apply {
            this.editText?.doAfterTextChanged {
                val str = it.toString()
                servItem?.quantity = str.toDoubleOrZero().toInt()
                setViewValue()
            }
        }
    }

    private fun setViewValue() {
        servItem?.setTotalAmount()
        binding.totalAmount.editText?.setText(servItem?.totalPrice.toPriceWithoutCurrency())
        binding.add.isEnabled = !servItem?.name.isNullOrBlank() && (servItem?.quantity ?: 0) > 0
    }

    companion object {
        fun newInstance(item: ServiceItem? = null,
                        itemTemplates: List<ServiceItemTemplate>
        ) = InputAdditionalItemDialog().apply {
            this.oldItem = ServiceItem()
            this.oldItem?.id = item?.id ?: 0
            this.oldItem?.name = item?.name ?: ""
            this.oldItem?.description = item?.description
            this.oldItem?.unitPrice = item?.unitPrice ?: 0.0
            this.oldItem?.quantity = item?.quantity ?: 0
            this.oldItem?.totalPrice = item?.totalPrice ?: 0.0

            // set item
            this.servItem = item
            this.itemTemplates.clear()
            this.itemTemplates.addAll(itemTemplates)
        }
    }
}