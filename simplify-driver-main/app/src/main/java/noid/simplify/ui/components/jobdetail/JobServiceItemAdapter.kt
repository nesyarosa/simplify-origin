package noid.simplify.ui.components.jobdetail

import android.annotation.SuppressLint
import noid.simplify.R
import noid.simplify.data.dto.ServiceItem
import noid.simplify.databinding.ItemServiceBinding
import noid.simplify.ui.base.BaseAdapter
import noid.simplify.utils.extensions.visible

class JobServiceItemAdapter : BaseAdapter<ServiceItem, ItemServiceBinding>() {

    private var isHidePrice = false
    private var isCanEdit = false

    override fun getLayout() = R.layout.item_service

    override fun onBindViewHolder(holder: Companion.BaseViewHolder<ItemServiceBinding>, position: Int) {
        holder.binding.item = items[position]
        holder.binding.itemPrice.visible(!isHidePrice)
        holder.binding.edit.visible(isCanEdit)
        if (!items[position].isDescriptionNull()) {
            holder.binding.itemName.setCompoundDrawablesWithIntrinsicBounds(
                0, 0, R.drawable.ic_info_fill, 0
            )
            holder.binding.itemName.setOnClickListener {
                itemListener?.invoke(items[position].description ?: "")
            }
        }

        holder.binding.edit.setOnClickListener {
            editListener?.invoke(position, items[position])
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setHidePrice(isHidePrice: Boolean) {
        this.isHidePrice = isHidePrice
        this.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setEditable(isCanEdit: Boolean) {
        this.isCanEdit = isCanEdit
        this.notifyDataSetChanged()
    }

    var itemListener: ((desc: String) -> Unit)? = null

    var editListener: ((position: Int, item: ServiceItem) -> Unit)? = null

}