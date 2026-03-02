package com.rodrigomirandamarenco.smessenger.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.rodrigomirandamarenco.smessenger.R
import com.rodrigomirandamarenco.smessenger.config.PreferencesManager
import com.rodrigomirandamarenco.smessenger.email.SmtpEmailSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsForwardingService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Create notification channel for foreground service
        createNotificationChannel()

        // Show foreground notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SMS Bridge Active")
            .setContentText("Forwarding incoming SMS to email")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        // Handle SMS forwarding if data is provided (from SmsReceiver)
        intent?.let {
            val sender = it.getStringExtra("sender")
            val body = it.getStringExtra("body")

            if (sender != null && body != null) {
                Log.d("SmsForwardingService", "Processing SMS from $sender")
                forwardSmsToEmail(sender, body)
            }
        }

        return START_STICKY
    }

    private fun forwardSmsToEmail(sender: String, body: String) {
        scope.launch {
            val prefs = PreferencesManager(this@SmsForwardingService)

            if (!prefs.isConfigured()) {
                Log.e("SmsForwardingService", "SMTP not configured")
                return@launch
            }

            val emailSender = SmtpEmailSender(
                smtpServer = prefs.smtpServer,
                smtpPort = prefs.smtpPort,
                username = prefs.smtpUsername,
                password = prefs.smtpPassword
            )

            val subject = "SMS from $sender"
            val emailBody = "From: $sender\n\n$body"

            val success = emailSender.sendEmail(
                toEmail = prefs.destinationEmail,
                subject = subject,
                body = emailBody
            )

            if (success) {
                Log.d("SmsForwardingService", "SMS forwarded successfully to ${prefs.destinationEmail}")
            } else {
                Log.e("SmsForwardingService", "Failed to forward SMS")
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SMS Bridge Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notification for SMS forwarding service"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "sms_bridge_channel"
        private const val NOTIFICATION_ID = 1
    }
}
