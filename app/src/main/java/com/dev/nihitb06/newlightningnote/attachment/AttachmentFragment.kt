package com.dev.nihitb06.newlightningnote.attachment

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import android.widget.Toast

import com.dev.nihitb06.newlightningnote.R
import com.dev.nihitb06.newlightningnote.attachment.AttachmentDetailsActivity.Companion.POSITION
import com.dev.nihitb06.newlightningnote.databaseutils.entities.Attachment
import com.dev.nihitb06.newlightningnote.utils.ImageUtils
import kotlinx.android.synthetic.main.fragment_attachment.view.*
import java.io.File
import java.io.IOException

class AttachmentFragment : Fragment() {

    private var uri: String? = null
    private var position = 0
    private var type: Int? = null
    private var itemView: View? = null

    private var isVideoPlaybackActive = false
    private var videoPlaybackTime = 0
    private lateinit var controller: MediaController

    private var mediaPlayer: MediaPlayer? = null
    private var audioPlaybackTime = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        videoPlaybackTime = savedInstanceState?.getInt(VIDEO_PLAYBACK_TIME, 0) ?: 0
        audioPlaybackTime = savedInstanceState?.getInt(AUDIO_PLAYBACK_TIME, 0) ?: 0

        arguments?.let {
            uri = arguments?.getString(URI)
            position = arguments?.getInt(POSITION) ?: 0
            type = arguments?.getInt(TYPE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        itemView = inflater.inflate(R.layout.fragment_attachment, container, false)
        setAttachment()
        return itemView
    }

    override fun onResume() {
        super.onResume()
        resumeVideoPlayback()
        mediaPlayer?.start()
    }

    override fun onPause() {
        pauseVideoPlayback()
        mediaPlayer?.pause()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(VIDEO_PLAYBACK_TIME, itemView?.videoPlayer?.currentPosition ?: 0)
        outState.putInt(AUDIO_PLAYBACK_TIME, mediaPlayer?.currentPosition ?: 0)
    }

    override fun onStop() {
        stopVideoPlayback()
        stopAudioPlayback()

        super.onStop()
    }

    private fun setAttachment() {
        when(type) {
            Attachment.IMAGE -> {
                Thread {
                    ImageUtils.setImage(context!!, itemView?.typePhoto, Uri.parse(uri).path, true)
                }.start()
            }
            Attachment.VIDEO ->  {
                Thread {
                    val bitmap = ThumbnailUtils.createVideoThumbnail(Uri.parse(uri).path, MediaStore.Images.Thumbnails.MINI_KIND)

                    try {
                        (context as Activity).runOnUiThread {
                            itemView?.typeNonPhoto?.setImageBitmap(bitmap)
                            itemView?.typeAction?.setImageResource(R.drawable.ic_play_circle_filled_black_24dp)
                        }
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    } catch (e: TypeCastException) {
                        e.printStackTrace()
                    }
                }.start()

                initializeVideoPlayer()

                itemView?.typeAction?.setOnClickListener {
                    itemView?.videoPlayer?.visibility = View.VISIBLE
                    itemView?.typeAction?.visibility = View.GONE

                    itemView?.videoPlayer?.start()
                    isVideoPlaybackActive = true
                }
            }
            Attachment.AUDIO -> {
                itemView?.typeNonPhoto?.setImageResource(R.drawable.ic_audio_white_24dp)
                itemView?.typeAction?.setImageResource(R.drawable.ic_play_circle_filled_black_24dp)

                initializeAudioPlayer()

                itemView?.typeAction?.setOnClickListener { view: View? ->
                    try {
                        if(mediaPlayer?.isPlaying == true) {
                            (view as ImageView).setImageResource(R.drawable.ic_play_circle_filled_black_24dp)
                            mediaPlayer?.pause()
                        } else {
                            (view as ImageView).setImageResource(R.drawable.ic_pause_circle_filled_black_24dp)
                            mediaPlayer?.start()
                        }
                    } catch (e: ClassCastException) {
                        e.printStackTrace()
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    }
                }
            }
            Attachment.GEO_LOCATION -> {
                itemView?.typeNonPhoto?.setImageResource(R.drawable.ic_place_white_24dp)
                itemView?.typeAction?.setImageResource(R.drawable.ic_directions_black_24dp)

                itemView?.typeAction?.setOnClickListener { setNav() }
            }
            Attachment.OTHER -> {}
        }
    }

    //Function for controlling audio playback
    private fun initializeAudioPlayer() {
        try {
            val file = File(Uri.parse(uri).path)
            if(file.exists()) {
                file.setReadable(true)
                mediaPlayer = MediaPlayer.create(context, FileProvider.getUriForFile(context!!, "com.dev.nihitb06.lightningnote.FileProvider", file))
            } else {
                showErrorSnackBar()
            }

            mediaPlayer?.setOnCompletionListener { stopAudioPlayback() }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            showErrorToast()
        }

        if(audioPlaybackTime > 0) {
            mediaPlayer?.seekTo(audioPlaybackTime)
        }

    }
    private fun stopAudioPlayback() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    //Functions for controlling video playback
    private fun initializeVideoPlayer() {
        try {
            val file = File(Uri.parse(uri).path)
            if(file.exists()) {
                file.setReadable(true)
                itemView?.videoPlayer?.setVideoURI(FileProvider.getUriForFile(context!!, "com.dev.nihitb06.lightningnote.FileProvider", file))
            } else {
                showErrorSnackBar()
            }

            controller = MediaController(context)
            controller.setMediaPlayer(itemView?.videoPlayer)
            itemView?.videoPlayer?.setMediaController(controller)

            itemView?.videoPlayer?.setOnCompletionListener {
                stopVideoPlayback()
            }

            if(videoPlaybackTime > 0) {
                itemView?.videoPlayer?.seekTo(videoPlaybackTime)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            showErrorToast()
        }
    }
    private fun pauseVideoPlayback() {
        if(itemView?.videoPlayer?.isPlaying == true && isVideoPlaybackActive) {
            itemView?.videoPlayer?.pause()
            itemView?.videoPlayer?.visibility = View.INVISIBLE
            controller.hide()
        }
    }
    private fun resumeVideoPlayback() {
        if(itemView?.videoPlayer?.isPlaying == false && isVideoPlaybackActive) {
            itemView?.videoPlayer?.visibility = View.VISIBLE
            itemView?.videoPlayer?.resume()
            controller.show()
        }
    }
    private fun stopVideoPlayback() {
        if(isVideoPlaybackActive) {
            itemView?.videoPlayer?.stopPlayback()
            itemView?.videoPlayer?.seekTo(1)
            itemView?.typeAction?.visibility = View.VISIBLE
            itemView?.videoPlayer?.visibility = View.INVISIBLE
            isVideoPlaybackActive = false
        }
    }

    //Function for Launching Nav intent
    private fun setNav() {
        val tokens = uri?.split(':')
        context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:"+tokens?.get(0)+","+tokens?.get(1)+"?zoom=15")))
    }

    private fun showErrorSnackBar() {
        Snackbar.make(itemView ?: view!!, "The File seems to be invalid", Snackbar.LENGTH_SHORT).show()
    }
    private fun showErrorToast() {
        Toast.makeText(context, "Something Went Wrong", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val URI = "uri"
        private const val TYPE = "type"
        private const val VIDEO_PLAYBACK_TIME = "VideoPlaybackTime"
        private const val AUDIO_PLAYBACK_TIME = "AudioPlaybackTime"

        @JvmStatic
        fun newInstance(uri: String, position: Int, type: Int) = AttachmentFragment().apply {
            arguments = Bundle().apply {
                putString(URI, uri)
                putInt(POSITION, position)
                putInt(TYPE, type)
            }
        }
    }
}
