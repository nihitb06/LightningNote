package com.dev.nihitb06.newlightningnote

import android.animation.Animator
import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.preference.PreferenceManager
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.view.MenuItem
import android.view.View
import com.dev.nihitb06.newlightningnote.appsettings.SettingsFragment
import com.dev.nihitb06.newlightningnote.databaseutils.LightningNoteDatabase
import com.dev.nihitb06.newlightningnote.databaseutils.entities.Attachment
import com.dev.nihitb06.newlightningnote.databaseutils.entities.Note
import com.dev.nihitb06.newlightningnote.notes.AttachmentRecyclerAdapter
import com.dev.nihitb06.newlightningnote.notes.NotesFragment
import com.dev.nihitb06.newlightningnote.notes.operations.AddNoteFragment
import com.dev.nihitb06.newlightningnote.utils.AttachmentUriManager
import com.dev.nihitb06.newlightningnote.utils.OnNoteClickListener
import com.dev.nihitb06.newlightningnote.notes.operations.ShowNoteFragment
import com.dev.nihitb06.newlightningnote.reminders.show.RemindersFragment
import com.dev.nihitb06.newlightningnote.services.FloatingWidgetService
import com.dev.nihitb06.newlightningnote.services.ShakeToNoteService
import com.dev.nihitb06.newlightningnote.themeutils.ThemeActivity
import com.dev.nihitb06.newlightningnote.utils.AnimationUtils
import com.dev.nihitb06.newlightningnote.utils.ImageUtils
import com.dev.nihitb06.newlightningnote.utils.MyActionBarDrawerToggle
import com.dev.nihitb06.newlightningnote.utils.PermissionManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add_note.view.*

class MainActivity : ThemeActivity(), NavigationView.OnNavigationItemSelectedListener, Animator.AnimatorListener {

    private var currentFragment = TAG_NOTES

    private var backButtonShowing = false
    private lateinit var drawerToggle: MyActionBarDrawerToggle

    private val onFABClickListener = View.OnClickListener {
        Handler().postDelayed({ switchFragment(AddNoteFragment(), TAG_ADD) }, 200)
    }
    private val onNoteClickListener = OnNoteClickListener { noteId: Long? ->
        Handler().postDelayed({ switchFragment(ShowNoteFragment.newInstance(this@MainActivity, noteId), TAG_SHOW) }, 200)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setNoActionBarTheme()
        setContentView(R.layout.activity_main)

        setupToolbarAndNavigation()

        onNavigationItemSelected(navBarContainer.menu.findItem(R.id.notes))

        if(PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean(getString(R.string.key_shake_to_note), true))
            startService(Intent(this, ShakeToNoteService::class.java))
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val addNote = intent?.getBooleanExtra(ADD_NOTE_BOOLEAN, false) ?: false
        val openNoteId = intent?.getLongExtra(OPEN_NOTE_ID, -1L) ?: 0L

        when {
            addNote -> onFABClickListener.onClick(null)
            openNoteId != -1L -> onNoteClickListener.onNoteClick(openNoteId)
            else -> onNavigationItemSelected(navBarContainer.menu.findItem(R.id.notes))
        }
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

        drawerToggle.setToolbarNavigationClickListener {
            if (currentFragment == TAG_ADD || currentFragment == TAG_SHOW)
                onBackPressed()
        }

        navBarContainer.setNavigationItemSelectedListener(this)
    }

    private fun switchFragment(fragment: Fragment, fragmentTag: String) {
        fragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment, fragmentTag).commit()

        if(fragmentTag == TAG_ADD || fragmentTag == TAG_SHOW)
            manageBackButton(true)
        else if(backButtonShowing)
            manageBackButton(false)

        currentFragment = fragmentTag
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.notes -> switchFragment(NotesFragment.newInstance(this, false, false, onFABClickListener, onNoteClickListener), TAG_NOTES)

            R.id.reminders -> switchFragment(RemindersFragment(), TAG_REMINDER)

            R.id.starred -> switchFragment(NotesFragment.newInstance(this, true, false, onFABClickListener, onNoteClickListener), TAG_STAR)

            R.id.trash -> switchFragment(NotesFragment.newInstance(this, false, true, onFABClickListener, onNoteClickListener), TAG_TRASH)

            R.id.settings -> switchFragment(SettingsFragment.newInstance(), TAG_SETTINGS)

            else -> return false
        }

        setCheck(item)
        closeDrawer()
        return true
    }

    private fun setCheck(item: MenuItem) {
        Handler().post {
            for(index in 0..(navBarContainer.menu.size()-1))
                navBarContainer.menu.getItem(index).isChecked = false

            item.isChecked = true
        }
    }

    private fun closeDrawer() { drawerLayout.closeDrawer(GravityCompat.START) }

    private fun saveNote(newNote: Note, fromAttachment: Boolean) {
        if(newNote.id != 0L) {
            updateNote(newNote, fromAttachment)
            return
        }

        Thread {
            val lightningNoteDatabase = LightningNoteDatabase.getDatabaseInstance(this)
            if(fromAttachment) newNote.hasAttachment = true
            lightningNoteDatabase.noteDao().insertNote(newNote)
        }.start()
        if(!fromAttachment) {
            currentFragment = TAG_STAR
            onBackPressed()
        }
    }
    private fun updateNote(note: Note, fromAttachment: Boolean) {
        Thread {
            if(fromAttachment) note.hasAttachment = true
            note.dateUpdated = System.currentTimeMillis()
            LightningNoteDatabase.getDatabaseInstance(this).noteDao().updateNote(note)
        }.start()
        if(!fromAttachment) {
            currentFragment = TAG_STAR
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        when {
            drawerLayout.isDrawerOpen(GravityCompat.START) -> closeDrawer()

            currentFragment == TAG_ADD -> when {
                returningFromAttachment -> updateNote(AddNoteFragment.returnNote(), false)
                AddNoteFragment.isNoteEmpty() -> saveNote(AddNoteFragment.returnNote(), false)
                else -> {
                    currentFragment = TAG_STAR
                    onBackPressed()
                }
            }

            currentFragment == TAG_SHOW -> if(ShowNoteFragment.isNoteChanged()) updateNote(ShowNoteFragment.returnNote(), false) else {
                currentFragment = TAG_STAR
                onBackPressed()
            }

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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        PermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == SettingsFragment.REQUEST_CODE_DRAW_OVERLAY) {
            if(resultCode == PackageManager.PERMISSION_GRANTED) {
                startService(Intent(this@MainActivity, FloatingWidgetService::class.java))
            } else {
                Snackbar.make(fragmentContainer, "Permission is Required", Snackbar.LENGTH_SHORT).show()
            }
        } else {
            Thread {
                val lightningNoteDatabase = LightningNoteDatabase.getDatabaseInstance(this)
                var noteId = when {
                    currentFragment == TAG_ADD && !returningFromAttachment -> {
                        lightningNoteDatabase.noteDao().insertNote(AddNoteFragment.returnNote())
                    }
                    currentFragment == TAG_ADD -> {
                        AddNoteFragment.returnNote().id
                    }
                    else -> {
                        ShowNoteFragment.returnNote().id
                    }
                }
                if(noteId == 0L)
                    noteId = lightningNoteDatabase.noteDao().insertNote(AddNoteFragment.returnNote())

                val start = System.currentTimeMillis()
                while (System.currentTimeMillis() < start+500);

                val attachmentUriManager = AttachmentUriManager(this, noteId)
                if(resultCode == Activity.RESULT_OK) {
                    if(currentFragment == TAG_ADD) {
                        if(AddNoteFragment.returnNote().id == 0L)
                            try {
                                AddNoteFragment.setNote(lightningNoteDatabase.noteDao().getNoteById(noteId))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        returningFromAttachment = true
                    }

                    val adapter = try {
                        (fragmentManager
                                .findFragmentByTag(currentFragment)
                                .view
                                .rvAttachmentsList
                                .adapter as AttachmentRecyclerAdapter)
                    } catch (e: Exception) {
                        null
                    }

                    when(requestCode) {
                        AttachmentUriManager.REQUEST_TAKE_IMAGE -> {
                            if(capturePhotoFilePath != "") {
                                ImageUtils.compressImageFile(Uri.parse(capturePhotoFilePath).path)

                                lightningNoteDatabase.attachmentDao().createAttachment(
                                        Attachment(capturePhotoFilePath, noteId, Attachment.IMAGE)
                                )

                                adapter?.notifyAttachments(true)

                                runOnUiThread {
                                    updateNote()
                                }
                            }
                        }
                        AttachmentUriManager.REQUEST_SELECT_IMAGE -> {
                            attachmentUriManager.copyFileToStorage(Environment.DIRECTORY_PICTURES, data?.data, noteId, adapter)
                        }
                        AttachmentUriManager.REQUEST_SELECT_VIDEO, AttachmentUriManager.REQUEST_TAKE_VIDEO -> {
                            attachmentUriManager.copyFileToStorage(Environment.DIRECTORY_MOVIES, data?.data, noteId, adapter)
                        }
                        AttachmentUriManager.REQUEST_SELECT_AUDIO -> {
                            attachmentUriManager.copyFileToStorage(Environment.DIRECTORY_MUSIC, data?.data, noteId, adapter)
                        }

                        else -> return@Thread
                    }
                }
            }.start()
        }
    }

    fun updateNote() {
        updateNote(if(currentFragment == TAG_ADD) AddNoteFragment.returnNote() else ShowNoteFragment.returnNote(), true)
    }

    companion object {
        private const val TAG_NOTES = "NotesFragment"
        private const val TAG_ADD = "AddNotesFragment"
        private const val TAG_REMINDER = "RemindersFragment"
        private const val TAG_STAR = "StarredNotesFragment"
        private const val TAG_TRASH = "TrashNotesFragment"
        private const val TAG_SETTINGS = "SettingsFragment"
        private const val TAG_SHOW = "ShowNotesFragment"

        const val OPEN_NOTE_ID = "OpenNoteId"
        const val ADD_NOTE_BOOLEAN = "AddNote"

        var capturePhotoFilePath = ""

        private var returningFromAttachment = false
        fun setReturningFromAttachment(returningFromAttachment: Boolean) {
            this.returningFromAttachment = returningFromAttachment
        }
        fun getReturningFromAttachment() = returningFromAttachment
    }
}
