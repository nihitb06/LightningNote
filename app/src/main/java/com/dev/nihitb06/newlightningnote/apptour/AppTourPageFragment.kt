package com.dev.nihitb06.newlightningnote.apptour


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.dev.nihitb06.newlightningnote.R
import kotlinx.android.synthetic.main.fragment_app_tour_page.view.*

class AppTourPageFragment : Fragment() {

    private var position = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        position = arguments?.getInt(POSITION) ?: -1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val itemView = inflater.inflate(R.layout.fragment_app_tour_page, container, false)

        val (title, subtitle) = getPageText()

        if(position != 0) {
            itemView.ivHelperOne.visibility = View.GONE
            itemView.ivBackground.visibility = View.VISIBLE
        }
        if(position != 1) {
            itemView.ivIllustration.scaleX = 2f
            itemView.ivIllustration.scaleY = 2f
        }
        if(position == 1) {
            itemView.ivHelperTwo.visibility = View.VISIBLE
        } else if(position == 2) {
            itemView.ivIllustration.visibility = View.GONE
            itemView.ivBackground.setImageResource(R.drawable.drawable_multimedia)

            itemView.ivBackground.setBackgroundResource(R.drawable.drawable_oval_white)
        }

        itemView.tvPageTitle.text = title
        itemView.tvPageSubtitle.text = subtitle

        itemView.tag = position

        return itemView
    }

    private fun getPageText(): Pair<String, String> = when(position) {
        0 -> Pair(getString(R.string.page_one_title), getString(R.string.page_one_summary))
        1 -> Pair(getString(R.string.page_two_title), getString(R.string.page_two_summary))
        2 -> Pair(getString(R.string.page_three_title), getString(R.string.page_three_summary))
        else -> Pair("Error!!", "Resources not found")
    }

    companion object {
        private const val POSITION = "Postion"

        @JvmStatic
        fun newInstance(position: Int) = AppTourPageFragment().apply {
            arguments = Bundle().apply {
                putInt(POSITION, position)
            }
        }
    }
}
