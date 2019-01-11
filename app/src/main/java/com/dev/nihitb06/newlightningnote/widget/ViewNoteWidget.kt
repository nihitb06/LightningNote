package com.dev.nihitb06.newlightningnote.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

import com.dev.nihitb06.newlightningnote.R
import com.dev.nihitb06.newlightningnote.databaseutils.LightningNoteDatabase

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [ViewNoteWidgetConfigureActivity]
 */
class ViewNoteWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            ViewNoteWidgetConfigureActivity.deleteTitlePref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int) {

            val noteId = ViewNoteWidgetConfigureActivity.loadTitlePref(context, appWidgetId)
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.view_note_widget)

            Thread {
                val note = if(noteId != -1L )
                    LightningNoteDatabase.getDatabaseInstance(context).noteDao().getNoteById(noteId)
                else
                    null

                try {
                    views.setTextViewText(R.id.tvNoteTitle, note?.title)
                    views.setTextViewText(R.id.tvNoteBody, note?.body)
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }

                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }.start()
        }
    }
}

