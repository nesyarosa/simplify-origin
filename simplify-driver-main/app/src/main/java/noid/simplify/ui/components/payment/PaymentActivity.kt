package noid.simplify.ui.components.payment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.R
import noid.simplify.constants.General
import noid.simplify.constants.Url
import noid.simplify.data.UserPreferences
import noid.simplify.data.dto.AdditionalService
import noid.simplify.data.dto.JobDetail
import noid.simplify.data.dto.JobToComplete
import noid.simplify.data.network.Resource
import noid.simplify.databinding.ActivityPaymentBinding
import noid.simplify.interfaces.OnLostConnection
import noid.simplify.ui.base.BaseActivity
import noid.simplify.ui.base.ToolbarBuilder
import noid.simplify.ui.components.others.SignatureDialog
import noid.simplify.utils.Tools
import noid.simplify.utils.extensions.*
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class PaymentActivity : BaseActivity<ActivityPaymentBinding>({ ActivityPaymentBinding.inflate(it) }), OnLostConnection {

    private var jobId = 0
    private var jobToComplete: JobToComplete? = null
    private var jobDetail: JobDetail? = null
    private var service: AdditionalService? = null
    private var totalPayment = 0.0
    private val viewModel: PaymentViewModel by viewModels()
    private var signature: Bitmap? = null
    private var signatureFilePath: String? = null
    private var signatureFileName = Tools.getImageName()
    private var signatureUploadUrl: String? = null

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun buildToolbar(): ToolbarBuilder {
        return ToolbarBuilder.Builder()
            .withToolbar(binding.appbar.toolbar)
            .withTitle(getString(R.string.payment_detail))
            .withActionGoBack(true)
            .build()
    }

    override fun ActivityPaymentBinding.onCreate(savedInstanceState: Bundle?) {
        this@PaymentActivity.apply {
            this.jobDetail = intent.getParcelableExtra(PaymentSuccessActivity.EXTRA_JOB)
            this.service = intent.getParcelableExtra(EXTRA_ADDITIONAL_SERVICE)
            this.jobToComplete = intent.getParcelableExtra(EXTRA_JOB_OBJECT)
            this.jobId = jobDetail?.jobId ?: 0
            val contractBalance = jobDetail?.contractBalance ?: 0.0
            val balance = if (contractBalance < 0) 0.0 else contractBalance
            this.totalPayment = balance + (jobDetail?.totalAmount() ?: 0.0)
            jobToComplete?.CheckListJob = jobDetail?.ChecklistJob
            jobToComplete?.employee = jobDetail?.employee
            jobToComplete?.location = "${userPreferences.latitude},${userPreferences.longitude}"
        }

        binding.paymentMethod.setOnCheckedChangeListener { _, checkedId ->
            jobToComplete?.paymentMethod = when (checkedId) {
                R.id.rb_cash -> General.METHOD__CASH
                R.id.rb_paynow -> General.METHOD__PAYNOW
                else -> General.METHOD__CHEQUE
            }
            if (jobToComplete?.paymentMethod == General.METHOD__PAYNOW) {
                QRCodeDialog.newInstance(jobDetail?.paynowQrcode).showModal(supportFragmentManager)
            }
        }
        binding.totalAmount.text = totalPayment.toPriceAmount()
        binding.collectedAmount.apply {
            this.editText?.doAfterTextChanged {
                val str = it.toString()
                if (str.isNotBlank()) {
                    val str2 = str.toDecimalFormatted()
                    val collectedAmount = try { str2.toDouble() } catch (e: Exception) { 0.0 }
                    val change = collectedAmount - totalPayment
                    binding.changeAmount.editText?.setText(change.toPriceWithoutCurrency())
                    jobToComplete?.collectedAmount = collectedAmount
                    if (str2 != str) {
                        this.editText?.setText(str2)
                        this.editText?.setSelection(str2.length)
                    }
                }
            }
        }
        binding.completePayment.setOnClickListener { openSignatureDialog() }
        observeViewModel()
    }

    override fun onRetry(url: String?) {
        val getSignatureUrl = "${Url.GET_URL_UPLOAD_SIGNATURE}/$signatureFileName"
        when (url) {
            getSignatureUrl -> {
                viewModel.getUrlUploadSignature(jobId, signatureFileName)
            }
            signatureUploadUrl -> {
                uploadSignature()
            }
            else -> {
                viewModel.doUpdateJob(jobId, jobToComplete)
            }
        }
    }

    private fun observeViewModel() {
        observeData(viewModel.getSignatureUrlLiveData, ::responseGetUrlSignature)
        observeData(viewModel.uploadSignatureLiveData, ::responseUploadSignature)
        observeData(viewModel.jobUpdateLiveData, ::responseUpdateJobStatus)
        observeError(viewModel.errorLiveData, supportFragmentManager, this)
    }

    private fun openSignatureDialog() {
        val signatureDialog = SignatureDialog.newInstance(object : SignatureDialog.OnSignatureCompleted {
            override fun onSignature(bmp: Bitmap?) {
                if (bmp == null) return
                signature = bmp
                jobToComplete?.signature = signatureFileName
                viewModel.getUrlUploadSignature(jobId, signatureFileName)
            }
        })
        signatureDialog.showModal(supportFragmentManager)
    }

    private fun uploadSignature() {
        val imgFile = Tools.bitmapToFile(signature, signatureFileName)
        signatureFilePath = imgFile?.absolutePath
        if (signatureUploadUrl != null && imgFile != null) {
            viewModel.uploadSignature(signatureUploadUrl!!, imgFile)
        } else {
            this.showToast(getString(R.string.err_signature_image))
        }
    }

    /**
     * Handle response from server
     */
    private fun responseGetUrlSignature(response: Resource<String>) {
        when (response) {
            is Resource.Loading -> this.showLoading()
            is Resource.Success -> {
                this.hideLoading()
                response.data?.let {
                    signatureUploadUrl = Tools.getResponseStringByKey(data = it, key = "signatureUrl")
                    uploadSignature()
                }
            }
            is Resource.DataError -> {
                this.hideLoading()
                viewModel.setErrorResponse(response.error)
            }
        }
    }

    private fun responseUploadSignature(response: Resource<String>) {
        when (response) {
            is Resource.Loading -> this.showLoading()
            is Resource.Success -> {
                this.hideLoading()
                jobToComplete?.jobStatus = General.STATUS__COMPLETED
                jobToComplete?.signature = signatureFileName
                viewModel.doUpdateJob(jobId, jobToComplete)

            }
            is Resource.DataError -> {
                this.hideLoading()
                viewModel.setErrorResponse(response.error)
            }
        }
    }

    private fun responseUpdateJobStatus(response: Resource<String>) {
        when (response) {
            is Resource.Loading -> this.showLoading()
            is Resource.Success -> {
                this.hideLoading()
                jobDetail?.collectedAmount = jobToComplete?.collectedAmount
                jobDetail?.signatureUrl = signatureFilePath
                PaymentSuccessActivity.open(this, jobDetail)
                this.finish()
            }
            is Resource.DataError -> {
                this.hideLoading()
                this.viewModel.setErrorResponse(response.error)
            }
        }
    }

    companion object {
        private const val EXTRA_JOB_OBJECT = "key.EXTRA_JOB_OBJECT"
        private const val EXTRA_ADDITIONAL_SERVICE = "key.EXTRA_ADDITIONAL_SERVICE"

        fun open(context: Context?,
                 jobDetail: JobDetail?,
                 jobToComplete: JobToComplete?,
                 service: AdditionalService?
        ) {
            Intent(context, PaymentActivity::class.java).also {
                it.putExtra(PaymentSuccessActivity.EXTRA_JOB, jobDetail)
                it.putExtra(EXTRA_JOB_OBJECT, jobToComplete)
                it.putExtra(EXTRA_ADDITIONAL_SERVICE, service)
                context?.startActivity(it)
            }
        }
    }

}