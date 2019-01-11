package com.dev.nihitb06.newlightningnote.reminders

import android.app.*
import android.content.Context
import android.content.Intent
import android.support.design.widget.TextInputEditText
import android.widget.Button
import android.widget.Switch
import com.dev.nihitb06.newlightningnote.R
import com.dev.nihitb06.newlightningnote.databaseutils.LightningNoteDatabase
import com.dev.nihitb06.newlightningnote.databaseutils.entities.Reminder
import java.util.*

class ReminderCreator (private val context: Context, private val noteIds: Array<Long>) {

    private val calender: Calendar = Calendar.getInstance()
    private var message = ""
    private var isHigh = false

    fun createReminder() {
        DatePickerDialog(context, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val dialog = Dialog(context)
                dialog.setContentView(R.layout.layout_reminder_message_dialog)

                dialog.findViewById<Button>(R.id.btnAddReminder).setOnClickListener {
                    message = dialog.findViewById<TextInputEditText>(R.id.etReminderMessage).text.toString()
                    isHigh = dialog.findViewById<Switch>(R.id.prioritySwitch).isActivated
                    setReminder(year, month, dayOfMonth, hourOfDay, minute)
                    dialog.dismiss()
                }
                dialog.findViewById<Button>(R.id.btnDismissDialog).setOnClickListener { dialog.dismiss() }

                dialog.show()
            }, calender.get(Calendar.HOUR_OF_DAY), calender.get(Calendar.MINUTE), false).show()
        }, calender.get(Calendar.YEAR), calender.get(Calendar.MONTH), calender.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun setReminder(year: Int, month: Int, day: Int, hour: Int, minute: Int) {
        Thread {
            for(noteId in noteIds) {
                val reminderId = LightningNoteDatabase.getDatabaseInstance(context)
                        .reminderDao().insertReminder(Reminder(year, month, day, hour, minute, noteId))
                val alarmIntent = Intent(context, ReminderNotificationService::class.java)
                alarmIntent.putExtra(NOTE_ID, noteId)
                alarmIntent.putExtra(REMINDER_ID, reminderId)
                alarmIntent.putExtra(MESSAGE, message)
                alarmIntent.putExtra(PRIORITY, isHigh)

                calender.set(Calendar.YEAR, year)
                calender.set(Calendar.MONTH, month)
                calender.set(Calendar.DAY_OF_MONTH, day)
                calender.set(Calendar.HOUR_OF_DAY, hour)
                calender.set(Calendar.MINUTE, minute)
                calender.set(Calendar.SECOND, 0)

                (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
                        .set(
                                AlarmManager.RTC_WAKEUP,
                                calender.timeInMillis,
                                PendingIntent.getService(
                                        context,
                                        minute+day+hour+month+year+noteId.toInt(),
                                        alarmIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                )
                        )
            }
        }.start()
    }

    companion object {
        const val NOTE_ID = "NoteId"
        const val REMINDER_ID = "ReminderId"
        const val MESSAGE = "Reminder Content"
        const val PRIORITY = "ReminderPriority"
    }
}