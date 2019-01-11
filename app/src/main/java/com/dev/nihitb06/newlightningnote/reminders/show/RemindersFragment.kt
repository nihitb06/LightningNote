package com.dev.nihitb06.newlightningnote.reminders.show

import android.os.Bundle
import android.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.dev.nihitb06.newlightningnote.R
import com.dev.nihitb06.newlightningnote.databaseutils.LightningNoteDatabase
import kotlinx.android.synthetic.main.fragment_reminder.view.*

class RemindersFragment : Fragment() {

    private lateinit var remindersRecyclerAdapter: RemindersRecyclerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val itemView = inflater.inflate(R.layout.fragment_reminder, container, false)

        remindersRecyclerAdapter = RemindersRecyclerAdapter(activity, itemView.listEmpty)

        itemView.rvRemindersList.layoutManager = LinearLayoutManager(activity)
        itemView.rvRemindersList.adapter = remindersRecyclerAdapter

        return itemView
    }

    override fun onStop() {
        if(::remindersRecyclerAdapter.isInitialized) {
            val updatedReminders = remindersRecyclerAdapter.returnUpdatedReminders()
            val lightningNoteDatabase = LightningNoteDatabase.getDatabaseInstance(activity)
            Thread {
                for (reminder in updatedReminders) {
                    lightningNoteDatabase.reminderDao().updateReminder(reminder)
                }
            }.start()
        }

        super.onStop()
    }
}
