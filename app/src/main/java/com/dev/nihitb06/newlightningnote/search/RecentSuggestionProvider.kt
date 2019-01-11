package com.dev.nihitb06.newlightningnote.search

import android.content.SearchRecentSuggestionsProvider

class RecentSuggestionProvider : SearchRecentSuggestionsProvider () {

    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        const val AUTHORITY = "com.dev.nihitb06.lightningnote.search.RecentSuggestionProvider"
        const val MODE = DATABASE_MODE_QUERIES
    }
}