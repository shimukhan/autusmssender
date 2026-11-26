package com.elmabd.smsbridge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (Prefs.isEnabled(context) && Prefs.getWebhookUrl(context).isNotBlank()) {
                KeepAliveService.start(context)
            }
        }
    }
}
