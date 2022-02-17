package noid.simplify.ui.components.forgot

import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.R
import noid.simplify.constants.Url
import noid.simplify.data.network.Resource
import noid.simplify.databinding.ActivityForgotBinding
import noid.simplify.interfaces.OnDismissListener
import noid.simplify.interfaces.OnLostConnection
import noid.simplify.ui.base.BaseActivity
import noid.simplify.ui.base.ToolbarBuilder
import noid.simplify.utils.extensions.*

@AndroidEntryPoint
class ForgotActivity : BaseActivity<ActivityForgotBinding>({ ActivityForgotBinding.inflate(it) }),
    OnLostConnection {
    override fun buildToolbar(): ToolbarBuilder {
        return ToolbarBuilder.Builder()
            .withToolbar(binding.appbar.toolbar)
            .withTitle(getString(R.string.back))
            .withActionGoBack(true)
            .build()
    }

    override fun ActivityForgotBinding.onCreate(savedInstanceState: Bundle?) {
        val viewModel: ForgotViewModel by viewModels()
        binding.viewModel = viewModel
        observeViewModel()
    }

    override fun onRetry(url: String?) {
        if (url == Url.FORGOT_PASSWORD) binding.viewModel?.doSendEmail()
    }

    private fun observeViewModel() {
        observeData(binding.viewModel?.emailMediator) { binding.reset.isEnabled = it }
        observeData(binding.viewModel?.doEmailLiveData, ::handleResponseSendEmail)
        observeError(binding.viewModel?.errorLiveData, supportFragmentManager, this)
    }

    private fun dialogChangePasswordState(registerSucceed: Boolean) = ChangePasswordStateDialog.newInstance(
        registerSucceed,
        object : OnDismissListener {
            override fun onDismiss() {
                if(registerSucceed) finish()
            }
        })
    private fun handleResponseSendEmail(response: Resource<String>) {
        when (response) {
            is Resource.Loading -> {
                this.showLoading()
            }
            is Resource.Success -> {
                this.hideLoading()
                showDialog(dialogChangePasswordState(true))
            }
            is Resource.DataError -> {
                this.hideLoading()
                showDialog(dialogChangePasswordState(false))
                binding.viewModel?.setErrorResponse(response.error)
            }
        }
    }
}
