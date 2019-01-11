package com.dev.nihitb06.newlightningnote.apptour

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.View
import com.dev.nihitb06.newlightningnote.MainActivity
import com.dev.nihitb06.newlightningnote.R
import kotlinx.android.synthetic.main.activity_app_tour.*

class AppTourActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_tour)

        viewPagerAppTour.adapter = AppTourPagerAdapter(supportFragmentManager)
        viewPagerAppTour.setPageTransformer(false, AppTourPagerTransformer(this, rootView))

        tabLayout.setupWithViewPager(viewPagerAppTour, true)

        viewPagerAppTour.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                if(position == 2) {
                    btnNext.setImageResource(R.drawable.ic_done_white_24dp)
                    btnNext.setOnClickListener { goToMain() }
                    btnSkip.visibility = View.GONE
                } else {
                    btnNext.setImageResource(R.drawable.ic_chevron_right_white_24dp)
                    btnNext.setOnClickListener {
                        viewPagerAppTour.currentItem = viewPagerAppTour.currentItem + 1
                    }
                    btnSkip.visibility = View.VISIBLE
                }
            }
        })

        btnSkip.setOnClickListener { goToMain() }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
