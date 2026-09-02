package com.splitezapp.data.analytics

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
