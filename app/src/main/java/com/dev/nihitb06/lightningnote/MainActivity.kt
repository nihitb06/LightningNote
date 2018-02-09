package com.dev.nihitb06.lightningnote

import android.animation.Animator
import android.app.Fragment
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.view.MenuItem
import android.view.View
import android.view.animation.LinearInterpolator
import com.dev.nihitb06.lightningnote.appsettings.SettingsFragment
import com.dev.nihitb06.lightningnote.databaseutils.LightningNoteDatabase
import com.dev.nihitb06.lightningnote.notes.NotesFragment
import com.dev.nihitb06.lightningnote.notes.addnotes.AddNoteFragment
import com.dev.nihitb06.lightningnote.themeutils.ThemeActivity
import com.dev.nihitb06.lightningnote.utils.AnimationUtils
import com.dev.nihitb06.lightningnote.utils.MyActionBarDrawerToggle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : ThemeActivity(), NavigationView.OnNavigationItemSelectedListener, Animator.AnimatorListener {

    private var currentFragment = TAG_NOTES

    private var backButtonShowing = false
    private lateinit var drawerToggle: MyActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setNoActionBarTheme()
        setContentView(R.layout.activity_main)

        setupToolbarAndNavigation()

        onNavigationItemSelected(navBarContainer.menu.findItem(R.id.notes))

        fabAddNotes.setOnClickListener { switchFragment(AddNoteFragment(), TAG_ADD) }
    }

    private fun setupToolbarAndNavigation() {
        setSupportActionBar(toolbar)
        toolbar.popupTheme

        drawerToggle = MyActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        )

        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        drawerToggle.setToolbarNavigationClickListener { if (currentFragment == TAG_ADD) onBackPressed() }

        navBarContainer.setNavigationItemSelectedListener(this)
    }

    private fun switchFragment(fragment: Fragment, fragmentTag: String) {
        fragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment, fragmentTag).commit()

        if(fragmentTag == TAG_NOTES || fragmentTag == TAG_STAR)
            Handler().postDelayed({ fabAddNotes.visibility = View.VISIBLE }, 10)
        else
            fabAddNotes.visibility = View.INVISIBLE

        if(fragmentTag == TAG_ADD)
            manageBackButton(true)
        else if(backButtonShowing)
            manageBackButton(false)

        currentFragment = fragmentTag
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.notes -> switchFragment(NotesFragment.newInstance(this, false), TAG_NOTES)

            R.id.starred -> switchFragment(NotesFragment.newInstance(this, true), TAG_STAR)

            R.id.settings -> switchFragment(SettingsFragment.newInstance(), TAG_SETTINGS)

            else -> return false
        }

        setCheck(item)
        closeDrawer()
        return true
    }

    private fun setCheck(item: MenuItem) {
        Handler().post({
            for(index in 0..(navBarContainer.menu.size()-1)) {
                navBarContainer.menu.getItem(index).isChecked = false
            }

            item.isChecked = true
        })
    }

    private fun closeDrawer() { drawerLayout.closeDrawer(GravityCompat.START) }

    private fun saveNote() {
        Thread {
            val newNote = AddNoteFragment.returnNote()
            if(newNote.title != "" || newNote.body != "")
                LightningNoteDatabase.getDatabaseInstance(this).noteDao().insertNote(newNote)
        }.start()
        currentFragment = TAG_STAR
        onBackPressed()
    }

    override fun onBackPressed() {
        when {
            drawerLayout.isDrawerOpen(GravityCompat.START) -> closeDrawer()

            currentFragment == TAG_ADD -> saveNote()

            currentFragment != TAG_NOTES -> onNavigationItemSelected(navBarContainer.menu.findItem(R.id.notes))

            else -> super.onBackPressed()
        }
    }

    private fun manageBackButton(enable: Boolean) {
        var start = 0f
        var end = 1f
        if(!enable) {
            start = 1f
            end = 0f

            backButtonFunctionality(enable)
        }

        AnimationUtils.hamburgerToBackArrow(drawerToggle, drawerLayout, start, end, this)
    }

    private fun backButtonFunctionality(enable: Boolean) {
        if(enable) {
            drawerToggle.isDrawerIndicatorEnabled = false

            supportActionBar?.setDisplayShowHomeEnabled(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        } else {
            supportActionBar?.setDisplayShowHomeEnabled(false)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)

            drawerToggle.isDrawerIndicatorEnabled = true
        }

    }

    override fun onAnimationRepeat(animation: Animator?) {
        //Do Nothing
    }

    override fun onAnimationEnd(animation: Animator?) {
        if(!backButtonShowing)
            backButtonFunctionality(!backButtonShowing)
        backButtonShowing = !backButtonShowing
    }

    override fun onAnimationCancel(animation: Animator?) {
        //Do Nothing
    }

    override fun onAnimationStart(animation: Animator?) {
        //Do Nothing
    }

    companion object {
        private const val TAG_NOTES = "NotesFragment"
        private const val TAG_ADD = "AddNotesFragment"
        private const val TAG_STAR = "StarredNotesFragment"
        private const val TAG_SETTINGS = "SettingsFragment"
    }
}
