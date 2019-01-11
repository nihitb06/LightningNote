package com.dev.nihitb06.newlightningnote.databaseutils.extramodels

import android.arch.persistence.room.ColumnInfo

data class NoteText (
        @ColumnInfo(name = "id")
        val id: Long,

        @ColumnInfo(name = "title")
        val title: String,

        @ColumnInfo(name = "body")
        val body: String
)