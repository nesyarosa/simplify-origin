package noid.simplify.ui.components.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.data.UserPreferences
import noid.simplify.databinding.ActivitySplashBinding
import noid.simplify.services.MyFirebaseMessagingService
import noid.simplify.ui.base.BaseActivity
import noid.simplify.ui.base.ToolbarBuilder
import noid.simplify.ui.components.intro.IntroActivity
import noid.simplify.ui.components.main.MainActivity
import noid.simplify.utils.extensions.isNull
import noid.simplify.utils.extensions.startNewActivity
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>({ ActivitySplashBinding.inflate(it) }) {

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun buildToolbar(): ToolbarBuilder {
        return ToolbarBuilder.Builder().build()
    }

    override fun ActivitySplashBinding.onCreate(savedInstanceState: Bundle?) {
        if (intent.isNull()) openActivity() else onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val extras = intent?.extras
        if (extras != null && extras.containsKey(MyFirebaseMessagingService.JOB_ID)) {
            val jobId = extras.getString(MyFirebaseMessagingService.JOB_ID)
            Intent(this, MainActivity::class.java).also {
                it.putExtra(MyFirebaseMessagingService.JOB_ID, jobId)
                startActivity(it)
                finish()
            }
        } else {
            openActivity()
        }
    }

    private fun openActivity() {
        val isTokenNull = userPreferences.authToken.isNull()
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            this@SplashActivity.startNewActivity(
                if (isTokenNull) IntroActivity::class.java else MainActivity::class.java
            )
            finish()
        }, SPLASH_TIME)
    }

    companion object {
        private const val SPLASH_TIME = 3000L
    }

}