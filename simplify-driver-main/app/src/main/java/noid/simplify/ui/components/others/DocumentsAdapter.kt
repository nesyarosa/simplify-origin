package noid.simplify.ui.components.others

import android.annotation.SuppressLint
import noid.simplify.R
import noid.simplify.data.dto.Documents
import noid.simplify.databinding.ItemDocumentsBinding
import noid.simplify.ui.base.BaseAdapter
import noid.simplify.utils.extensions.visible

class DocumentsAdapter : BaseAdapter<Documents, ItemDocumentsBinding>() {

    override fun getLayout() = R.layout.item_documents


    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: Companion.BaseViewHolder<ItemDocumentsBinding>, position: Int) {
        holder.binding.item = items[position]
        holder.binding.notes.visible(!items[position].documentUrl.isNullOrEmpty())
        holder.binding.btnDownload.visible(!items[position].documentUrl.isNullOrEmpty())
        holder.binding.btnDownload.setOnClickListener {
            downloadListener?.invoke(items[position])
        }
    }
    var downloadListener: ((item: Documents) -> Unit)? = null
}