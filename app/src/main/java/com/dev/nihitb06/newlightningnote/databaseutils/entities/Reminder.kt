package com.dev.nihitb06.newlightningnote.databaseutils.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "Reminders")
data class Reminder (
        var year: Int,
        var month: Int,
        var day: Int,
        var hour: Int,
        var minute: Int,
        var noteId: Long
) {
    @PrimaryKey (autoGenerate = true)
    var id: Long = 0

    var isCancelled = false
}