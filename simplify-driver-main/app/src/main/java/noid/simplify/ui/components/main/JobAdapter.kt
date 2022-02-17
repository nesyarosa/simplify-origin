package noid.simplify.ui.components.main

import android.annotation.SuppressLint
import noid.simplify.R
import noid.simplify.constants.General
import noid.simplify.data.dto.JobList
import noid.simplify.databinding.ItemJobBinding
import noid.simplify.ui.base.BaseAdapter
import noid.simplify.utils.extensions.visible

class JobAdapter : BaseAdapter<JobList, ItemJobBinding>() {

    private var isHasJobInProgress = false

    override fun getLayout() = R.layout.item_job

    override fun onBindViewHolder(holder: Companion.BaseViewHolder<ItemJobBinding>, position: Int) {
        holder.binding.item = items[position]
        holder.binding.progressLayout.visible(items[position].jobStatus != General.STATUS__COMPLETED)
        holder.binding.jobStatus.visible(items[position].jobStatus != General.STATUS__COMPLETED)
        holder.binding.main.apply {
            if (isHasJobInProgress) {
                this.disabled = items[position].jobStatus != General.STATUS__IN_PROGRESS
            }else{
                this.disabled = isHasJobInProgress
            }
            this.setOnClickListener { listener?.invoke(it, position, items[position]) }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setJobActive(isHas: Boolean) {
        this.isHasJobInProgress = isHas
        this.notifyDataSetChanged()
    }
}