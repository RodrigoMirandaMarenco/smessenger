package com.rodrigomirandamarenco.smessenger.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.util.Log
import com.rodrigomirandamarenco.smessenger.config.PreferencesManager
import com.rodrigomirandamarenco.smessenger.service.SmsForwardingService

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        val prefs = PreferencesManager(context)

        // Check if forwarding is enabled and configured
        if (!prefs.isForwardingEnabled || !prefs.isConfigured()) {
            Log.d("SmsReceiver", "Forwarding disabled or not configured")
            return
        }

        // Extract SMS messages
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        
        for (message in messages) {
            val sender = message.originatingAddress ?: "Unknown"
            val body = message.messageBody ?: ""
            
            Log.d("SmsReceiver", "Received SMS from: $sender")

            // Start the forwarding service to handle email sending
            val forwardIntent = Intent(context, SmsForwardingService::class.java).apply {
                putExtra("sender", sender)
                putExtra("body", body)
                putExtra("timestamp", System.currentTimeMillis())
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(forwardIntent)
            } else {
                context.startService(forwardIntent)
            }
        }
    }
}
