package com.dev.nihitb06.newlightningnote.databaseutils.dao

import android.arch.persistence.room.*
import com.dev.nihitb06.newlightningnote.databaseutils.entities.Reminder

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertReminder(reminder: Reminder): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateReminder(reminder: Reminder): Int

    @Delete
    fun deleteReminder(reminder: Reminder): Int

    @Query("SELECT * FROM Reminders")
    fun getReminders(): List<Reminder>

    @Query("SELECT * FROM Reminders WHERE Id = :reminderId")
    fun getReminderById(reminderId: Long): Reminder
}