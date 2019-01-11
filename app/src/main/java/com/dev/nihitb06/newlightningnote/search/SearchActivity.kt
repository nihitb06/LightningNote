package com.dev.nihitb06.newlightningnote.search

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.support.v7.widget.LinearLayoutManager
import com.dev.nihitb06.newlightningnote.R
import com.dev.nihitb06.newlightningnote.databaseutils.LightningNoteDatabase
import com.dev.nihitb06.newlightningnote.search.RecentSuggestionProvider.Companion.AUTHORITY
import com.dev.nihitb06.newlightningnote.search.RecentSuggestionProvider.Companion.MODE
import com.dev.nihitb06.newlightningnote.themeutils.ThemeActivity
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : ThemeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setNoActionBarTheme()
        setContentView(R.layout.activity_search)

        setToolbar()

        if(intent.action == Intent.ACTION_SEARCH) {
            rvSearchResultsList.layoutManager = LinearLayoutManager(this)
            try {
                Thread {
                    val query = intent.getStringExtra(SearchManager.QUERY)
                    rvSearchResultsList.adapter = SearchResultsRecyclerAdapter(
                            this,
                            LightningNoteDatabase.getDatabaseInstance(this).noteDao().search("%"+query+"%"),
                            listEmpty
                    )

                    SearchRecentSuggestions(this, AUTHORITY, MODE).saveRecentQuery(query, null)
                }.start()
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
        }
    }

    private fun setToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }
}
