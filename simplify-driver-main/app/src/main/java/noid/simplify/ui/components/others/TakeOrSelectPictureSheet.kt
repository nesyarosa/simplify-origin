package noid.simplify.ui.components.others

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.constants.PermissionCode
import noid.simplify.databinding.SheetTakeOrSelectPictureBinding
import noid.simplify.ui.base.BaseSheetDialog

@AndroidEntryPoint
class TakeOrSelectPictureSheet: BaseSheetDialog<SheetTakeOrSelectPictureBinding>({ SheetTakeOrSelectPictureBinding.inflate(it) }) {

    private var listener: OnSelectListener? = null

    override fun SheetTakeOrSelectPictureBinding.onViewCreated(savedInstanceState: Bundle?) {
        binding.gallery.setOnClickListener {
            listener?.onSelect(PermissionCode.REQUEST_GALLERY)
            dismiss()
        }
        binding.camera.setOnClickListener {
            listener?.onSelect(PermissionCode.REQUEST_CAMERA)
            dismiss()
        }
    }

    interface OnSelectListener {
        fun onSelect(requestCode: Int)
    }

    companion object {
        fun newInstance(listener: OnSelectListener) = TakeOrSelectPictureSheet().apply {
            this.listener = listener
        }
    }
}