package com.dev.nihitb06.newlightningnote.widget

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dev.nihitb06.newlightningnote.R
import com.dev.nihitb06.newlightningnote.databaseutils.entities.Note
import com.dev.nihitb06.newlightningnote.utils.OnNoteClickListener
import kotlinx.android.synthetic.main.layout_note_widget.view.*

class WidgetViewRecyclerAdapter (
        private val notesList: List<Note>,
        private val onNoteClickListener: OnNoteClickListener
) : RecyclerView.Adapter<WidgetViewRecyclerAdapter.WidgetViewHolder> () {

    inner class WidgetViewHolder(private val thisView: View) : RecyclerView.ViewHolder (thisView) {

        internal fun bindView(note: Note) {
            thisView.tvNoteTitle.text = note.title
            thisView.tvNoteBody.text = note.body

            thisView.contentArea.setOnClickListener {
                onNoteClickListener.onNoteClick(note.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            = WidgetViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_note_widget, parent, false))

    override fun onBindViewHolder(holder: WidgetViewHolder, position: Int) {
        holder.bindView(notesList[position])
    }

    override fun getItemCount() = notesList.size
}