package com.dev.nihitb06.newlightningnote.apptour

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class AppTourPagerAdapter (fragmentManager: FragmentManager) : FragmentStatePagerAdapter (fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return AppTourPageFragment.newInstance(position)
    }

    override fun getCount() = 3
}