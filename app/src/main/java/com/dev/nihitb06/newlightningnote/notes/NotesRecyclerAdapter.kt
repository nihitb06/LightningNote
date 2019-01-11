package com.dev.nihitb06.newlightningnote.notes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.widget.ImageViewCompat
import android.support.v7.widget.RecyclerView
import android.view.*
import com.dev.nihitb06.newlightningnote.R
import com.dev.nihitb06.newlightningnote.attachment.AttachmentDetailsActivity
import com.dev.nihitb06.newlightningnote.attachment.AttachmentParcelable
import com.dev.nihitb06.newlightningnote.databaseutils.LightningNoteDatabase
import com.dev.nihitb06.newlightningnote.databaseutils.entities.Attachment
import com.dev.nihitb06.newlightningnote.databaseutils.entities.Note
import com.dev.nihitb06.newlightningnote.notes.noteutils.MultiSelectionModeHelper
import com.dev.nihitb06.newlightningnote.notes.noteutils.NoteLongClickListenerSingleSelection
import com.dev.nihitb06.newlightningnote.utils.OnNoteClickListener
import com.dev.nihitb06.newlightningnote.reminders.ReminderCreator
import com.dev.nihitb06.newlightningnote.utils.ImageUtils
import kotlinx.android.synthetic.main.layout_note.view.*
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class NotesRecyclerAdapter (
        private val context: Context,
        private val onlyStarred: Boolean,
        private val onlyDeleted: Boolean,
        private val listEmptyView: View,
        private val onNoteClickListener: OnNoteClickListener?,
        private var sortBy: Int,
        private val isThemeDark: Boolean,
        private val isListLinear: Boolean
) : RecyclerView.Adapter<NotesRecyclerAdapter.NoteViewHolder> () {

    private lateinit var notes: List<Note>
    private val visibleViews = ArrayList<View>()
    private val lightningNoteDatabase = LightningNoteDatabase.getDatabaseInstance(context)

    private val isMultipleSelectionEnabled = PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(context.getString(R.string.key_multi_selection), false)
    private val multiSelectionModeHelper = MultiSelectionModeHelper(context)
    private var noteActionMode: ActionMode? =  null
    private val noteActionModeCallback = object: ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val inflater = mode?.menuInflater
            inflater?.let {
                inflater.inflate(if(onlyDeleted) R.menu.menu_note_deleted else R.menu.menu_note_multiple_selection, menu)
                return true
            }

            return false
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            val selected = multiSelectionModeHelper.getSelected()
            val firstPosition = try { multiSelectionModeHelper.getCheckedItemFirstPosition() } catch (e: IndexOutOfBoundsException) { 0 }
            val lastPosition = try { multiSelectionModeHelper.getCheckedItemLastPosition() } catch (e: IndexOutOfBoundsException) { itemCount - 1 }
            when(item?.itemId) {
                R.id.noteReminder -> {
                    if(multiSelectionModeHelper.getCheckedItemCount() > 0) {
                        ReminderCreator(
                                context,
                                Array(multiSelectionModeHelper.getCheckedItemCount()) {
                                    notes[selected[it]].id
                                }
                        ).createReminder()
                    }

                    mode?.finish()
                    return true
                }

                R.id.noteRestore -> {
                    if(multiSelectionModeHelper.getCheckedItemCount() > 0)
                        Thread {
                            for(i in selected.size -1 downTo 0) {
                                try {
                                    val index = selected[i]
                                    val note = notes[index]
                                    note.isDeleted = false

                                    lightningNoteDatabase.noteDao().updateNote(note)

                                    (notes as ArrayList).remove(note)
                                } catch (e: IndexOutOfBoundsException) {
                                    e.printStackTrace()
                                }
                            }
                            updateList(firstPosition, lastPosition)
                        }.start()

                    mode?.finish()
                    return true
                }

                R.id.noteDelete -> {
                    if(multiSelectionModeHelper.getCheckedItemCount() > 0)
                        Thread {
                            for(i in selected.size -1 downTo 0) {
                                try {
                                    val index = selected[i]
                                    val note = notes[index]

                                    if(onlyDeleted) {
                                        val attachmentUris = lightningNoteDatabase.attachmentDao().getAttachmentsToShare(note.id)
                                        lightningNoteDatabase.noteDao().deleteNote(note)

                                        for(uri in attachmentUris) {
                                            try {
                                                val file = File(Uri.parse(uri).path)

                                                if(file.exists())
                                                    file.delete()
                                            } catch (e: NullPointerException) {
                                                e.printStackTrace()
                                            } catch (e: IOException) {
                                                e.printStackTrace()
                                            }
                                        }
                                    } else {
                                        note.isDeleted = true
                                        lightningNoteDatabase.noteDao().updateNote(note)
                                    }

                                    (notes as ArrayList).remove(note)
                                    (context as Activity).runOnUiThread {
                                        notifyItemRemoved(index)
                                        setEmptyPlaceholder()
                                    }
                                } catch (e: IndexOutOfBoundsException) {
                                    e.printStackTrace()
                                }
                            }
                            updateList(firstPosition, lastPosition)
                        }.start()

                    mode?.finish()
                    return true
                }
            }

            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            multiSelectionModeHelper.clearChoices()
            noteActionMode = null
        }
    }

    private fun updateList(firstPosition: Int, lastPosition: Int) {
        (context as Activity).runOnUiThread {
            if(firstPosition == lastPosition)
                notifyItemRemoved(firstPosition)
            else
                notifyItemRangeChanged(firstPosition, lastPosition)
            setEmptyPlaceholder()
        }
    }

    init {
        Thread {
            setNotes()
        }.start()
    }

    fun changeSortBy(sortBy: Int) {
        this.sortBy = sortBy
        Thread { setNotes() }.start()
    }

    private fun setNotes() {
        notes = when {
            onlyStarred -> lightningNoteDatabase.noteDao().getStarredNotes()

            onlyDeleted -> lightningNoteDatabase.noteDao().getDeletedNotes()

            else -> when(sortBy) {
                NotesFragment.ORDER_LAST_UPDATED -> lightningNoteDatabase.noteDao().getUndeletedNotesLastUpdated()
                NotesFragment.ORDER_NEWEST -> lightningNoteDatabase.noteDao().getUndeletedNotesNewest()
                NotesFragment.ORDER_OLDEST -> lightningNoteDatabase.noteDao().getUndeletedNotesOldest()
                else -> lightningNoteDatabase.noteDao().getUnDeletedNotes()
            }
        }

        (context as Activity).runOnUiThread {
            notifyItemRangeChanged(0, notes.size)
            setEmptyPlaceholder()
        }
    }

    private fun setEmptyPlaceholder() {
        if(notes.isEmpty())
            listEmptyView.visibility = View.VISIBLE
    }

    inner class NoteViewHolder (private val thisView: View) : RecyclerView.ViewHolder (thisView) {

        init {
            visibleViews.add(thisView)
        }

        fun bindNote(note: Note, position: Int) {
            thisView.tvNoteTitle.text = note.title
            thisView.tvNoteBody.text = note.body

            thisView.tvNoteTime.text = SimpleDateFormat.getDateTimeInstance().format(Date(note.dateUpdated))

            if(isThemeDark) {
                val tint = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))

                ImageViewCompat.setImageTintList(thisView.attachment, tint)
                ImageViewCompat.setImageTintList(thisView.starred, tint)
                ImageViewCompat.setImageTintList(thisView.un_starred, tint)
            }

            thisView.contentArea.setOnLongClickListener {
                if(isMultipleSelectionEnabled) {
                    if(noteActionMode != null)
                        return@setOnLongClickListener false
                    noteActionMode = (context as Activity).startActionMode(noteActionModeCallback)
                    setSelected(position)
                    return@setOnLongClickListener true
                } else {
                    return@setOnLongClickListener NoteLongClickListenerSingleSelection(context, note, thisView, visibleViews, onlyDeleted) {
                        removeNote(note, position)
                    }.onLongClick()
                }
            }

            thisView.contentArea.setOnClickListener {
                if(noteActionMode != null) {
                    toggleSelected(position)
                } else {
                    if(!onlyDeleted)
                        onNoteClickListener?.onNoteClick(note.id)
                }
            }
            if(!onlyDeleted) {

                setSymbols(note, position)

                if(note.hasAttachment) {
                    Thread {
                        val imgUri = lightningNoteDatabase.attachmentDao().getImageForList(note.id)
                        (context as Activity).runOnUiThread {
                            var success = false
                            try {
                                thisView.attachmentImage.visibility = View.VISIBLE
                                thisView.requestLayout()
                                thisView.attachmentImage.post {
                                    try {
                                        ImageUtils.setImage(context, thisView.attachmentImage, Uri.parse(imgUri).path, isListLinear)
                                    } catch (e: NullPointerException) {
                                        e.printStackTrace()
                                        thisView.attachmentImage.visibility = View.GONE
                                    }
                                }

                                thisView.attachmentImage.setOnClickListener {
                                    context.startActivity(
                                            Intent(context, AttachmentDetailsActivity::class.java)
                                                    .putExtra(AttachmentDetailsActivity.POSITION, position)
                                                    .putExtra(
                                                            AttachmentDetailsActivity.URIS,
                                                            arrayOf(AttachmentParcelable(imgUri, Attachment.IMAGE))
                                                    )
                                    )
                                }

                                success = true
                            } catch (e: FileNotFoundException) {
                                Snackbar.make(listEmptyView, "One or more Attachments seem to be missing", Snackbar.LENGTH_SHORT).show()
                            }  catch (e: IOException) {
                                e.printStackTrace()
                            } finally {
                                if(!success)
                                    thisView.attachmentImage.visibility = View.GONE
                            }
                        }
                    }.start()
                } else {
                    thisView.attachmentImage.visibility = View.GONE
                }
            }
        }

        private fun setSymbols(note: Note, position: Int) {
            initialize()
            var view = thisView.un_starred
            if(note.isStarred) {
                thisView.starred.visibility = View.VISIBLE
                view = thisView.starred
            } else
                thisView.un_starred.visibility = View.VISIBLE

            view.setOnClickListener {
                note.isStarred = !note.isStarred
                Thread {
                    lightningNoteDatabase.noteDao().updateNote(note)
                    (context as Activity).runOnUiThread {
                        if(onlyStarred) {
                            removeNote(note, position)
                        } else {
                            notifyItemChanged(position)
                        }
                    }
                }.start()
            }

            if(note.hasAttachment)
                thisView.attachment.visibility = View.VISIBLE
        }

        private fun initialize() {
            thisView.starred.visibility = View.GONE
            thisView.un_starred.visibility = View.GONE
            thisView.attachment.visibility = View.GONE
        }

        private fun removeNote(note: Note, position: Int) {
            visibleViews.remove(thisView)
            (notes as ArrayList).remove(note)
            (context as Activity).runOnUiThread {
                notifyItemRemoved(position)
                setEmptyPlaceholder()
            }
        }

        private fun setSelected(position: Int) {
            multiSelectionModeHelper.setItemChecked(thisView, position)
        }
        private fun toggleSelected(position: Int) {
            multiSelectionModeHelper.toggleItemChecked(thisView, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder
            = NoteViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_note, parent, false))

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bindNote(notes[position], position)
    }

    override fun getItemCount() = if(::notes.isInitialized) notes.size else 0
}