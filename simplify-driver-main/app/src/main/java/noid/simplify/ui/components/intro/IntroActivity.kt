package noid.simplify.ui.components.intro

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.databinding.ActivityIntroBinding
import noid.simplify.ui.base.BaseActivity
import noid.simplify.ui.base.ToolbarBuilder
import noid.simplify.ui.components.login.LoginActivity
import noid.simplify.utils.extensions.startNewActivity

@AndroidEntryPoint
class IntroActivity : BaseActivity<ActivityIntroBinding>({ ActivityIntroBinding.inflate(it) }) {

    override fun buildToolbar(): ToolbarBuilder {
        return ToolbarBuilder.Builder().build()
    }

    override fun ActivityIntroBinding.onCreate(savedInstanceState: Bundle?) {
        binding.login.setOnClickListener {
            this@IntroActivity.startNewActivity(LoginActivity::class.java)
        }
    }

}
