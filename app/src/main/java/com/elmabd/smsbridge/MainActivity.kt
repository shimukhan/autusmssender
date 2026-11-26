package com.elmabd.smsbridge

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var switchEnabled: Switch
    private lateinit var etWebhookUrl: EditText
    private lateinit var etSenders: EditText
    private lateinit var btnSave: Button
    private lateinit var btnTest: Button
    private lateinit var btnBattery: Button
    private lateinit var btnClearLogs: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvLogs: TextView

    private val bgExecutor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        switchEnabled = findViewById(R.id.switchEnabled)
        etWebhookUrl = findViewById(R.id.etWebhookUrl)
        etSenders = findViewById(R.id.etSenders)
        btnSave = findViewById(R.id.btnSave)
        btnTest = findViewById(R.id.btnTest)
        btnBattery = findViewById(R.id.btnBattery)
        btnClearLogs = findViewById(R.id.btnClearLogs)
        tvStatus = findViewById(R.id.tvStatus)
        tvLogs = findViewById(R.id.tvLogs)

        requestNeededPermissions()
        loadSavedSettings()

        btnSave.setOnClickListener {
            val url = etWebhookUrl.text.toString().trim()
            val senders = etSenders.text.toString().trim().ifEmpty { Prefs.DEFAULT_SENDERS }
            val enabled = switchEnabled.isChecked

            if (url.isBlank() || (!url.startsWith("http://") && !url.startsWith("https://"))) {
                Toast.makeText(this, "সঠিক Webhook URL দিন (https:// দিয়ে শুরু)", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            Prefs.setWebhookUrl(this, url)
            Prefs.setSenderFilters(this, senders)
            Prefs.setEnabled(this, enabled)

            if (enabled) {
                KeepAliveService.start(this)
                tvStatus.text = "সেটিংস সেভ হয়েছে ও ব্যাকগ্রাউন্ড সার্ভিস চালু আছে।"
            } else {
                KeepAliveService.stop(this)
                tvStatus.text = "সেটিংস সেভ হয়েছে (ফরোয়ার্ডিং বন্ধ রাখা হয়েছে)।"
            }
            refreshLogs()
        }

        btnTest.setOnClickListener {
            val url = etWebhookUrl.text.toString().trim()
            if (url.isBlank()) {
                Toast.makeText(this, "আগে Webhook URL দিয়ে সেভ করুন।", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Prefs.setWebhookUrl(this, url)
            btnTest.isEnabled = false
            tvStatus.text = "টেস্ট পেমেন্ট SMS পাঠানো হচ্ছে..."

            val randomCode = "APP" + (100000..999999).random()
            val sampleSms = "You have received Tk 50.00 from 01711000000. Ref 1. Fee Tk 0.00. Balance Tk 1050.00. TrxID $randomCode at 26/11/2026 17:15"

            bgExecutor.execute {
                val res = WebhookSender.sendWithRetry(this, "bKash", sampleSms, maxAttempts = 1)
                runOnUiThread {
                    btnTest.isEnabled = true
                    tvStatus.text = if (res.ok) {
                        "সফল! [HTTP ${res.code}] TrxID: $randomCode WordPress-এ পৌঁছেছে।"
                    } else {
                        "ব্যর্থ [HTTP ${res.code}]: ${res.body.take(150)}"
                    }
                    refreshLogs()
                }
            }
        }

        btnBattery.setOnClickListener {
            openBatteryOptimizationSettings()
        }

        btnClearLogs.setOnClickListener {
            Prefs.clearLogs(this)
            refreshLogs()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshLogs()
    }

    private fun loadSavedSettings() {
        switchEnabled.isChecked = Prefs.isEnabled(this)
        etWebhookUrl.setText(Prefs.getWebhookUrl(this))
        etSenders.setText(Prefs.getSenderFilters(this))
        refreshLogs()

        if (Prefs.isEnabled(this) && Prefs.getWebhookUrl(this).isNotBlank()) {
            KeepAliveService.start(this)
        }
    }

    private fun refreshLogs() {
        tvLogs.text = Prefs.getLogsText(this)
    }

    private fun requestNeededPermissions() {
        val perms = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val missing = perms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 2001)
        }
    }

    private fun openBatteryOptimizationSettings() {
        try {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
                return
            }
        } catch (_: Exception) {
        }
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "ফোনের Settings → Apps থেকে Battery Unrestricted করুন।", Toast.LENGTH_LONG).show()
        }
    }
}
