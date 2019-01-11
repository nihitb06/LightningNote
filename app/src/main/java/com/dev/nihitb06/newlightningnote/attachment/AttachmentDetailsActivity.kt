package com.dev.nihitb06.newlightningnote.attachment

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.dev.nihitb06.newlightningnote.R
import kotlinx.android.synthetic.main.activity_attachment_details.*

class AttachmentDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attachment_details)

        setToolbar()

        try {
            val parcelablesArray = intent!!.getParcelableArrayExtra(URIS)
            val attachmentParcelables = Array(parcelablesArray?.size ?: 0) { parcelablesArray?.get(it) as AttachmentParcelable }

            attachmentpager.adapter = AttachmentPagerAdapter(attachmentParcelables, supportFragmentManager)
            attachmentpager.currentItem = intent?.getIntExtra(POSITION, 0) ?: 0
            attachmentpager.setPageTransformer(true, DepthPagerTransformer())
        } catch (e: ClassCastException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setToolbar() {
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    companion object {
        const val POSITION = "Position"
        const val URIS = "Uris"
    }
}
