package noid.simplify.ui.components.jobdetail

import android.os.Bundle
import androidx.core.os.bundleOf
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.databinding.SheetDescriptionServiceItemBinding
import noid.simplify.ui.base.BaseSheetDialog

@AndroidEntryPoint
class DescriptionServiceItemSheet: BaseSheetDialog<SheetDescriptionServiceItemBinding>({ SheetDescriptionServiceItemBinding.inflate(it) }) {

    override fun SheetDescriptionServiceItemBinding.onViewCreated(savedInstanceState: Bundle?) {
        val desc = arguments?.getString(EXTRA_DESC)
        binding.description.text = desc
        binding.close.setOnClickListener { dismiss() }
    }

    companion object {
        private const val EXTRA_DESC = "key.EXTRA_DESC"

        fun newInstance(desc: String) = DescriptionServiceItemSheet().apply {
            this.arguments = bundleOf(EXTRA_DESC to desc)
        }
    }
}