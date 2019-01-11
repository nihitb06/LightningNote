package com.dev.nihitb06.newlightningnote.databaseutils.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "Notes")
data class Note (
        var title: String,
        var body: String
) {
    @PrimaryKey ( autoGenerate = true )
    var id: Long = 0

    var isStarred: Boolean = false
    var isDeleted: Boolean = false

    var hasAttachment: Boolean = false

    var dateCreated: Long = System.currentTimeMillis()
    var dateUpdated: Long = System.currentTimeMillis()
}