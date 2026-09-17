package com.buildsol.androidinteview.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class CounterService : Service(){

    private var count = 0
    private var job : Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification(count))

        job = scope.launch {
            while(true){
                delay(1000.milliseconds)
                count++
                updateNotification(count)
            }
        }
        return START_STICKY
    }

    override fun onDestroy(){
        super.onDestroy()
        job?.cancel()
    }

    private fun buildNotification(count:Int): Notification{
        return NotificationCompat.Builder(this,CHANNEL_ID)
            .setContentTitle("Counter Service Running")
            .setContentText("Count: $count")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()

    }

    private fun createNotificationChannel(){
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Counter Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }


    private fun updateNotification(count:Int){
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID,buildNotification(count))
    }

    companion object {
        const val NOTIFICATION_ID = 42
        const val CHANNEL_ID = "counter_channel"
    }
}