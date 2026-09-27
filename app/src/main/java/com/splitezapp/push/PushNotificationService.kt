package com.splitezapp.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

// Firebase disabled — stub so the app builds without google-services.json
object PushNotificationService {

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
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun registerToken(context: Context) {
        // No-op until Firebase is configured
    }
}
