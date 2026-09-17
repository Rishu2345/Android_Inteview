package com.buildsol.androidinteview

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.Manifest
import android.R
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.buildsol.androidinteview.ui.theme.AndroidInteviewTheme
import androidx.core.net.toUri
import com.buildsol.androidinteview.services.CounterService

private const val CHANNEL_ID = "intent_demo_channel"
private const val NOTIFICATION_ID = 101

class MainActivity : ComponentActivity() {
    private lateinit var getResultLauncher : ActivityResultLauncher<Intent>

    // Launcher for the POST_NOTIFICATIONS runtime permission (required on API 33+).
    // If the user denies it, we just show a Toast instead of silently failing.
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) @androidx.annotation.RequiresPermission(android.Manifest.permission.POST_NOTIFICATIONS) { granted ->
        if (granted) {
            postNotification()
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()

        getResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){result->
            if(result.resultCode == RESULT_OK){
                val text = result.data?.getStringExtra("REPLY_KEY")
                Toast.makeText(this,"Got back : $text", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle a deep link if this Activity was launched via the intent-filter below
        handleIncomingIntent(intent)

        enableEdgeToEdge()
        setContent {
            AndroidInteviewTheme {
                IntentDemoScreen(
                    onExplicitClick = {
                        val intent = Intent(this, DetailActivity::class.java).apply {
                            putExtra("USER_NAME", "Divya")
                            putExtra("USER_AGE", 22)
                        }
                        startActivity(intent)
                    },
                    onForResultClick = {
                        getResultLauncher.launch(Intent(this, DetailActivity::class.java))
                    },
                    onOpenUrlClick = {
                        startActivity(Intent(Intent.ACTION_VIEW,
                            "https://developer.android.com".toUri()))
                    },
                    onShareClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Learning Android intents!")
                        }
                        startActivity(Intent.createChooser(shareIntent, "Share via"))
                    },
                    onDialClick = {
                        startActivity(Intent(Intent.ACTION_DIAL, "tel:9999999999".toUri()))
                    },
                    onNotifyClick = {
                        showPendingIntentNotification()
                    },
                    onStartServiceClick = {
                        startForegroundService(Intent(this, CounterService::class.java))
                    },
                    onStopServiceClick = {
                        stopService(Intent(this, CounterService::class.java))
                    }
                )
            }
        }
    }

    // If the Activity is already running (singleTop/singleTask) and a new deep link
    // comes in, onCreate won't run again — onNewIntent will instead.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val data: Uri? = intent.data
            if (data != null && data.host == "detail") {
                Toast.makeText(this, "Opened via deep link: $data", Toast.LENGTH_LONG).show()
                // You could parse data.getQueryParameter(...) here and navigate to DetailActivity
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Intent Demo Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Channel used to demo PendingIntent"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun showPendingIntentNotification() {
        // On API 33+ we must hold POST_NOTIFICATIONS before calling notify(),
        // or the notification is silently dropped (or throws SecurityException).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        postNotification()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun postNotification() {
        // This is the Intent we want fired when the user TAPS the notification.
        val contentIntent = Intent(this, DetailActivity::class.java).apply {
            putExtra("USER_NAME", "Divya (from Notification)")
            putExtra("USER_AGE", 22)
        }

        // Wrapping it in a PendingIntent lets the NotificationManager (a system process,
        // not your app) launch it later, on your app's behalf.
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, // request code — distinguishes multiple pending intents for the same component
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle("PendingIntent Demo")
            .setContentText("Tap me to open DetailActivity")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // dismiss the notification once tapped
            .build()

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
    }
}



@Composable
fun IntentDemoScreen(
    onExplicitClick: () -> Unit,
    onForResultClick: () -> Unit,
    onOpenUrlClick: () -> Unit,
    onShareClick: () -> Unit,
    onDialClick: () -> Unit,
    onNotifyClick: () -> Unit,
    onStartServiceClick:() -> Unit,
    onStopServiceClick:() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = onExplicitClick) { Text("Explicit Intent") }
        Button(onClick = onForResultClick) { Text("Start For Result") }
        Button(onClick = onOpenUrlClick) { Text("Open URL (Implicit)") }
        Button(onClick = onShareClick) { Text("Share Text (Implicit)") }
        Button(onClick = onDialClick) { Text("Dial Number (Implicit)") }
        Button(onClick = onNotifyClick) { Text("Show Notification (PendingIntent)") }
        Button(onClick = onStartServiceClick) { Text("Start Foreground Service") }
        Button(onClick = onStopServiceClick) { Text("Stop Foreground Service") }
    }
}

class DetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val name = intent.getStringExtra("USER_NAME") ?: "Unknown"
        val age = intent.getIntExtra("USER_AGE", 0)

        setContent {
            AndroidInteviewTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "Name: $name")
                        Text(text = "Age: $age")
                        Button(onClick = {
                            val resultIntent = Intent().apply {
                                putExtra("REPLY_KEY", "Hello from Detail!")
                            }
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        }) {
                            Text("Send Result Back")
                        }
                    }
                }
            }
        }
    }
}