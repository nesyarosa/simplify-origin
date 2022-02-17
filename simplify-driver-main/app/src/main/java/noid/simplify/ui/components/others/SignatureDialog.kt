package noid.simplify.ui.components.others

import android.graphics.Bitmap
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.databinding.DialogSignatureBinding
import noid.simplify.ui.base.BaseDialog
import noid.simplify.ui.base.ToolbarBuilder

@AndroidEntryPoint
class SignatureDialog: BaseDialog<DialogSignatureBinding>({ DialogSignatureBinding.inflate(it) }) {

    private var listener: OnSignatureCompleted? = null

    override fun buildToolbar(): ToolbarBuilder {
        return ToolbarBuilder.Builder().build()
    }

    override fun DialogSignatureBinding.onViewCreated(savedInstanceState: Bundle?) {
        binding.clear.setOnClickListener {
            binding.signatureView.clear()
        }
        binding.cancel.setOnClickListener {
            listener?.onSignature(null)
            dismiss()
        }
        binding.finish.setOnClickListener {
            val bmp = binding.signatureView.signatureBitmap
            listener?.onSignature(bmp)
            dismiss()
        }
    }

    interface OnSignatureCompleted {
        fun onSignature(bmp: Bitmap?)
    }

    companion object {
        fun newInstance(onSignatureCompleted: OnSignatureCompleted) = SignatureDialog().apply {
            this.listener = onSignatureCompleted
        }
    }
}