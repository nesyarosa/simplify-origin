package noid.simplify.ui.components.payment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.data.dto.JobDetail
import noid.simplify.databinding.ActivityPaymentSuccessBinding
import noid.simplify.ui.base.BaseActivity
import noid.simplify.ui.base.ToolbarBuilder
import noid.simplify.utils.Receipt
import noid.simplify.utils.extensions.backToHome

@AndroidEntryPoint
class PaymentSuccessActivity : BaseActivity<ActivityPaymentSuccessBinding>({
    ActivityPaymentSuccessBinding.inflate(
        it
    )
}) {

    private var job: JobDetail? = null

    override fun buildToolbar(): ToolbarBuilder {
        return ToolbarBuilder.Builder().build()
    }

    override fun ActivityPaymentSuccessBinding.onCreate(savedInstanceState: Bundle?) {
        this@PaymentSuccessActivity.job = intent.getParcelableExtra(EXTRA_JOB)
        this@PaymentSuccessActivity.apply {
            binding.print.setOnClickListener { job?.let { Receipt.doPrint(this, it) } }
            binding.backToHome.setOnClickListener { this.backToHome() }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.backToHome()
    }

    companion object {
        const val EXTRA_JOB = "key.EXTRA_JOB"

        fun open(context: Context, job: JobDetail?) {
            Intent(context, PaymentSuccessActivity::class.java).also {
                it.putExtra(EXTRA_JOB, job)
                context.startActivity(it)
            }
        }
    }

}