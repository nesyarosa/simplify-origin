package noid.simplify.ui.components.others

import android.os.Bundle
import androidx.core.os.bundleOf
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.databinding.SheetLostConnectionBinding
import noid.simplify.interfaces.OnLostConnection
import noid.simplify.ui.base.BaseSheetDialog
import noid.simplify.ui.components.main.MainActivity

@AndroidEntryPoint
class LostConnectionSheet: BaseSheetDialog<SheetLostConnectionBinding>({ SheetLostConnectionBinding.inflate(it) }) {

    private var onLostConnection: OnLostConnection? = null

    override fun SheetLostConnectionBinding.onViewCreated(savedInstanceState: Bundle?) {
        val url = arguments?.getString(EXTRA_URL)
        binding.close.setOnClickListener {
            dismiss()
            if (requireActivity() !is MainActivity) requireActivity().finish()
        }
        binding.retry.setOnClickListener {
            dismiss()
            onLostConnection?.onRetry(url = url)
        }
    }

    companion object {
        const val EXTRA_URL = "key.EXTRA_URL"

        fun newInstance(url: String?, onLostConnection: OnLostConnection?) = LostConnectionSheet().apply {
            this.arguments = bundleOf(EXTRA_URL to url)
            this.onLostConnection = onLostConnection
        }
    }
}