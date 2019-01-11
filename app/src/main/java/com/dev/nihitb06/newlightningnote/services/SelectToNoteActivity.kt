package com.dev.nihitb06.newlightningnote.services

import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.dev.nihitb06.newlightningnote.databaseutils.LightningNoteDatabase
import com.dev.nihitb06.newlightningnote.databaseutils.entities.Note

class SelectToNoteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_NoDisplay)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val text = intent?.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)

            text?.let {
                Thread {
                    LightningNoteDatabase.getDatabaseInstance(this).noteDao().insertNote(Note(
                            if(text.contains('.')) text.split('.')[0] else text.split(' ')[0],
                            text.toString()
                    ))
                }.start()

                Toast.makeText(this, "Note Added", Toast.LENGTH_SHORT).show()
            }
        }

        finish()
    }
}
