package com.splitezapp.data.analytics

import android.content.Context
import android.os.Build
import com.splitezapp.data.api.ApiClient
import kotlinx.coroutines.*
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Lightweight analytics tracker. Batches events and sends them to the backend.
 */
object AnalyticsTracker {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val queue = CopyOnWriteArrayList<TrackEvent>()
    private var sessionId: String = UUID.randomUUID().toString()
    private const val PLATFORM = "android"

    /** Persistent install ID stored in SharedPreferences. */
    private fun getInstallId(context: Context): String {
        val prefs = context.getSharedPreferences("splitez_prefs", Context.MODE_PRIVATE)
        val key = "install_id"
        return prefs.getString(key, null) ?: run {
            val newId = UUID.randomUUID().toString()
            prefs.edit().putString(key, newId).apply()
            newId
        }
    }

    /** Register this device install with the backend. */
    fun registerInstall(context: Context) {
        scope.launch {
            try {
                ApiClient.api.registerInstall(
                    InstallPayload(
                        installId = getInstallId(context),
                        platform = PLATFORM,
                        appVersion = "1.0.0",
                        osVersion = Build.VERSION.RELEASE,
                        deviceModel = Build.MODEL,
                    )
                )
            } catch (_: Exception) { /* non-critical */ }
        }
    }

    fun startSession() {
        sessionId = UUID.randomUUID().toString()
        track("app_open")
    }

    fun trackScreen(screen: String) {
        track("screen_view", screen = screen)
    }

    fun trackAction(action: String, screen: String? = null) {
        track("action", screen = screen, action = action)
    }

    fun track(event: String, screen: String? = null, action: String? = null) {
        queue.add(TrackEvent(
            event = event,
            sessionId = sessionId,
            screen = screen,
            action = action,
            platform = PLATFORM,
            appVersion = "1.0.0"
        ))
        if (queue.size >= 10) flush()
    }

    fun flush() {
        if (queue.isEmpty()) return
        val batch = queue.toList()
        queue.clear()

        scope.launch {
            try {
                ApiClient.api.trackEventsBatch(BatchPayload(batch))
            } catch (_: Exception) {
                // Re-queue on failure
                if (queue.size < 100) queue.addAll(0, batch)
            }
        }
    }
}

data class TrackEvent(
    val event: String,
    val sessionId: String? = null,
    val screen: String? = null,
    val action: String? = null,
    val platform: String? = null,
    val appVersion: String? = null,
)

data class BatchPayload(val events: List<TrackEvent>)

data class InstallPayload(
    val installId: String,
    val platform: String,
    val appVersion: String? = null,
    val osVersion: String? = null,
    val deviceModel: String? = null,
)
