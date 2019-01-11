package com.dev.nihitb06.newlightningnote.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager

import com.dev.nihitb06.newlightningnote.R
import com.dev.nihitb06.newlightningnote.databaseutils.LightningNoteDatabase
import com.dev.nihitb06.newlightningnote.themeutils.ThemeActivity
import com.dev.nihitb06.newlightningnote.utils.OnNoteClickListener
import kotlinx.android.synthetic.main.view_note_widget_configure.*

/**
 * The configuration screen for the [ViewNoteWidget] AppWidget.
 */
class ViewNoteWidgetConfigureActivity : ThemeActivity() {

    private var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(Activity.RESULT_CANCELED)

        setContentView(R.layout.view_note_widget_configure)

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        //Setup Remote View by populating items
        rvWidgetList.layoutManager = LinearLayoutManager(this)
        Thread {
            val notesList = LightningNoteDatabase.getDatabaseInstance(this).noteDao().getUnDeletedNotes()
            rvWidgetList.adapter = WidgetViewRecyclerAdapter(notesList, OnNoteClickListener {
                noteId: Long ->

                val context = this@ViewNoteWidgetConfigureActivity

                // When the button is clicked, store the NoteId locally
                saveTitlePref(context, mAppWidgetId, noteId)

                // It is the responsibility of the configuration activity to update the app widget
                val appWidgetManager = AppWidgetManager.getInstance(context)
                ViewNoteWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId)

                // Make sure we pass back the original appWidgetId
                val resultValue = Intent()
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
                setResult(Activity.RESULT_OK, resultValue)
                finish()
            })
        }.start()
    }

    companion object {

        private const val PREFS_NAME = "com.dev.nihitb06.lightningnote.widget.ViewNoteWidget"
        private const val PREF_PREFIX_KEY = "appwidget_"

        // Write the prefix to the SharedPreferences object for this widget
        internal fun saveTitlePref(context: Context, appWidgetId: Int, noteId: Long) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.putLong(PREF_PREFIX_KEY + appWidgetId, noteId)
            prefs.apply()
        }

        // Read the prefix from the SharedPreferences object for this widget.
        // If there is no preference saved, get the default from a resource
        internal fun loadTitlePref(context: Context, appWidgetId: Int)
                = context.getSharedPreferences(PREFS_NAME, 0).getLong(PREF_PREFIX_KEY + appWidgetId, -1L)

        internal fun deleteTitlePref(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.remove(PREF_PREFIX_KEY + appWidgetId)
            prefs.apply()
        }
    }
}

