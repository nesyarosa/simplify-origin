package noid.simplify.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import android.util.Log
import noid.simplify.App
import noid.simplify.BuildConfig
import noid.simplify.constants.DateFormat
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


object Tools {

    fun log(clazzName: String, tag: String, msg: String?) {
        if (BuildConfig.DEBUG) Log.e(clazzName, "$tag => $msg")
    }

    fun getResponseStringByKey(data: String?, key: String): String {
        if (data.isNullOrBlank()) return ""
        return JSONObject(data).optString(key, "")
    }

    fun openSettings(context: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts(
            "package",
            BuildConfig.APPLICATION_ID, null
        )
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun bitmapToFile(
        bitmap: Bitmap?,
        fileName: String
    ): File? {
        val context = App.context.get() ?: return null
        val fileNameToSave = "SIGNATURE_$fileName"
        var file: File? = null
        return try {
            file = File(context.externalCacheDir?.absolutePath + File.separator.toString() + fileNameToSave)
            file.createNewFile()

            val bos = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 0, bos)
            val byteArray = bos.toByteArray()
            val fos = FileOutputStream(file)
            fos.write(byteArray)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file
        }
    }

    fun getImageName(): String {
        val timeStamp = SimpleDateFormat(DateFormat.FORMAT_DATE_FOR_IMAGE, Locale.getDefault()).format(Date())
        return "$timeStamp.jpg"
    }

    fun getString(id : Int): String {
        return App.context.get()?.getString(id) ?: ""
    }



}