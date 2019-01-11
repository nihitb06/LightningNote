package com.dev.nihitb06.newlightningnote.notes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.dev.nihitb06.newlightningnote.R
import com.dev.nihitb06.newlightningnote.attachment.AttachmentDetailsActivity
import com.dev.nihitb06.newlightningnote.attachment.AttachmentDetailsActivity.Companion.POSITION
import com.dev.nihitb06.newlightningnote.attachment.AttachmentDetailsActivity.Companion.URIS
import com.dev.nihitb06.newlightningnote.attachment.AttachmentParcelable
import com.dev.nihitb06.newlightningnote.databaseutils.LightningNoteDatabase
import com.dev.nihitb06.newlightningnote.databaseutils.entities.Attachment
import com.dev.nihitb06.newlightningnote.utils.ImageUtils
import kotlinx.android.synthetic.main.layout_attachment.view.*
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class AttachmentRecyclerAdapter (
        private val context: Context,
        private val noteId: Long,
        private var hasAttachment: Boolean,
        private val view: View
) : RecyclerView.Adapter<AttachmentRecyclerAdapter.AttachmentViewHolder> () {

    private var attachments: List<Attachment>? = null
    private var uris: Array<AttachmentParcelable>? = null
    init {
        if(hasAttachment) {
            getAttachments(false)
        }
    }

    private fun getAttachments(shouldTryAgain: Boolean) {
        Thread {
            attachments = LightningNoteDatabase.getDatabaseInstance(context).attachmentDao().getNoteAttachments(noteId)

            if(attachments?.size != 0) {
                uris = Array(attachments?.size ?: 0) { AttachmentParcelable(attachments?.get(it)?.uri, attachments!![it].type) }
                (context as Activity).runOnUiThread {
                    notifyDataSetChanged()
                    setListVisible()
                }
            } else if(shouldTryAgain) {
                val start = System.currentTimeMillis()
                while (System.currentTimeMillis() < start+500);
                getAttachments(false)
            }
        }.start()
    }
    fun notifyAttachments(hasAttachment: Boolean) {
        this.hasAttachment = hasAttachment
        getAttachments(true)
    }
    private fun setListVisible() { view.visibility = View.VISIBLE }

    inner class AttachmentViewHolder (private val thisView: View) : RecyclerView.ViewHolder (thisView) {

        fun bindAttachment(attachment: Attachment?, position: Int) {
            thisView.attachmentAction.setImageDrawable(null)

            thisView.setOnLongClickListener {
                val popupMenu = PopupMenu(context, thisView)

                popupMenu.inflate(R.menu.menu_attachment)
                popupMenu.setOnMenuItemClickListener { item: MenuItem? ->
                    when(item?.itemId) {
                        R.id.attachmentDelete -> {
                            attachment?.let {
                                Thread {
                                    try {
                                        val file = File(Uri.parse(attachment.uri).path)
                                        if(file.exists()) {
                                            file.delete()
                                        }
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }

                                    LightningNoteDatabase.getDatabaseInstance(context).attachmentDao().deleteAttachment(attachment)
                                }.start()

                                try {
                                    (attachments as ArrayList).remove(attachment)
                                    notifyItemRemoved(position)
                                } catch (e: ClassCastException) {
                                    e.printStackTrace()
                                } catch (e: NullPointerException) {
                                    e.printStackTrace()
                                }
                            }

                            return@setOnMenuItemClickListener true
                        }
                        else -> return@setOnMenuItemClickListener false
                    }
                }

                popupMenu.show()

                return@setOnLongClickListener true
            }

            try {
                when (attachment?.type) {
                    Attachment.IMAGE -> {
                        Thread {
                            ImageUtils.setImage(context, thisView.attachment, Uri.parse(attachment.uri).path, false)
                        }.start()
                    }

                    Attachment.VIDEO -> {
                        Thread {
                            val bitmap = ThumbnailUtils.createVideoThumbnail(Uri.parse(attachment.uri).path, MediaStore.Images.Thumbnails.MINI_KIND)

                            (context as Activity).runOnUiThread {
                                thisView.attachment.setImageBitmap(bitmap)
                                thisView.attachmentAction.setImageResource(R.drawable.ic_play_circle_filled_black_24dp)
                            }
                        }.start()
                    }

                    Attachment.AUDIO -> {
                        thisView.attachment.setImageResource(R.drawable.ic_audio_white_24dp)
                        thisView.attachmentAction.setImageResource(R.drawable.ic_play_circle_filled_black_24dp)
                    }

                    Attachment.GEO_LOCATION -> {
                        thisView.attachment.setImageResource(R.drawable.ic_place_white_24dp)
                        thisView.attachmentAction.setImageResource(R.drawable.ic_directions_black_24dp)
                    }
                }

                thisView.setOnClickListener {
                    context.startActivity(
                            Intent(context, AttachmentDetailsActivity::class.java)
                                    .putExtra(POSITION, position)
                                    .putExtra(URIS, uris)
                    )
                }
            } catch (e: FileNotFoundException) {
                Snackbar.make(view, "One or more Attachments seem to be missing", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            = AttachmentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_attachment, parent, false))

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        holder.bindAttachment(attachments?.get(position), position)
    }

    override fun getItemCount() = attachments?.size ?: 0
}