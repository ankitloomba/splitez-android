package com.splitezapp.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.splitezapp.MainActivity
import com.splitezapp.R
import com.splitezapp.data.api.ApiClient
import com.splitezapp.data.models.RegisterDeviceRequest
import kotlinx.coroutines.*

/**
 * Handles incoming FCM push notifications and token refresh.
 *
 * Setup steps:
 * 1. Add google-services.json to app/ directory
 * 2. Add firebase-messaging dependency to build.gradle
 * 3. Register this service in AndroidManifest.xml
 */
class PushNotificationService : FirebaseMessagingService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val CHANNEL_ID = "splitez_notifications"
        const val CHANNEL_NAME = "SplitEZ Notifications"
        private var notificationId = 0

        /** Create the notification channel (call from Application.onCreate or MainActivity). */
        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = "Expense splits, settlements, and group updates"
                    enableVibration(true)
                }
                val manager = context.getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(channel)
            }
        }

        /** Register the current FCM token with the backend. */
        fun registerToken(context: Context) {
            try {
                com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { token ->
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                ApiClient.api.registerDevice(
                                    RegisterDeviceRequest(token = token, platform = "android")
                                )
                            } catch (_: Exception) { /* will retry next launch */ }
                        }
                    }
            } catch (_: Exception) {
                // Firebase not configured yet — skip
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send updated token to backend
        scope.launch {
            try {
                ApiClient.api.registerDevice(
                    RegisterDeviceRequest(token = token, platform = "android")
                )
            } catch (_: Exception) { /* will retry */ }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val notification = message.notification
        val data = message.data

        val title = notification?.title ?: data["title"] ?: "SplitEZ"
        val body = notification?.body ?: data["body"] ?: ""

        showNotification(title, body, data)
    }

    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String>,
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            // Pass notification data for deep linking
            data.forEach { (k, v) -> putExtra(k, v) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(notificationId++, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
