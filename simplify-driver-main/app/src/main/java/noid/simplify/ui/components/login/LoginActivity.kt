package noid.simplify.ui.components.login

import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.R
import noid.simplify.constants.Url
import noid.simplify.data.UserPreferences
import noid.simplify.data.network.Resource
import noid.simplify.databinding.ActivityLoginBinding
import noid.simplify.interfaces.OnLostConnection
import noid.simplify.ui.base.BaseActivity
import noid.simplify.ui.base.ToolbarBuilder
import noid.simplify.ui.components.main.MainActivity
import noid.simplify.utils.Tools
import noid.simplify.utils.extensions.observeData
import noid.simplify.utils.extensions.observeError
import noid.simplify.utils.extensions.startNewActivity
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding>({ ActivityLoginBinding.inflate(it) }), OnLostConnection {

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun buildToolbar(): ToolbarBuilder {
        return ToolbarBuilder.Builder()
            .withToolbar(binding.appbar.toolbar)
            .withTitle(getString(R.string.back))
            .withActionGoBack(true)
            .build()
    }

    override fun ActivityLoginBinding.onCreate(savedInstanceState: Bundle?) {
        val viewModel: LoginViewModel by viewModels()
        binding.viewModel = viewModel
        observeViewModel()
    }

    override fun onRetry(url: String?) {
        if (url == Url.LOGIN) binding.viewModel?.doLogin()
        if (url == Url.UPDATE_TOKEN) binding.viewModel?.doUpdateToken()
    }

    private fun observeViewModel() {
        observeData(binding.viewModel?.loginMediator) { binding.login.isEnabled = it }
        observeData(binding.viewModel?.loginLiveData, ::handleResponseLogin)
        observeData(binding.viewModel?.updateTokenLiveData, ::handleResponseUpdateToken)
        observeError(binding.viewModel?.errorLiveData, supportFragmentManager, this)
    }

    private fun handleResponseLogin(response: Resource<String>) {
        when (response) {
            is Resource.Loading -> {
                this.showLoading()
            }
            is Resource.Success -> {
                response.data?.let {
                    val token = Tools.getResponseStringByKey(data = it, key = "token")
                    val currentUser = Tools.getResponseStringByKey(data = it, key = "currentUser")
                    userPreferences.authToken = token
                    userPreferences.currentUser = currentUser
                    binding.viewModel?.doUpdateToken()
                }
            }
            is Resource.DataError -> {
                this.hideLoading()
                binding.viewModel?.setErrorResponse(response.error)
            }
        }
    }

    private fun handleResponseUpdateToken(response: Resource<String>) {
        when (response) {
            is Resource.Loading -> { }
            is Resource.Success -> {
                this.hideLoading()
                this.startNewActivity(MainActivity::class.java)
                this.finishAffinity()
            }
            is Resource.DataError -> {
                this.hideLoading()
                binding.viewModel?.setErrorResponse(response.error)
            }
        }
    }

}
