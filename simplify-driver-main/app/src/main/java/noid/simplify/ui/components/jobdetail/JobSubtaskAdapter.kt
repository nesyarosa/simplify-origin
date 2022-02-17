package noid.simplify.ui.components.jobdetail

import android.annotation.SuppressLint
import android.widget.CheckBox
import noid.simplify.R
import noid.simplify.data.dto.CheckListJob
import noid.simplify.databinding.ItemSubtaskBinding
import noid.simplify.ui.base.BaseAdapter
import noid.simplify.utils.RecyclerUtil

class JobSubtaskAdapter(private var isEnable: Boolean = true) : BaseAdapter<CheckListJob, ItemSubtaskBinding>() {

    override fun getLayout() = R.layout.item_subtask

    override fun onBindViewHolder(
        holder: Companion.BaseViewHolder<ItemSubtaskBinding>,
        position: Int
    ) {
        val childAdapter by lazy { JobSubtaskChildAdapter(isEnable) }
        holder.binding.item = items[position]
        if (!isEnable) {
            holder.binding.checkbox.isChecked = isAllChildChecked(position)
        }

        if (!items[position].isDescriptionNull()) {
            holder.binding.description.setOnClickListener {
                itemListener?.invoke(items[position].description ?: "")
            }
        }

        // action for child adapter
        RecyclerUtil.setRecyclerLinear(
            holder.binding.root.context,
            holder.binding.childs,
            childAdapter
        )
        childAdapter.setItems(items[position].ChecklistJobItems)
        holder.binding.checkbox.isEnabled = isEnable
        holder.binding.checkbox.setOnClickListener {
            items[position].ChecklistJobItems?.forEachIndexed { index, sub ->
                sub.status = (it as CheckBox).isChecked
                childAdapter.update(sub, index)
            }
            taskStatusListener?.invoke(items[position])
        }
        childAdapter.onClick = {
            holder.binding.checkbox.isChecked = isAllChildChecked(position)
            taskStatusListener?.invoke(items[position])
        }
    }

    var taskStatusListener: ((item: CheckListJob) -> Unit)? = null

    var itemListener: ((desc: String) -> Unit)? = null

    private fun isAllChildChecked(position: Int): Boolean {
        return items[position].ChecklistJobItems?.count { it.status } == items[position].ChecklistJobItems?.count()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setEnable(status : Boolean){
        this.isEnable = status
        notifyDataSetChanged()
    }
}
