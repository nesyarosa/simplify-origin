package noid.simplify.utils.extensions

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import noid.simplify.App
import noid.simplify.R
import noid.simplify.data.UserPreferences
import noid.simplify.data.error.ErrorResponse
import noid.simplify.interfaces.OnConfirmationListener
import noid.simplify.interfaces.OnLostConnection
import noid.simplify.ui.components.intro.IntroActivity
import noid.simplify.ui.components.main.MainActivity
import noid.simplify.utils.ImageRotator
import noid.simplify.utils.SingleEvent
import java.io.File


fun <T> LifecycleOwner.observeData(liveData: LiveData<T>?, action: (t: T) -> Unit) {
    liveData?.observe(this,
            { it?.let { t -> action(t) } }
    )
}

fun LifecycleOwner.observeError(
        event: LiveData<SingleEvent<Any>>?,
        fragmentManager: FragmentManager,
        onLostConnection: OnLostConnection? = null
) {
    fragmentManager.showError(this, event, onLostConnection)
}

fun FragmentManager.showError(
        lifecycleOwner: LifecycleOwner,
        singleEvent: LiveData<SingleEvent<Any>>?,
        onLostConnection: OnLostConnection?
) {
    singleEvent?.observe(lifecycleOwner, { event ->
        event.getContentIfNotHandled()?.let {
            if (it is ErrorResponse) {
                it.show(this, onLostConnection)
            } else {
                val context = App.context.get()
                context.showToast(context?.getString(R.string.err_network))
            }
        }
    })
}

fun FragmentManager.openFragment(container: Int, fragment: Fragment) {
    this.beginTransaction().replace(container, fragment).commit()
}

fun BottomSheetDialogFragment.show(fm: FragmentManager,
                                   isCancelable: Boolean = true) {
    this.setStyle(BottomSheetDialogFragment.STYLE_NO_TITLE, R.style.Theme_Simplify_SheetTheme)
    this.isCancelable = isCancelable
    this.showNow(fm, this.javaClass.simpleName)
}

fun DialogFragment.showModal(fm: FragmentManager) {
    this.setStyle(BottomSheetDialogFragment.STYLE_NO_TITLE, R.style.Theme_Simplify_Modal)
    this.showNow(fm, this.javaClass.simpleName)
}

fun <A : Activity> Activity.startNewActivity(activity: Class<A>, requestCode: Int = -1) {
    Intent(this, activity).also {
        if (requestCode == -1) {
            this.startActivity(it)
        } else {
            this.startActivityForResult(it, requestCode)
        }
    }
}

fun FragmentActivity.showDialog(dialog: DialogFragment) {
    try {
        val tag = dialog.javaClass.simpleName
        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag(tag)
        if (prev != null) ft.remove(prev)
        ft.addToBackStack(null)
        dialog.show(ft, tag)
    } catch (e: Exception) {
        e.printStackTrace()
    }}

fun <A : Activity> Context?.startNewActivity(activity: Class<A>, requestCode: Int = -1) {
    Intent(this, activity).also {
        if (requestCode == -1) {
            this?.startActivity(it)
            return
        }

        if (this is Activity && requestCode != -1) this.startActivityForResult(it, requestCode)
    }
}

 fun Context.downloadFile(url : String) {
    try {
        val fileName = "${System.currentTimeMillis()}"
        val downloadManager = this.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        val uri = Uri.parse(url)
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        val request = DownloadManager.Request(uri)
        request.setMimeType(mimeType)
        request.setTitle(fileName)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            fileName
        )
        downloadManager?.enqueue(request)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.showInformationDialog(
    @StringRes title: Int,
    message: String?
) {
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(this.resources.getString(R.string.close)) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}

fun Context?.logout() {
    if (this == null) return
    val up = UserPreferences(this)
    up.clearValues()
    Intent(this, IntroActivity::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
        if (this is Activity) finishAffinity()
    }
}

fun Context?.showToast(msg: String?, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, msg, duration).show()
}

fun Context.hasPermissions(vararg permissions: String): Boolean = permissions.all {
    ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
}


fun String?.toBitmap(): Bitmap? {
    if (this == null) return null
    val imageFile = File(this)
    return ImageRotator.handleBitmapDownSamplingAndRotation(imageFile)
}

fun Activity.backToHome() {
    Intent(this, MainActivity::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
        finishAffinity()
    }
}

fun Context.showConfirmationDialog(
        @StringRes message: Int,
        onConfirmationListener: OnConfirmationListener
) {
    MaterialAlertDialogBuilder(this)
            .setMessage(message)
            .setNegativeButton(this.resources.getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
                onConfirmationListener.onCancel()
            }
            .setPositiveButton(this.resources.getString(R.string.yes)) { dialog, _ ->
                dialog.dismiss()
                onConfirmationListener.onConfirm()
            }
            .show()
}

