package com.dev.nihitb06.newlightningnote.notes.noteutils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.support.design.widget.TextInputEditText
import android.support.v4.content.ContextCompat
import android.support.v4.widget.ImageViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.dev.nihitb06.newlightningnote.R
import com.dev.nihitb06.newlightningnote.databaseutils.LightningNoteDatabase
import com.dev.nihitb06.newlightningnote.databaseutils.entities.Note
import com.dev.nihitb06.newlightningnote.notes.AttachmentRecyclerAdapter
import com.dev.nihitb06.newlightningnote.reminders.ReminderCreator
import com.dev.nihitb06.newlightningnote.utils.AttachmentUriManager
import com.dev.nihitb06.newlightningnote.utils.PermissionManager
import com.nightonke.boommenu.BoomButtons.TextOutsideCircleButton
import kotlinx.android.synthetic.main.fragment_add_note.view.*

class AddShowFunctionality (private val context: Context, private val itemView: View, private val isThemeDark: Boolean) {

    private val actions = arrayOf(
            "Click Photo",
            "Record Video",
            "Record Audio",
            "Add Photo ",
            "Add Video ",
            "Add Audio",
            "Geo Location"
    )

    private val icons = arrayOf(
            R.drawable.ic_photo_camera_white_24dp,
            R.drawable.ic_videocam_white_24dp,
            R.drawable.ic_keyboard_voice_white_24dp,
            R.drawable.ic_photo_white_24dp,
            R.drawable.ic_local_movies_white_24dp,
            R.drawable.ic_audiotrack_white_24dp,
            R.drawable.ic_place_white_24dp
    )

    private val colors = arrayOf(
            ContextCompat.getColor(context, R.color.colorDarkAccent),
            ContextCompat.getColor(context, R.color.colorAccent),
            ContextCompat.getColor(context, R.color.colorRedAccent),
            ContextCompat.getColor(context, R.color.colorTealAccent),
            ContextCompat.getColor(context, R.color.colorGreenAccent),
            ContextCompat.getColor(context, R.color.colorBlueAccent),
            ContextCompat.getColor(context, R.color.colorRedAccent)
    )

    private val permissionLists = arrayOf(
            arrayOf(
                    Manifest.permission.CAMERA
            ),
            arrayOf(
                    Manifest.permission.RECORD_AUDIO
            ),
            if (Build.VERSION.SDK_INT >= 16) {
                arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            } else {
                arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            },
            arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            )
    )

    fun setupView(thisNote: Note, oldNote: Note?) {
        Thread {
            val id = if(thisNote.id == 0L) LightningNoteDatabase.getDatabaseInstance(context).noteDao().getLastId()+1 else thisNote.id
            (context as Activity).runOnUiThread { setAttachmentAdapter(oldNote?.run { thisNote.id } ?: id, thisNote.hasAttachment) }
            initializeAttachments(oldNote?.run { thisNote.id } ?: id, oldNote?.run { false } ?: true)
        }.start()
        setNoteChangeListeners(thisNote, oldNote)
        setStarred(thisNote)
    }

    private fun setAttachmentAdapter(noteId: Long, hasAttachment: Boolean) {
        itemView.rvAttachmentsList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        itemView.rvAttachmentsList.adapter = AttachmentRecyclerAdapter(context, noteId, hasAttachment, itemView.rvAttachmentsList)
    }

    private fun initializeAttachments(noteId: Long, isNoteBeingAdded: Boolean) {
        val attachmentUriManager = AttachmentUriManager(context, noteId)

        for (i in 0..6) {
            itemView.attachmentButton.addBuilder(
                    TextOutsideCircleButton.Builder()
                            .normalImageRes(icons[i])
                            .normalText(actions[i])
                            .normalColor(colors[i])
                            .shadowEffect(true)
                            .rippleEffect(true)
                            .listener { index: Int ->
                                PermissionManager.askForPermission(
                                        context,
                                        when(i) {
                                            0, 1 -> permissionLists[0]

                                            2 -> permissionLists[1]

                                            3, 4, 5 -> permissionLists[2]

                                            6 -> permissionLists[3]

                                            else -> arrayOf()
                                        }, object : PermissionManager.OnPermissionResultListener {
                                    override fun onGranted() {
                                        attachmentUriManager.createIntent(index, itemView.rvAttachmentsList.adapter as AttachmentRecyclerAdapter, isNoteBeingAdded)
                                    }

                                    override fun onDenied() {
                                        Toast.makeText(context, "Permission is Required", Toast.LENGTH_LONG).show()
                                    }
                                }
                                )
                            }
            )
        }

        itemView.attachmentButton.visibility = View.VISIBLE
    }

    private fun setNoteChangeListeners(thisNote: Note, oldNote: Note?) {
        if(isThemeDark)
            ImageViewCompat.setImageTintList(itemView.starred, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)))

        (itemView.noteTitle as TextInputEditText).addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if(((oldNote != null) && (s?.toString() != oldNote.title)) || oldNote == null)
                    thisNote.title = s?.toString() ?: ""
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Do Nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //Do Nothing
            }
        })

        (itemView.noteBody as TextInputEditText).addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if(((oldNote != null) && (s?.toString() != oldNote.body)) || oldNote == null)
                    thisNote.body = s?.toString() ?: ""
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Do Nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //Do Nothing
            }
        })

        itemView.starred.setOnClickListener {
            thisNote.isStarred = !thisNote.isStarred

            setStarred(thisNote)
        }

        val reminderCreator = ReminderCreator(context, arrayOf(thisNote.id))
        itemView.btnAddReminder.setOnClickListener { reminderCreator.createReminder() }
    }

    private fun setStarred(thisNote: Note) {
        itemView.starred.setImageResource(
                if(thisNote.isStarred)
                    R.drawable.ic_star_black_24dp
                else
                    R.drawable.ic_star_border_black_24dp
        )
    }
}