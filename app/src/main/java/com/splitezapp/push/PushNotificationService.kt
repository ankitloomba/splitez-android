package com.splitezapp.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushNotificationService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: send token to backend
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // TODO: handle incoming push notification
    }

    companion object {
        fun registerToken(context: Context) {
            // Token registration handled automatically by Firebase SDK
        }

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "splitez_notifications",
                    "SplitEZ Notifications",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = "Expense splits, settlements, and group updates"
                    enableVibration(true)
                }
                context.getSystemService(NotificationManager::class.java)
                    .createNotificationChannel(channel)
            }
        }
    }
}
