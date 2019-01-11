package com.dev.nihitb06.newlightningnote.databaseutils.dao

import android.arch.persistence.room.*
import com.dev.nihitb06.newlightningnote.databaseutils.entities.Note
import com.dev.nihitb06.newlightningnote.databaseutils.extramodels.NoteText

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNote(note: Note): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateNote(note: Note): Int

    @Delete
    fun deleteNote(note: Note): Int

    @Query("SELECT * FROM Notes WHERE id = :noteId")
    fun getNoteById(noteId: Long): Note

    @Query("SELECT * FROM Notes")
    fun getAllNotes(): List<Note>

    @Query("SELECT * FROM Notes WHERE isStarred = 1 AND isDeleted = 0")
    fun getStarredNotes(): List<Note>

    @Query("SELECT * FROM Notes WHERE isDeleted = 1")
    fun getDeletedNotes(): List<Note>

    @Query("SELECT * FROM Notes WHERE isDeleted = 0")
    fun getUnDeletedNotes(): List<Note>

    @Query("SELECT * FROM Notes WHERE isDeleted = 0 ORDER BY dateUpdated DESC")
    fun getUndeletedNotesLastUpdated(): List<Note>

    @Query("SELECT * FROM Notes WHERE isDeleted = 0 ORDER BY dateCreated DESC")
    fun getUndeletedNotesNewest(): List<Note>

    @Query("SELECT * FROM Notes WHERE isDeleted = 0 ORDER BY dateCreated ASC")
    fun getUndeletedNotesOldest(): List<Note>

    @Query("SELECT id, title, body FROM Notes WHERE title LIKE :query OR body LIKE :query")
    fun search(query: String): List<NoteText>

    @Query("SELECT COUNT(*) FROM notes")
    fun getCount(): Long

    @Query("SELECT id FROM notes ORDER BY id DESC LIMIT 1")
    fun getLastId(): Long
}
