package noid.simplify.utils

import android.app.Activity
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.IBinder
import androidx.annotation.StringRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import noid.simplify.R
import noid.simplify.constants.DateFormat
import noid.simplify.data.UserPreferences
import noid.simplify.data.dto.JobDetail
import noid.simplify.data.dto.User
import noid.simplify.data.network.DataUtil
import noid.simplify.utils.extensions.showToast
import noid.simplify.utils.extensions.toFormattedString
import noid.simplify.utils.extensions.toPriceAmount
import recieptservice.com.recieptservice.PrinterInterface
import java.util.*

object Receipt {

    private const val CENTER = 1

    fun doPrint(activity: Activity, job: JobDetail) {
        var technician = getUsernameActive(activity)
        job.employee?.let { list ->
            technician = list.joinToString { "${it.id}" }
        }
        val current = Calendar.getInstance().time.toFormattedString(DateFormat.FORMAT_DATE_RECEIPT)

        val intent = Intent()
        intent.setClassName(
                "recieptservice.com.recieptservice",
                "recieptservice.com.recieptservice.service.PrinterService"
        )
        activity.bindService(intent, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                val serInterface = PrinterInterface.Stub.asInterface(service)
                try {
                    serInterface.printText(
                            "${job.entityName}\n" +
                                    "${job.entityAddress}\n" +
                                    "${job.entityContactNumber}\n" +
                                    "${job.entityEmail}\n" +
                                    "================================"
                    )
                    serInterface.setAlignment(CENTER)
                    serInterface.nextLine(1)
                    serInterface.printText(getValueToPrint(
                            ctx = activity,
                            resId = R.string.contract,
                            value = "#${job.serviceId}")
                    )
                    serInterface.nextLine(1)
                    serInterface.printText(getValueToPrint(
                            ctx = activity,
                            resId = R.string.receipt,
                            value = "#${job.jobId}")
                    )
                    serInterface.nextLine(1)
                    serInterface.printText(getValueToPrint(
                            ctx = activity,
                            resId = R.string.client,
                            value = job.clientName)
                    )
                    serInterface.nextLine(1)
                    serInterface.printText(getValueToPrint(
                            ctx = activity,
                            resId = R.string.served_by,
                            value = technician)
                    )
                    serInterface.nextLine(1)
                    serInterface.printText(getValueToPrint(
                            ctx = activity,
                            resId = R.string.time,
                            value = current)
                    )
                    serInterface.printText(getValueToPrint(
                            ctx = activity,
                            resId = R.string.balance,
                            value = (job.contractBalance ?: 0.0).toPriceAmount())
                    )
                    serInterface.nextLine(1)
                    serInterface.printText("--------------------------------")
                    serInterface.nextLine(1)
                    job.serviceItem?.forEach {
                        serInterface.printText("${it.getItemWithServiceTypeChunked()}\n")
                        serInterface.printText(getTextWithPrice(key = it.getQty(), isShowPrice = false))
                    }
                    job.additionalServiceItem?.forEach {
                        serInterface.printText("${it.getItemWithServiceTypeChunked()}\n")
                        serInterface.printText(getTextWithPrice(key = it.getQty(), price = it.totalPrice))
                    }
                    serInterface.printText("================================\n")
                    serInterface.printText(getTextWithPrice(activity.getString(R.string.sub_total), job.getSubTotalAdditionalItems()))
                    serInterface.printText(getTextWithPrice(activity.getString(R.string.gst_7), job.getGstAmountForAdditional()))
                    serInterface.printText(getTextWithPrice(activity.getString(R.string.amount_due), job.getAmountDue()))
                    serInterface.printText(getTextWithPrice(activity.getString(R.string.total_collected), job.collectedAmount ?: 0.0))
                    if (job.signatureUrl != null) {
                        Glide.with(activity)
                                .asBitmap()
                                .override(150)
                                .load(job.signatureUrl)
                                .listener(object : RequestListener<Bitmap> {
                                    override fun onLoadFailed(
                                            e: GlideException?,
                                            model: Any?,
                                            target: Target<Bitmap>?,
                                            isFirstResource: Boolean
                                    ): Boolean {
                                        serInterface.printText("\n${activity.getString(R.string.footer_thanks)}\n\n\n")
                                        serInterface.setAlignment(CENTER)
                                        return false
                                    }

                                    override fun onResourceReady(
                                            resource: Bitmap?,
                                            model: Any?,
                                            target: Target<Bitmap>?,
                                            dataSource: DataSource?,
                                            isFirstResource: Boolean
                                    ): Boolean {
                                        resource?.let {
                                            serInterface.printBitmap(it)
                                            serInterface.setAlignment(CENTER)
                                            serInterface.nextLine(1)
                                            serInterface.printText("\n\n____________________\n")
                                            serInterface.printText(job.clientName)
                                            serInterface.setAlignment(CENTER)
                                            serInterface.nextLine(2)
                                            serInterface.printText("\n${activity.getString(R.string.footer_thanks)}\n\n\n")
                                            serInterface.setAlignment(CENTER)
                                        }

                                        return false
                                    }
                                }).submit()
                    } else {
                        serInterface.printText("\n${activity.getString(R.string.footer_thanks)}\n\n\n")
                        serInterface.setAlignment(CENTER)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    activity.showToast(e.message)
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                activity.showToast(activity.getString(R.string.err_printer))
            }
        }, Service.BIND_AUTO_CREATE)
    }

    private fun getValueToPrint(ctx: Context, @StringRes resId: Int, value: String?): String {
        return String.format(
                Locale.getDefault(),
                "%-10.10s%1.1s %-20.20s",
                ctx.getString(resId),
                ":",
                value ?: "NA"
        )
    }

    private fun getTextWithPrice(key: String, price: Double? = null, isShowPrice: Boolean = true): String {
        val value = if (isShowPrice) price?.toPriceAmount() else ""
        return "${String.format(
                Locale.getDefault(),
                "%-23.23s %8.8s",
                key,
                value
        )}\n"
    }

    private fun getUsernameActive(ctx: Context): String {
        val user = DataUtil.toModel<User>(UserPreferences(ctx).currentUser)
        return user?.displayName ?: "NA"
    }

}