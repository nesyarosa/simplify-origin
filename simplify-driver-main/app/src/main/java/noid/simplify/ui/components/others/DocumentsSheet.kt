package noid.simplify.ui.components.others

import android.os.Bundle
import noid.simplify.data.dto.Documents
import noid.simplify.databinding.SheetDocumentsBinding
import noid.simplify.ui.base.BaseSheetDialog
import noid.simplify.utils.RecyclerUtil
import noid.simplify.utils.extensions.downloadFile
import noid.simplify.utils.extensions.visible

class DocumentsSheet(doc : List<Documents>?) : BaseSheetDialog<SheetDocumentsBinding>({ SheetDocumentsBinding.inflate(it) }) {

    private val documents = doc

    override fun SheetDocumentsBinding.onViewCreated(savedInstanceState: Bundle?) {
        val docAdapter by lazy { DocumentsAdapter() }
        RecyclerUtil.setRecyclerLinear(requireContext(), binding.listDocs, docAdapter, isHasFixedSize = false)
        documents?.let { docAdapter.addItems(it) }
        docAdapter.downloadListener = {
            it.documentUrl?.let { url -> requireContext().downloadFile(url) }
        }
    }
}