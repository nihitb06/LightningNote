package com.dev.nihitb06.newlightningnote.databaseutils.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "Attachments", foreignKeys = [ForeignKey(
        entity = Note::class,
        parentColumns = ["id"],
        childColumns = ["noteId"],
        onDelete = ForeignKey.CASCADE)])
data class Attachment (
    var uri: String,

    var noteId: Long,
    var type: Int
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    companion object {
        const val IMAGE = 0
        const val VIDEO = 1
        const val AUDIO = 2
        const val GEO_LOCATION = 3
        const val OTHER = -1
    }
}