package noid.simplify.ui.components.payment

import android.os.Bundle
import android.view.WindowManager
import androidx.core.os.bundleOf
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.databinding.DialogQrCodeBinding
import noid.simplify.ui.base.BaseDialog
import noid.simplify.ui.base.ToolbarBuilder
import noid.simplify.utils.extensions.showImage


@AndroidEntryPoint
class QRCodeDialog: BaseDialog<DialogQrCodeBinding>({ DialogQrCodeBinding.inflate(it) }) {

    private var urlQrCode: String? = null
    private var originalBrightness: Float? = 0f
    private var params: WindowManager.LayoutParams? = null

    override fun buildToolbar(): ToolbarBuilder {
        return ToolbarBuilder.Builder().build()
    }

    override fun DialogQrCodeBinding.onViewCreated(savedInstanceState: Bundle?) {
        this@QRCodeDialog.apply {
            this.urlQrCode = arguments?.getString(EXTRA_QRCODE)
            this.params = requireActivity().window.attributes
            this.originalBrightness = params?.screenBrightness
            this.params?.screenBrightness = 1.0f
            this.requireActivity().window.attributes = params
            this.binding.qrcodeView.showImage(urlQrCode)
            this.dialog?.setCanceledOnTouchOutside(true)
        }

        binding.root.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        this.params?.screenBrightness = originalBrightness
        this.requireActivity().window.attributes = params
    }

    companion object {
        private const val EXTRA_QRCODE = "key.EXTRA_QRCODE"

        fun newInstance(urlQrCode: String?) = QRCodeDialog().apply {
            this.arguments = bundleOf(EXTRA_QRCODE to urlQrCode)
        }
    }
}