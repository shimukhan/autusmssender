package com.elmabd.smsbridge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import java.util.concurrent.Executors

class SmsReceiver : BroadcastReceiver() {

    companion object {
        private val executor = Executors.newSingleThreadExecutor()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        if (!Prefs.isEnabled(context)) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        // Group multi-part SMS by sender address
        val grouped = LinkedHashMap<String, StringBuilder>()
        for (sms in messages) {
            val sender = sms.originatingAddress ?: "Unknown"
            val body = sms.messageBody ?: ""
            grouped.getOrPut(sender) { StringBuilder() }.append(body)
        }

        val pendingResult = goAsync()
        val appContext = context.applicationContext

        executor.execute {
            try {
                for ((sender, bodyBuilder) in grouped) {
                    val fullMessage = bodyBuilder.toString().trim()
                    if (fullMessage.isEmpty()) continue
                    if (!Prefs.matchesSender(appContext, sender)) continue
                    WebhookSender.sendWithRetry(appContext, sender, fullMessage, maxAttempts = 3)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
