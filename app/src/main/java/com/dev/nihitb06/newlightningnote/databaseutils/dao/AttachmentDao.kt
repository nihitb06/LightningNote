package com.dev.nihitb06.newlightningnote.databaseutils.dao

import android.arch.persistence.room.*
import com.dev.nihitb06.newlightningnote.databaseutils.entities.Attachment

@Dao
interface AttachmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun createAttachment(attachment: Attachment): Long

    @Delete
    fun deleteAttachment(attachment: Attachment): Int

    @Query("SELECT * FROM Attachments WHERE noteId=:noteId")
    fun getNoteAttachments(noteId: Long): List<Attachment>

    @Query("SELECT uri FROM Attachments WHERE noteId=:noteId AND type=0 LIMIT 1")
    fun getImageForList(noteId: Long): String

    @Query("SELECT COUNT(*) FROM Attachments WHERE noteId=:noteId")
    fun countAttachments(noteId: Long): Int

    @Query("SELECT uri FROM Attachments WHERE noteId=:noteId")
    fun getAttachmentsToShare(noteId: Long): List<String>
}