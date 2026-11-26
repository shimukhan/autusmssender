package com.elmabd.smsbridge

import android.content.Context
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Prefs {
    private const val PREF_NAME = "bd_autopay_sms_prefs"
    private const val KEY_URL = "webhook_url"
    private const val KEY_SENDERS = "sender_filters"
    private const val KEY_ENABLED = "forwarding_enabled"
    private const val KEY_LOGS = "delivery_logs"

    const val DEFAULT_SENDERS = "bKash,NAGAD,16216,upay"

    private fun sp(context: Context) =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getWebhookUrl(context: Context): String =
        sp(context).getString(KEY_URL, "")?.trim() ?: ""

    fun setWebhookUrl(context: Context, url: String) {
        sp(context).edit().putString(KEY_URL, url.trim()).apply()
    }

    fun getSenderFilters(context: Context): String =
        sp(context).getString(KEY_SENDERS, DEFAULT_SENDERS)?.trim().takeUnless { it.isNullOrEmpty() }
            ?: DEFAULT_SENDERS

    fun setSenderFilters(context: Context, senders: String) {
        sp(context).edit().putString(KEY_SENDERS, senders.trim()).apply()
    }

    fun isEnabled(context: Context): Boolean =
        sp(context).getBoolean(KEY_ENABLED, true)

    fun setEnabled(context: Context, enabled: Boolean) {
        sp(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun matchesSender(context: Context, incomingSender: String): Boolean {
        val raw = getSenderFilters(context)
        if (raw == "*" || raw.isBlank()) return true
        val senderLower = incomingSender.lowercase(Locale.US)
        val tokens = raw.split(",").map { it.trim().lowercase(Locale.US) }.filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return true
        return tokens.any { token -> senderLower.contains(token) }
    }

    @Synchronized
    fun addLog(context: Context, entry: String) {
        val time = SimpleDateFormat("dd/MM HH:mm:ss", Locale.US).format(Date())
        val line = "[$time] $entry"
        val arr = try {
            JSONArray(sp(context).getString(KEY_LOGS, "[]"))
        } catch (_: Exception) {
            JSONArray()
        }
        val updated = JSONArray()
        updated.put(line)
        val maxKeep = 25
        for (i in 0 until minOf(arr.length(), maxKeep - 1)) {
            updated.put(arr.getString(i))
        }
        sp(context).edit().putString(KEY_LOGS, updated.toString()).apply()
    }

    fun getLogsText(context: Context): String {
        val arr = try {
            JSONArray(sp(context).getString(KEY_LOGS, "[]"))
        } catch (_: Exception) {
            JSONArray()
        }
        if (arr.length() == 0) return "এখনো কোনো লগ নেই।"
        val sb = StringBuilder()
        for (i in 0 until arr.length()) {
            if (i > 0) sb.append("\n\n")
            sb.append(arr.getString(i))
        }
        return sb.toString()
    }

    fun clearLogs(context: Context) {
        sp(context).edit().putString(KEY_LOGS, "[]").apply()
    }
}
