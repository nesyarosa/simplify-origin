package noid.simplify.ui.components.others

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.databinding.SheetListMapAppBinding
import noid.simplify.ui.base.BaseSheetDialog
import noid.simplify.utils.extensions.visible

@AndroidEntryPoint
class ListMapAppSheet(
    private val postalCode : String
) : BaseSheetDialog<SheetListMapAppBinding>({ SheetListMapAppBinding.inflate(it) }) {
    override fun SheetListMapAppBinding.onViewCreated(savedInstanceState: Bundle?) {

        googleMaps.setOnClickListener {
            Intent(Intent.ACTION_VIEW).also {
                it.data = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$postalCode")
                context?.startActivity(it)
            }
        }
       waze.setOnClickListener {
            Intent(Intent.ACTION_VIEW).also {
                it.data = Uri.parse("https://waze.com/ul?q=$postalCode&navigate=yes")
                context?.startActivity(it)
            }
        }

        close.setOnClickListener {
            dismiss()
        }
        googleMaps.visible(isPackageInstalled("com.google.android.apps.maps", requireActivity().packageManager))
        waze.visible(isPackageInstalled("com.waze",requireActivity().packageManager))
        notInstalled.visible(isAllAppInstalled())
        close.visible(isAllAppInstalled())

    }
    private fun isAllAppInstalled() : Boolean {
        return isPackageInstalled("com.google.android.apps.maps", requireActivity().packageManager) && isPackageInstalled("com.waze",requireActivity().packageManager)
    }


    private fun isPackageInstalled(packageName : String, packageManager : PackageManager) : Boolean {
        return try {
            packageManager.getPackageInfo(packageName,  PackageManager.GET_PERMISSIONS)
            true
        } catch (e : PackageManager.NameNotFoundException) {
            false
        }
    }
    companion object {
        fun newInstance(postalCode: String) = ListMapAppSheet(postalCode).apply {

        }
    }

}