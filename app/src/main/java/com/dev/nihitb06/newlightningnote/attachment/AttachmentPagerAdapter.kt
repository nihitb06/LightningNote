package com.dev.nihitb06.newlightningnote.attachment

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.ViewGroup

class AttachmentPagerAdapter (private val attachments: Array<AttachmentParcelable>?, fragmentManager: FragmentManager)
    : FragmentStatePagerAdapter (fragmentManager) {

    private var currentFragment: Fragment? = null

    override fun getItem(position: Int)
            = AttachmentFragment.newInstance(attachments?.get(position)?.uri ?: "",  position, attachments!![position].type)

    override fun getCount() = attachments?.size ?: 0

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        if(currentFragment != `object`) {
            currentFragment?.onPause()
            currentFragment = `object` as Fragment
            currentFragment?.onResume()
        }
        super.setPrimaryItem(container, position, `object`)
    }
}