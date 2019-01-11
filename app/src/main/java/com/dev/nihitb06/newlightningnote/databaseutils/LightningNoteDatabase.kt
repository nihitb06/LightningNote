package com.dev.nihitb06.newlightningnote.databaseutils

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.dev.nihitb06.newlightningnote.databaseutils.dao.AttachmentDao
import com.dev.nihitb06.newlightningnote.databaseutils.dao.NoteDao
import com.dev.nihitb06.newlightningnote.databaseutils.dao.ReminderDao
import com.dev.nihitb06.newlightningnote.databaseutils.entities.Attachment
import com.dev.nihitb06.newlightningnote.databaseutils.entities.Note
import com.dev.nihitb06.newlightningnote.databaseutils.entities.Reminder

@Database(entities = [Note::class, Reminder::class, Attachment::class], version = 1)
abstract class LightningNoteDatabase : RoomDatabase () {

    abstract fun noteDao(): NoteDao
    abstract fun reminderDao(): ReminderDao
    abstract fun attachmentDao(): AttachmentDao

    companion object {
        private var databaseInstance: LightningNoteDatabase? = null

        @Synchronized
        fun getDatabaseInstance(context: Context): LightningNoteDatabase {
            if (databaseInstance == null) {
                databaseInstance = Room.databaseBuilder(
                        context.applicationContext,
                        LightningNoteDatabase::class.java,
                        "LightningNoteDatabase"
                ).build()
            }

            return databaseInstance!!
        }
    }
}