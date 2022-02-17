package noid.simplify.ui.components.jobdetail

import noid.simplify.R
import noid.simplify.data.dto.ChecklistItems
import noid.simplify.databinding.ItemSubtaskChildBinding
import noid.simplify.ui.base.BaseAdapter

class JobSubtaskChildAdapter(private val isEnable: Boolean) : BaseAdapter<ChecklistItems, ItemSubtaskChildBinding>() {

    var onClick: (() -> Unit)? = null

    override fun getLayout() = R.layout.item_subtask_child

    override fun onBindViewHolder(
        holder: Companion.BaseViewHolder<ItemSubtaskChildBinding>,
        position: Int
    ) {
        holder.binding.item = items[position]
        holder.binding.checkbox.isEnabled = isEnable
        holder.binding.checkbox.setOnClickListener {
            onClick?.invoke()
        }
    }

}