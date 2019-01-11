package com.dev.nihitb06.newlightningnote.utils

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.CardView
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import kotlinx.android.synthetic.main.layout_note.view.*

class AnimationUtils {

    companion object {
        const val ELEVATION_START = 0f
        const val ELEVATION_END = 24f

        fun hamburgerToBackArrow(
                drawerToggle: MyActionBarDrawerToggle,
                mainDrawerLayout: DrawerLayout,
                start: Float,
                end: Float,
                animatorListener: Animator.AnimatorListener?
        ) {
            val anim = ValueAnimator.ofFloat(start, end)

            anim.addUpdateListener { animation: ValueAnimator? ->
                try {
                    drawerToggle.onDrawerAnimate(mainDrawerLayout, animation!!.animatedValue.toString().toFloat())
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
            }

            anim.addListener(animatorListener)

            anim.interpolator = AccelerateDecelerateInterpolator()
            anim.duration = 300
            anim.start()
        }

        fun setSelected(view: View, start: Float, end: Float) = ObjectAnimator.ofFloat(
                view.root as CardView,
                "cardElevation",
                start,
                end
        ).start()
    }
}