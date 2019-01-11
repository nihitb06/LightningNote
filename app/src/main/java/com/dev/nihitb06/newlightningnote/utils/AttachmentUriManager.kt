package com.dev.nihitb06.newlightningnote.utils

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.NotificationCompat
import android.support.v4.content.FileProvider
import android.widget.Toast
import com.dev.nihitb06.newlightningnote.MainActivity
import com.dev.nihitb06.newlightningnote.R
import com.dev.nihitb06.newlightningnote.databaseutils.LightningNoteDatabase
import com.dev.nihitb06.newlightningnote.databaseutils.entities.Attachment
import com.dev.nihitb06.newlightningnote.notes.AttachmentRecyclerAdapter
import com.dev.nihitb06.newlightningnote.notes.operations.AddNoteFragment
import com.dev.nihitb06.newlightningnote.reminders.ReminderNotificationService
import com.google.android.gms.location.LocationServices
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AttachmentUriManager (private val context: Context, private val noteId: Long) {

    fun createIntent(index: Int, adapter: AttachmentRecyclerAdapter?, isNoteBeingAdded: Boolean) {
        var intent: Intent? = null
        var requestCode = 0
        when(index) {
            0 -> {
                intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                requestCode = REQUEST_TAKE_IMAGE

                if(intent.resolveActivity(context.packageManager) != null) {
                    val photoFile = ImageUtils.createImageFile(context)
                    photoFile?.let {
                        val uri = FileProvider.getUriForFile(
                                context,
                                "com.dev.nihitb06.lightningnote.FileProvider",
                                photoFile
                        )
                        MainActivity.capturePhotoFilePath = Uri.fromFile(photoFile).toString()

                        intent?.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    }
                }
            }
            1 -> {
                intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                intent.putExtra(android.provider.MediaStore.EXTRA_VIDEO_QUALITY, 0)
                requestCode = REQUEST_TAKE_VIDEO
            }
            2 -> {
                AudioUtils.recordAudio(context, noteId, adapter, isNoteBeingAdded)
                (context as MainActivity).updateNote()
                return
            }
            3 -> {
                intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                requestCode = REQUEST_SELECT_IMAGE
            }
            4 -> {
                intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                requestCode = REQUEST_SELECT_VIDEO
            }
            5 -> {
                intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                requestCode = REQUEST_SELECT_AUDIO
            }
            6 -> {
                if(context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED)
                    LocationServices.getFusedLocationProviderClient(context)
                            .lastLocation.addOnSuccessListener { location: Location? ->
                        location?.run {
                            val locationUri = location.latitude.toString() + ":" + location.longitude.toString()
                            Thread {
                                val lightningNoteDatabase = LightningNoteDatabase.getDatabaseInstance(context)
                                var id = noteId
                                if(isNoteBeingAdded && !MainActivity.getReturningFromAttachment()) {
                                    id = lightningNoteDatabase.noteDao().insertNote(AddNoteFragment.returnNote())
                                    AddNoteFragment.setNote(lightningNoteDatabase.noteDao().getNoteById(id))
                                    MainActivity.setReturningFromAttachment(true)
                                }

                                lightningNoteDatabase.attachmentDao().createAttachment(
                                        Attachment(locationUri, id, Attachment.GEO_LOCATION)
                                )

                                (context as Activity).runOnUiThread {
                                    Toast.makeText(context, "Location Stored!!", Toast.LENGTH_SHORT).show()

                                    (context as MainActivity).updateNote()
                                    adapter?.notifyAttachments(true)
                                }
                            }.start()
                        } ?: Toast.makeText(context, "Kindly Turn on your GPS", Toast.LENGTH_SHORT).show()
                    }

                (context as MainActivity).updateNote()
                return
            }
            else -> {}
        }

        (context as Activity).startActivityForResult(intent, requestCode)
    }

    fun copyFileToStorage(fileDirectory: String, uri: Uri?, noteId: Long, adapter: AttachmentRecyclerAdapter?) {
        val fileDir = context.getExternalFilesDir(fileDirectory)
        if(fileDir.isDirectory) {
            val fileExtension = getFileExtension(uri)

            val fileName = "Attachment_" + SimpleDateFormat.getDateTimeInstance().format(Date()) + fileExtension

            try {
                var notificationManager: NotificationManager? = null
                if(fileDirectory == Environment.DIRECTORY_MOVIES)
                    notificationManager = createProgressNotification()

                val newFile = File(fileDir, fileName)
                getTempFile(uri, fileExtension)?.copyTo(newFile, false, DEFAULT_BUFFER_SIZE)

                Thread({
                    if(fileDirectory == Environment.DIRECTORY_PICTURES)
                        ImageUtils.compressImageFile(newFile.path)

                    val lightningNoteDatabase = LightningNoteDatabase.getDatabaseInstance(context)
                    lightningNoteDatabase.attachmentDao().createAttachment(Attachment(
                            Uri.fromFile(newFile).toString(),
                            noteId,
                            when(fileDirectory) {
                                Environment.DIRECTORY_PICTURES -> Attachment.IMAGE
                                Environment.DIRECTORY_MOVIES -> Attachment.VIDEO
                                Environment.DIRECTORY_MUSIC -> Attachment.AUDIO
                                else -> Attachment.OTHER
                            }
                    ))

                    val start = System.currentTimeMillis()
                    while (System.currentTimeMillis() < start + 500);

                    (context as MainActivity).updateNote()

                    notificationManager?.cancel(NOTIFICATION)
                    adapter?.notifyAttachments(true)
                }, "Save").start()
            } catch (e: NoSuchFileException) {
                e.printStackTrace()
            } catch (e: FileAlreadyExistsException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun getTempFile(uri: Uri?, extension: String): File? {
        val newFile = File.createTempFile("NewFile", extension)
        newFile.deleteOnExit()

        val inputStream = context.contentResolver.openInputStream(uri)

        if(inputStream != null) {
            try {
                val outputStream = FileOutputStream(newFile)

                inputStream.use { it.copyTo(outputStream) }

                inputStream.close()
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return newFile
    }

    private fun getFileExtension(uri: Uri?): String {
        var fileExtension = ""

        val filePathColumn: Array<out String> = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, filePathColumn, null, null, null)

        if(cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val filePath = cursor.getString(columnIndex)

            fileExtension = filePath.substring(filePath.lastIndexOf("."))
        }

        cursor.close()
        return fileExtension
    }

    private fun createProgressNotification(): NotificationManager {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(NOTIFICATION, NotificationCompat.Builder(context, ReminderNotificationService.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle("Storing Video")
                .setContentText("Your Video is being saved")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(100, 0, true)
                .build())

        return notificationManager
    }

    companion object {
        const val REQUEST_TAKE_IMAGE = 101
        const val REQUEST_TAKE_VIDEO = 102
        const val REQUEST_SELECT_IMAGE = 103
        const val REQUEST_SELECT_VIDEO = 104
        const val REQUEST_SELECT_AUDIO = 105

        private const val NOTIFICATION = 400
    }
}