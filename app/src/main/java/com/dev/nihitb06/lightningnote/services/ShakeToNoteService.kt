package com.dev.nihitb06.lightningnote.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.dev.nihitb06.lightningnote.MainActivity
import com.dev.nihitb06.lightningnote.MainActivity.Companion.ADD_NOTE_BOOLEAN
import com.dev.nihitb06.lightningnote.R
import com.dev.nihitb06.lightningnote.reminders.ReminderNotificationService
import com.dev.nihitb06.lightningnote.reminders.ReminderNotificationService.Companion.CHANNEL_ID

class ShakeToNoteService : Service() {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var shakeListener: ShakeListener

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        shakeListener = ShakeListener(object: ShakeListener.OnShakeListener {
            override fun onShake(count: Int) {
                if(count >= 2) {
                    openActivityForNoteAddition()
                }
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sensorManager.registerListener(shakeListener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, ReminderNotificationService.NAME, NotificationManager.IMPORTANCE_MIN)
            )
        }
        startForeground(FOREGROUND_NOTIFICATION_ID, buildNotification())
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(shakeListener)
        super.onDestroy()
    }

    private fun openActivityForNoteAddition() {
        startActivity(
                Intent(this, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(ADD_NOTE_BOOLEAN, true)
        )
    }

    private fun buildNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_content))
            .setContentIntent(
                    PendingIntent.getService(
                            this,
                            50,
                            Intent(this, MainActivity::class.java),
                            PendingIntent.FLAG_UPDATE_CURRENT
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setAutoCancel(false)
            .build()

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 10
    }
}
