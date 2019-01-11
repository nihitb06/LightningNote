package com.dev.nihitb06.newlightningnote.notes.noteutils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.design.widget.Snackbar
import android.support.v4.content.FileProvider
import android.support.v7.widget.PopupMenu
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import com.dev.nihitb06.newlightningnote.R
import com.dev.nihitb06.newlightningnote.databaseutils.LightningNoteDatabase
import com.dev.nihitb06.newlightningnote.databaseutils.entities.Note
import com.dev.nihitb06.newlightningnote.reminders.ReminderCreator
import com.dev.nihitb06.newlightningnote.utils.AnimationUtils.Companion.ELEVATION_END
import com.dev.nihitb06.newlightningnote.utils.AnimationUtils.Companion.ELEVATION_START
import com.dev.nihitb06.newlightningnote.utils.AnimationUtils.Companion.setSelected
import com.dev.nihitb06.newlightningnote.utils.ImageProcessingUtils
import kotlinx.android.synthetic.main.layout_note.view.*
import java.io.File

class NoteLongClickListenerSingleSelection (
        private val context: Context,
        private val note: Note,
        private val thisView: View,
        private val visibleViews: ArrayList<View>,
        private val onlyDeleted: Boolean,
        private val notifyDataSetChanged: () -> Unit
) : View.OnLongClickListener {

    fun onLongClick(): Boolean {
        return onLongClick(thisView)
    }

    override fun onLongClick(v: View?): Boolean {
        val popupMenu = PopupMenu(context, thisView)
        popupMenu.inflate(if(onlyDeleted) R.menu.menu_note_deleted else R.menu.menu_note)
        popupMenu.setOnMenuItemClickListener { item: MenuItem? ->
            processMenuItem(item)
        }

        popupMenu.setOnDismissListener {
            unBlur()
            setSelected(thisView, 24f, 6f)
        }

        blur()
        setSelected(thisView, ELEVATION_START, ELEVATION_END)

        popupMenu.show()

        return true
    }

    private fun blur() {
        Thread {

        }.start()
        visibleViews.filter { it != thisView }.map { view: View ->
            (context as Activity).runOnUiThread {
                (view.frameLayout).foreground = BitmapDrawable(
                        context.resources,
                        ImageProcessingUtils.blurScreenshot(
                                context,
                                view.frameLayout
                        )
                )
            }
        }
    }

    private fun unBlur() {
        Thread {
            visibleViews.filter { it != thisView }.map { view: View ->
                (context as Activity).runOnUiThread { (view.frameLayout).foreground = null }
            }
        }.start()
    }

    fun processMenuItem(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.noteShare -> { shareNote() }
            R.id.noteCopy -> { copyNote() }
            R.id.noteReminder -> { addReminder() }
            R.id.noteDelete -> { deleteNote() }
            R.id.noteRestore -> { restoreNote() }
        }

        return false
    }

    private fun shareNote() {
        Thread {
            val lightningNoteDatabase = LightningNoteDatabase.getDatabaseInstance(context)
            val shareIntent = Intent(Intent.ACTION_SEND)

            val attachments = lightningNoteDatabase.attachmentDao().getAttachmentsToShare(note.id)
            if(attachments.isNotEmpty()) {
                if(attachments.size == 1) {
                    val file = File(Uri.parse(attachments[0]).path)

                    shareIntent.type = getMimeType(file.name)
                    shareIntent.putExtra(
                            Intent.EXTRA_STREAM,
                            FileProvider.getUriForFile(
                                    context,
                                    "com.dev.nihitb06.lightningnote.FileProvider",
                                    file
                            )
                    )
                } else {
                    shareIntent.action = Intent.ACTION_SEND_MULTIPLE
                    shareIntent.type = "*/*"

                    val files = ArrayList<Uri>()
                    for (uri in attachments) {
                        val file = File(Uri.parse(uri).path)
                        files.add(
                                FileProvider.getUriForFile(
                                        context,
                                        "com.dev.nihitb06.lightningnote.FileProvider",
                                        file
                                )
                        )
                    }

                    shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
                }
            } else {
                shareIntent.type = "text/plain"
            }

            shareIntent.putExtra(Intent.EXTRA_SUBJECT, note.title)
            shareIntent.putExtra(Intent.EXTRA_TITLE, note.title)
            shareIntent.putExtra(Intent.EXTRA_TEXT, note.body)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            context.startActivity(Intent.createChooser(shareIntent, "Share Note"))
        }.start()
    }
    private fun copyNote() {
        val clipBoardText = (if(note.title != "") note.title else "") + (if(note.body != "") "\n\n" + note.body else "")

        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip = ClipData.newPlainText(
                note.title,
                clipBoardText
        )

        Snackbar.make(thisView, R.string.text_copied, Snackbar.LENGTH_SHORT).show()
    }
    private fun addReminder() {
        ReminderCreator(context, arrayOf(note.id)).createReminder()
    }
    private fun deleteNote() {
        val lightningNoteDatabase = LightningNoteDatabase.getDatabaseInstance(context)
        Thread {
            if(onlyDeleted) {
                val attachmentUris = lightningNoteDatabase.attachmentDao().getAttachmentsToShare(note.id)
                lightningNoteDatabase.noteDao().deleteNote(note)

                for(uri in attachmentUris) {
                    val file = File(Uri.parse(uri).path)

                    if(file.exists())
                        file.delete()
                }
            } else {
                note.isDeleted = true
                lightningNoteDatabase.noteDao().updateNote(note)
            }

            notifyDataSetChanged()
        }.start()
    }
    private fun restoreNote() {
        Thread {
            note.isDeleted = false
            LightningNoteDatabase.getDatabaseInstance(context).noteDao().updateNote(note)

            notifyDataSetChanged()
        }.start()
    }

    private fun getMimeType(fileName: String)
            = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileName.substring(fileName.lastIndexOf(".")+1))
}