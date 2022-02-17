package noid.simplify.ui.components.profile

import android.app.Activity
import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.R
import noid.simplify.constants.Url
import noid.simplify.data.network.Resource
import noid.simplify.databinding.ActivityEditProfileBinding
import noid.simplify.interfaces.OnLostConnection
import noid.simplify.ui.base.BaseActivity
import noid.simplify.ui.base.ToolbarBuilder
import noid.simplify.utils.extensions.observeData
import noid.simplify.utils.extensions.observeError

@AndroidEntryPoint
class EditProfileActivity : BaseActivity<ActivityEditProfileBinding>({ ActivityEditProfileBinding.inflate(it) }), OnLostConnection {
    override fun buildToolbar(): ToolbarBuilder {
        return ToolbarBuilder.Builder()
            .withToolbar(binding.appbar.toolbar)
            .withTitle(getString(R.string.back))
            .withActionGoBack(true)
            .build()
    }

    override fun ActivityEditProfileBinding.onCreate(savedInstanceState: Bundle?) {
        val viewModel: EditProfileViewModel by viewModels()
        binding.viewModel = viewModel
        observeViewModel()
    }

    override fun onRetry(url: String?) {
        if (url == Url.USERS) binding.viewModel?.doUpdateProfile()
    }

    private fun observeViewModel() {
        observeData(binding.viewModel?.userMediator) { binding.save.isEnabled = it }
        observeData(binding.viewModel?.updateProfileLiveData, ::handleResponseUpdate)
        observeError(binding.viewModel?.errorLiveData, supportFragmentManager, this)
    }

    private fun handleResponseUpdate(response: Resource<String>) {
        when (response) {
            is Resource.Loading -> {
                this.showLoading()
            }
            is Resource.Success -> {
                this.hideLoading()
                this.binding.viewModel?.saveUserToLocal()
                this.setResult(Activity.RESULT_OK)
                this.finish()
            }
            is Resource.DataError -> {
                this.hideLoading()
                binding.viewModel?.setErrorResponse(response.error)
            }
        }
    }

}