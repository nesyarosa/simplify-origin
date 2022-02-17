package noid.simplify.ui.components.jobdetail

import noid.simplify.R
import noid.simplify.constants.General
import noid.simplify.data.dto.Note
import noid.simplify.databinding.ItemNoteBinding
import noid.simplify.ui.base.BaseAdapter
import noid.simplify.utils.extensions.visible

class JobNoteAdapter : BaseAdapter<Note, ItemNoteBinding>() {

    private var status: String? = General.STATUS__ASSIGNED

    override fun getLayout() = R.layout.item_note

    override fun onBindViewHolder(holder: Companion.BaseViewHolder<ItemNoteBinding>, position: Int) {
        holder.binding.item = items[position]
        holder.binding.delete.visible(status == General.STATUS__IN_PROGRESS)
        holder.binding.download.visible(status == General.STATUS__IN_PROGRESS)
        holder.binding.delete.setOnClickListener {
            deleteNoteListener?.invoke(position)
        }
        holder.binding.download.visible(!items[position].imageUrl.isNullOrEmpty() && status == General.STATUS__IN_PROGRESS)
        holder.binding.download.setOnClickListener {
            downloadNoteListener?.invoke(items[position])
        }
    }

    var deleteNoteListener: ((position: Int) -> Unit)? = null
    var downloadNoteListener: ((items: Note) -> Unit)? = null

    fun setJobStatus(status: String?) {
        this.status = status
    }


}