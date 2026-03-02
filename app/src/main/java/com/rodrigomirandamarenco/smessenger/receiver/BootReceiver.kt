package com.rodrigomirandamarenco.smessenger.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.rodrigomirandamarenco.smessenger.config.PreferencesManager
import com.rodrigomirandamarenco.smessenger.service.SmsForwardingService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        val prefs = PreferencesManager(context)
        
        // Start the foreground service if forwarding is enabled
        if (prefs.isForwardingEnabled && prefs.isConfigured()) {
            Log.d("BootReceiver", "Boot completed, starting SmsForwardingService")
            val serviceIntent = Intent(context, SmsForwardingService::class.java)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}
