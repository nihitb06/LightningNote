package com.dev.nihitb06.newlightningnote.notes.operations

import android.os.Bundle
import android.app.Fragment
import android.view.*

import com.dev.nihitb06.newlightningnote.R
import com.dev.nihitb06.newlightningnote.databaseutils.entities.Note
import com.dev.nihitb06.newlightningnote.notes.noteutils.AddShowFunctionality
import com.dev.nihitb06.newlightningnote.themeutils.ThemeActivity

class AddNoteFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        thisNote = Note("", "")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val itemView = inflater.inflate(R.layout.fragment_add_note, container, false)

        val addShowFunctionality = AddShowFunctionality(activity, itemView, (activity as ThemeActivity).isThemeDark())
        addShowFunctionality.setupView(thisNote, null)

        return itemView
    }

    companion object {
        private var thisNote = Note("", "")

        fun returnNote() = thisNote
        fun setNote(note: Note) {
            thisNote = note
        }
        fun isNoteEmpty(): Boolean = thisNote.title != "" || thisNote.body != "" || thisNote.hasAttachment
    }
}
