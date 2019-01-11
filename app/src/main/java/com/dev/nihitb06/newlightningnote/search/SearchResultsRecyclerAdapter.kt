package com.dev.nihitb06.newlightningnote.search

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dev.nihitb06.newlightningnote.MainActivity
import com.dev.nihitb06.newlightningnote.MainActivity.Companion.OPEN_NOTE_ID
import com.dev.nihitb06.newlightningnote.R
import com.dev.nihitb06.newlightningnote.databaseutils.extramodels.NoteText
import kotlinx.android.synthetic.main.layout_search_result.view.*

class SearchResultsRecyclerAdapter (private val context: Context, private var searchResults: List<NoteText>, private val listEmptyView: View)
    : RecyclerView.Adapter<SearchResultsRecyclerAdapter.SearchResultViewHolder> () {

    init {
        setListEmptyView()
    }

    private fun setListEmptyView() {
        if(itemCount == 0)
            listEmptyView.visibility = View.VISIBLE
        else
            listEmptyView.visibility = View.GONE
    }

    inner class SearchResultViewHolder (private val thisView: View) : RecyclerView.ViewHolder (thisView) {

        fun bindSearchResult(searchResult: NoteText) {
            thisView.tvNoteTitle.text = searchResult.title
            thisView.tvNoteBody.text = searchResult.body

            thisView.setOnClickListener {
                context.startActivity(Intent(context, MainActivity::class.java).putExtra(OPEN_NOTE_ID, searchResult.id))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            = SearchResultViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_search_result, parent, false))

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bindSearchResult(searchResults[position])
    }

    override fun getItemCount() = searchResults.size
}