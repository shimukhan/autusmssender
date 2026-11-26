package com.elmabd.smsbridge

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class SendResult(val ok: Boolean, val code: Int, val body: String)

object WebhookSender {

    fun sendWithRetry(context: Context, sender: String, message: String, maxAttempts: Int = 3): SendResult {
        val webhookUrl = Prefs.getWebhookUrl(context)
        if (webhookUrl.isBlank()) {
            Prefs.addLog(context, "ERR ($sender): Webhook URL সেট করা নেই।")
            return SendResult(false, 0, "Webhook URL সেট করা নেই।")
        }

        var lastResult = SendResult(false, 0, "Unknown error")
        for (attempt in 1..maxAttempts) {
            lastResult = sendOnce(webhookUrl, sender, message)
            if (lastResult.ok) {
                Prefs.addLog(context, "OK ($sender) [HTTP ${lastResult.code}]: ${lastResult.body.take(120)}")
                return lastResult
            }
            if (attempt < maxAttempts) {
                try {
                    Thread.sleep(attempt * 3000L)
                } catch (_: InterruptedException) {
                    break
                }
            }
        }
        Prefs.addLog(context, "FAIL ($sender) [HTTP ${lastResult.code}]: ${lastResult.body.take(120)}")
        return lastResult
    }

    private fun sendOnce(webhookUrl: String, sender: String, message: String): SendResult {
        var conn: HttpURLConnection? = null
        return try {
            val payload = JSONObject().apply {
                put("sender", sender)
                put("from", sender)
                put("message", message)
                put("text", message)
            }.toString()

            val url = URL(webhookUrl)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 15000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "BDAutoPaySMSBridge/1.0")
            }

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(payload)
                writer.flush()
            }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = if (stream != null) {
                BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
            } else {
                ""
            }

            SendResult(code in 200..299, code, body.ifBlank { "Empty response" })
        } catch (e: Exception) {
            SendResult(false, 0, e.message ?: "Network error")
        } finally {
            conn?.disconnect()
        }
    }
}
