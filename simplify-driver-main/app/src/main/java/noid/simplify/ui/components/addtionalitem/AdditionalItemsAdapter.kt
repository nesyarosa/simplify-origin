package noid.simplify.ui.components.addtionalitem

import noid.simplify.R
import noid.simplify.data.dto.ServiceItem
import noid.simplify.databinding.ItemAdditionalBinding
import noid.simplify.ui.base.BaseAdapter
import noid.simplify.utils.extensions.visibleOrInvisible

class AdditionalItemsAdapter : BaseAdapter<ServiceItem, ItemAdditionalBinding>() {

    override fun getLayout() = R.layout.item_additional

    override fun onBindViewHolder(holder: Companion.BaseViewHolder<ItemAdditionalBinding>, position: Int) {
        holder.binding.item = items[position]
        holder.binding.edit.setOnClickListener {
            editListener?.invoke(position, items[position])
        }
        holder.binding.delete.visibleOrInvisible(items[position].id == 0)
        holder.binding.delete.setOnClickListener {
            deleteListener?.invoke(position)
        }
    }

    var editListener: ((position: Int, item: ServiceItem) -> Unit)? = null
    var deleteListener: ((position: Int) -> Unit)? = null

}