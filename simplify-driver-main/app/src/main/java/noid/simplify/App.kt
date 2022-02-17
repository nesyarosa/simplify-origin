package noid.simplify

import android.content.Context
import androidx.multidex.MultiDexApplication
import com.google.firebase.FirebaseApp
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import dagger.hilt.android.HiltAndroidApp
import noid.simplify.constants.General
import java.lang.ref.WeakReference


@HiltAndroidApp
class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        context = WeakReference<Context>(applicationContext)
        FirebaseApp.initializeApp(applicationContext)
        AppCenter.start(this, General.APP_CENTER_KEY,
            Analytics::class.java, Crashes::class.java
        )
    }

    companion object {
        lateinit var context: WeakReference<Context>
    }
}