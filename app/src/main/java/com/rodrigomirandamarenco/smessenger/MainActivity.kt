package com.rodrigomirandamarenco.smessenger

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodrigomirandamarenco.smessenger.config.PreferencesManager
import com.rodrigomirandamarenco.smessenger.service.SmsForwardingService
import com.rodrigomirandamarenco.smessenger.ui.theme.SMessengerTheme

class MainActivity : ComponentActivity() {
    private lateinit var preferencesManager: PreferencesManager

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferencesManager = PreferencesManager(this)

        // Request necessary permissions
        requestPermissions()

        setContent {
            SMessengerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SmsConfigurationScreen(
                        preferencesManager = preferencesManager,
                        onStartService = { startSmsForwardingService() },
                        onStopService = { 
                            val intent = Intent(this@MainActivity, SmsForwardingService::class.java)
                            stopService(intent)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.INTERNET,
            Manifest.permission.RECEIVE_BOOT_COMPLETED
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.SCHEDULE_EXACT_ALARM)
        }

        requestPermissionsLauncher.launch(permissions.toTypedArray())
    }

    private fun startSmsForwardingService() {
        val intent = Intent(this, SmsForwardingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}

@Composable
fun SmsConfigurationScreen(
    preferencesManager: PreferencesManager,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    modifier: Modifier = Modifier
) {
    val destinationEmail = remember { mutableStateOf(preferencesManager.destinationEmail) }
    val smtpServer = remember { mutableStateOf(preferencesManager.smtpServer) }
    val smtpPort = remember { mutableStateOf(preferencesManager.smtpPort.toString()) }
    val smtpUsername = remember { mutableStateOf(preferencesManager.smtpUsername) }
    val smtpPassword = remember { mutableStateOf(preferencesManager.smtpPassword) }
    val isEnabled = remember { mutableStateOf(preferencesManager.isForwardingEnabled) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "SMS Bridge Configuration",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Destination Email
                TextField(
                    value = destinationEmail.value,
                    onValueChange = { destinationEmail.value = it },
                    label = { Text("Destination Email") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )

                // SMTP Server
                TextField(
                    value = smtpServer.value,
                    onValueChange = { smtpServer.value = it },
                    label = { Text("SMTP Server") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true
                )

                // SMTP Port
                TextField(
                    value = smtpPort.value,
                    onValueChange = { smtpPort.value = it },
                    label = { Text("SMTP Port") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )

                // SMTP Username
                TextField(
                    value = smtpUsername.value,
                    onValueChange = { smtpUsername.value = it },
                    label = { Text("SMTP Username") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true
                )

                // SMTP Password
                TextField(
                    value = smtpPassword.value,
                    onValueChange = { smtpPassword.value = it },
                    label = { Text("SMTP Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true
                )
            }
        }

        // Enable/Disable Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable SMS Forwarding", fontSize = 16.sp)
            Checkbox(
                checked = isEnabled.value,
                onCheckedChange = { isEnabled.value = it }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Save and Start Button
        Button(
            onClick = {
                // Save configuration
                preferencesManager.destinationEmail = destinationEmail.value
                preferencesManager.smtpServer = smtpServer.value
                preferencesManager.smtpPort = smtpPort.value.toIntOrNull() ?: 587
                preferencesManager.smtpUsername = smtpUsername.value
                preferencesManager.smtpPassword = smtpPassword.value
                preferencesManager.isForwardingEnabled = isEnabled.value

                // Start or stop service based on toggle
                if (isEnabled.value) {
                    onStartService()
                } else {
                    onStopService()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Configuration")
        }

        Text(
            text = "Your SMTP credentials will be stored locally on this device. " +
                    "For Gmail, use your app password and enable 'Less secure app access' in your account settings.",
            fontSize = 12.sp,
            modifier = Modifier.padding(8.dp)
        )
    }
}