package com.dev.nihitb06.newlightningnote.reminders

import android.app.NotificationManager
import android.app.RemoteInput
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.dev.nihitb06.newlightningnote.R
import com.dev.nihitb06.newlightningnote.databaseutils.LightningNoteDatabase
import com.dev.nihitb06.newlightningnote.reminders.ReminderCreator.Companion.NOTE_ID
import com.dev.nihitb06.newlightningnote.reminders.ReminderNotificationService.Companion.ACTION
import com.dev.nihitb06.newlightningnote.reminders.ReminderNotificationService.Companion.CHANNEL_ID
import com.dev.nihitb06.newlightningnote.reminders.ReminderNotificationService.Companion.NOTIFICATION_CANCEL
import com.dev.nihitb06.newlightningnote.reminders.ReminderNotificationService.Companion.NOTIFICATION_ID
import com.dev.nihitb06.newlightningnote.reminders.ReminderNotificationService.Companion.NOTIFICATION_NOTE_UPDATE
import com.dev.nihitb06.newlightningnote.reminders.ReminderNotificationService.Companion.UPDATE_NOTE_KEY
import com.dev.nihitb06.newlightningnote.reminders.ReminderNotificationService.Companion.count

class ManageNotificationActionService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = intent?.getIntExtra(NOTIFICATION_ID, -1) ?: -1
        val lightningNoteDatabase = LightningNoteDatabase.getDatabaseInstance(this)

        when (intent?.getStringExtra(ACTION)) {
            NOTIFICATION_CANCEL -> {
                if(notificationId != -1) {
                    notificationManager.cancel(notificationId)
                    count--
                }
            }

            NOTIFICATION_NOTE_UPDATE -> {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                    val remoteInput = RemoteInput.getResultsFromIntent(intent)
                    remoteInput?.let {
                        Thread {
                            val note = lightningNoteDatabase.noteDao().getNoteById(
                                    intent.getLongExtra(NOTE_ID, -1)
                            )

                            val body = note.body
                            note.body = body + remoteInput.getCharSequence(UPDATE_NOTE_KEY)

                            lightningNoteDatabase.noteDao().updateNote(note)
                        }.start()
                    }

                    notificationManager.cancel(notificationId)
                    notificationManager.notify(
                            notificationId,
                            NotificationCompat.Builder(this, CHANNEL_ID)
                                    .setSmallIcon(R.mipmap.ic_launcher_foreground)
                                    .setContentTitle("Note Updated")
                                    .setColor(intent.getIntExtra("Color", 0))
                                    .build()
                    )

                    count--
                }
            }
        }

        if(count == 300)
            notificationManager.cancel(0)

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
