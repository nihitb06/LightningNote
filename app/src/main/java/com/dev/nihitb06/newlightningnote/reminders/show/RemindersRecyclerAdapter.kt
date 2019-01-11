package com.dev.nihitb06.newlightningnote.reminders.show

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dev.nihitb06.newlightningnote.MainActivity
import com.dev.nihitb06.newlightningnote.MainActivity.Companion.OPEN_NOTE_ID
import com.dev.nihitb06.newlightningnote.R
import com.dev.nihitb06.newlightningnote.databaseutils.LightningNoteDatabase
import com.dev.nihitb06.newlightningnote.databaseutils.entities.Reminder
import kotlinx.android.synthetic.main.layout_reminder.view.*

class RemindersRecyclerAdapter (private val context: Context, private val listEmptyView: View)
    : RecyclerView.Adapter<RemindersRecyclerAdapter.ReminderViewHolder> () {

    private lateinit var reminders: List<Reminder>
    private val updatedReminders: ArrayList<Reminder> = ArrayList()
    init {
        Thread {
            reminders = LightningNoteDatabase.getDatabaseInstance(context).reminderDao().getReminders()

            (context as Activity).runOnUiThread {
                notifyDataSetChanged()
                setEmptyPlaceholder()
            }
        }.start()
    }
    private fun setEmptyPlaceholder() {
        if(reminders.isEmpty())
            listEmptyView.visibility = View.VISIBLE
    }

    inner class ReminderViewHolder (private val thisView: View) : RecyclerView.ViewHolder (thisView) {

        fun bindReminder(reminder: Reminder) {
            thisView.tvReminderTime.text = context.resources.getString(R.string.time, (reminder.hour % 12), reminder.minute)
            thisView.tvTimeOfDay.text = if(reminder.hour%12 == reminder.hour) context.getString(R.string.am) else context.getString(R.string.pm)
            thisView.tvReminderDate.text = context.resources.getString(R.string.date, reminder.day, reminder.month, reminder.year)
            thisView.switchCancelled.isChecked = !reminder.isCancelled
            thisView.switchCancelled.setOnCheckedChangeListener { _, isChecked ->
                reminder.isCancelled = !isChecked
                updatedReminders.add(reminder)
            }

            thisView.setOnClickListener {
                context.startActivity(Intent(context, MainActivity::class.java).putExtra(OPEN_NOTE_ID, reminder.noteId))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            = ReminderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_reminder, parent, false))

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bindReminder(reminders[position])
    }

    override fun getItemCount() = if(::reminders.isInitialized) reminders.size else 0

    fun returnUpdatedReminders() = updatedReminders
}