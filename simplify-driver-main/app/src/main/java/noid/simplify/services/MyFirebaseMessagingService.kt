package noid.simplify.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import noid.simplify.R
import noid.simplify.constants.Url
import noid.simplify.data.MainRepository
import noid.simplify.data.dto.Notification
import noid.simplify.ui.components.main.MainActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface MainRepositoryInterface {
        val mainRepository: MainRepository
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification?.let {
            if (remoteMessage.data.isNotEmpty()){
                val data = remoteMessage.data
                sendNotification(it, data)
            }
        }
    }

    override fun onNewToken(token: String) {
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
        runBlocking {
            val notification = Notification(token = token)
            val repositoryInterface = EntryPoints.get(
                applicationContext,
                MainRepositoryInterface::class.java
            )
            repositoryInterface.mainRepository.put(Url.UPDATE_TOKEN, notification)
        }
    }

    private fun sendNotification(
        notification: RemoteMessage.Notification,
        data: Map<String, String>
    ) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(JOB_ID, data[JOB_ID])
        intent.action = Intent.ACTION_MAIN
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        showNotification(notification, pendingIntent, data[JOB_STATUS])
    }

    private fun showNotification(
        notification: RemoteMessage.Notification,
        pendingIntent: PendingIntent,
        tag: String?
    ) {
        val channelId = getString(R.string.app_name)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                tag,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val uniqueId = System.currentTimeMillis().toInt()
        notificationManager.notify(uniqueId, notificationBuilder)
    }

    companion object {
        const val JOB_ID = "jobId"
        const val JOB_STATUS = "jobStatus"
    }

}