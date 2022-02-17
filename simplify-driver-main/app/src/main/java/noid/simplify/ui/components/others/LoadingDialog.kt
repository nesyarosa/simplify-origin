package noid.simplify.ui.components.others

import android.app.Dialog
import android.content.Context
import android.view.Window
import noid.simplify.R

class LoadingDialog(context: Context) {

    private var dialog: Dialog = Dialog(context, R.style.Theme_Simplify_LoadingDialog)

    fun show() {
        if (!dialog.isShowing) dialog.show()
    }

    fun dismiss() {
        if (dialog.isShowing) dialog.dismiss()
    }

    init {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_loading)
        dialog.setCancelable(false)
    }
}