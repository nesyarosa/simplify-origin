package noid.simplify.ui.components.forgot

import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.R
import noid.simplify.data.UserPreferences
import noid.simplify.data.network.Resource
import noid.simplify.databinding.ActivityChangePasswordBinding
import noid.simplify.interfaces.OnLostConnection
import noid.simplify.ui.base.BaseActivity
import noid.simplify.ui.base.ToolbarBuilder
import noid.simplify.ui.components.login.LoginActivity
import noid.simplify.utils.extensions.*
import javax.inject.Inject

@AndroidEntryPoint
class ChangePasswordActivity : BaseActivity<ActivityChangePasswordBinding>({ ActivityChangePasswordBinding.inflate(it) }),
    OnLostConnection {

    @Inject
    lateinit var userPreferences : UserPreferences

    override fun buildToolbar(): ToolbarBuilder {
        return ToolbarBuilder.Builder()
            .withToolbar(binding.appbar.toolbar)
            .withTitle(getString(R.string.new_password))
            .withTitle(getString(R.string.back))
            .withActionGoBack(true)
            .build()
    }

    override fun ActivityChangePasswordBinding.onCreate(savedInstanceState: Bundle?) {
        val viewModel: ChangePasswordViewModel by viewModels()
        binding.viewModel = viewModel
        binding.save.setOnClickListener {
            binding.viewModel?.doChangePassword()
        }
        observeViewModel()
    }

    override fun onRetry(url: String?) {
        binding.viewModel?.doChangePassword()
    }

    fun observeViewModel() {
        observeData(binding.viewModel?.isPasswordMatching, ::handlePasswordTyping)
        observeData(binding.viewModel?.doChangeLiveData, ::handleChangePassword)
        observeError(binding.viewModel?.errorLiveData, supportFragmentManager, this)
    }

    private fun handlePasswordTyping(isMatch: Boolean) {
        binding.save.isEnabled = isMatch
        binding.retypePassword.error = if (isMatch) null else getString(R.string.password_not_equal)
    }

    private fun handleChangePassword(res: Resource<String>) {
        when(res) {
            is Resource.Loading -> this.hideLoading()
            is Resource.Success -> {
                this.hideLoading()
                showToast(getString(R.string.change_pwd_successful))
                if (userPreferences.currentUser != null) {
                    finish()
                } else {
                    this@ChangePasswordActivity.startNewActivity(LoginActivity::class.java)
                }
            }
            is Resource.DataError -> {
                this.hideLoading()
                showToast(getString(R.string.change_pwd_failed))
                binding.viewModel?.setErrorResponse(res.error)
            }
        }
    }
}
