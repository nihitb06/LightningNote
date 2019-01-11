package com.dev.nihitb06.newlightningnote.notes.operations


import android.os.Bundle
import android.app.Fragment
import android.content.Context
import android.view.*
import com.dev.nihitb06.newlightningnote.R
import com.dev.nihitb06.newlightningnote.databaseutils.LightningNoteDatabase
import com.dev.nihitb06.newlightningnote.databaseutils.entities.Note
import com.dev.nihitb06.newlightningnote.notes.noteutils.AddShowFunctionality
import com.dev.nihitb06.newlightningnote.notes.noteutils.NoteLongClickListenerSingleSelection
import com.dev.nihitb06.newlightningnote.themeutils.ThemeActivity
import kotlinx.android.synthetic.main.fragment_add_note.view.*

class ShowNoteFragment : Fragment() {

    private var noteId: Long? = null
    private var noteLongClickListenerSingleSelection: NoteLongClickListenerSingleSelection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val itemView = inflater.inflate(R.layout.fragment_add_note, container, false)

        itemView.noteTitle.setText(thisNote.title)
        itemView.noteBody.setText(thisNote.body)

        val addShowFunctionality = AddShowFunctionality(activity, itemView, (activity as ThemeActivity).isThemeDark())
        addShowFunctionality.setupView(thisNote, oldNote)

        return itemView
    }

    override fun onStart() {
        super.onStart()

        noteLongClickListenerSingleSelection = NoteLongClickListenerSingleSelection(
                activity,
                thisNote,
                activity.findViewById(R.id.fragmentContainer),
                ArrayList(),
                false
        ) { activity.recreate() }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater?.inflate(R.menu.menu_note, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return noteLongClickListenerSingleSelection?.processMenuItem(item) ?: false
    }

    companion object {
        fun newInstance(context: Context, noteId: Long?): ShowNoteFragment {
            val fragment = ShowNoteFragment()
            fragment.noteId = noteId

            Thread {
                thisNote = LightningNoteDatabase.getDatabaseInstance(context).noteDao().getNoteById(noteId!!)
                oldNote = thisNote.copy()
            }.start()

            return fragment
        }

        private lateinit var thisNote: Note
        private lateinit var oldNote: Note

        fun returnNote() = thisNote
        fun isNoteChanged(): Boolean {
            return (thisNote.title != oldNote.title || thisNote.body != oldNote.body || thisNote.isStarred != oldNote.isStarred)
        }
    }
}
