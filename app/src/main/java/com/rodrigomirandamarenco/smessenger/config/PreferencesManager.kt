package com.rodrigomirandamarenco.smessenger.config

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    var destinationEmail: String
        get() = prefs.getString(KEY_DESTINATION_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_DESTINATION_EMAIL, value).apply()

    var smtpServer: String
        get() = prefs.getString(KEY_SMTP_SERVER, DEFAULT_SMTP_SERVER) ?: DEFAULT_SMTP_SERVER
        set(value) = prefs.edit().putString(KEY_SMTP_SERVER, value).apply()

    var smtpPort: Int
        get() = prefs.getInt(KEY_SMTP_PORT, DEFAULT_SMTP_PORT)
        set(value) = prefs.edit().putInt(KEY_SMTP_PORT, value).apply()

    var smtpUsername: String
        get() = prefs.getString(KEY_SMTP_USERNAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SMTP_USERNAME, value).apply()

    var smtpPassword: String
        get() = prefs.getString(KEY_SMTP_PASSWORD, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SMTP_PASSWORD, value).apply()

    var isForwardingEnabled: Boolean
        get() = prefs.getBoolean(KEY_FORWARDING_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_FORWARDING_ENABLED, value).apply()

    fun isConfigured(): Boolean {
        return destinationEmail.isNotEmpty() &&
                smtpServer.isNotEmpty() &&
                smtpUsername.isNotEmpty() &&
                smtpPassword.isNotEmpty()
    }

    companion object {
        private const val PREFS_NAME = "sms_bridge_prefs"
        private const val KEY_DESTINATION_EMAIL = "destination_email"
        private const val KEY_SMTP_SERVER = "smtp_server"
        private const val KEY_SMTP_PORT = "smtp_port"
        private const val KEY_SMTP_USERNAME = "smtp_username"
        private const val KEY_SMTP_PASSWORD = "smtp_password"
        private const val KEY_FORWARDING_ENABLED = "forwarding_enabled"

        // Default Gmail SMTP settings
        private const val DEFAULT_SMTP_SERVER = "smtp.gmail.com"
        private const val DEFAULT_SMTP_PORT = 587
    }
}
