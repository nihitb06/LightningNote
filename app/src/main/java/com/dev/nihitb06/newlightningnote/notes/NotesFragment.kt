package com.dev.nihitb06.newlightningnote.notes

import android.app.Fragment
import android.app.SearchManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.*
import android.widget.SearchView

import com.dev.nihitb06.newlightningnote.R
import com.dev.nihitb06.newlightningnote.utils.OnNoteClickListener
import com.dev.nihitb06.newlightningnote.themeutils.ThemeActivity
import kotlinx.android.synthetic.main.fragment_notes.view.*

class NotesFragment : Fragment() {

    private lateinit var fragmentContext: Context
    private lateinit var sharedPreferences: SharedPreferences

    private var showOnlyStarred = false
    private var showOnlyDeleted = false
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var staggeredGridLayoutManager: StaggeredGridLayoutManager

    private lateinit var rvNotesList: RecyclerView

    private var onFABClickListener: View.OnClickListener? = null
    private var onNoteClickListener: OnNoteClickListener? = null

    private lateinit var notesRecyclerAdapter: NotesRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        sortBy = sharedPreferences.getInt(sortStyle, ORDER_LAST_UPDATED)

        linearLayoutManager = object: LinearLayoutManager(activity) {
            override fun supportsPredictiveItemAnimations(): Boolean {
                return false
            }
        }

        staggeredGridLayoutManager = object: StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL) {
            override fun supportsPredictiveItemAnimations(): Boolean {
                return false
            }
        }
        staggeredGridLayoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val itemView = inflater.inflate(R.layout.fragment_notes, container, false)
        rvNotesList = itemView.rvNotesList

        rvNotesList.layoutManager = if(sharedPreferences.getBoolean(isListLinear, false))
             linearLayoutManager
        else
            staggeredGridLayoutManager

        notesRecyclerAdapter = NotesRecyclerAdapter(
                activity,
                showOnlyStarred,
                showOnlyDeleted,
                itemView.listEmpty,
                onNoteClickListener,
                sortBy,
                (activity as ThemeActivity).isThemeDark(),
                sharedPreferences.getBoolean(isListLinear, false)
        )
        rvNotesList.adapter = notesRecyclerAdapter
        rvNotesList.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                itemView.progress.visibility = View.GONE
                rvNotesList.visibility = View.VISIBLE
                if(!showOnlyStarred && !showOnlyDeleted)
                    itemView.fabAddNotes.visibility = View.VISIBLE

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    rvNotesList.viewTreeObserver.removeOnGlobalLayoutListener(this)
                } else {
                    rvNotesList.viewTreeObserver.removeGlobalOnLayoutListener(this)
                }
            }
        })
        setScroll()

        itemView.fabAddNotes.setOnClickListener { v: View? ->  onFABClickListener?.onClick(v) }

        return itemView
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater?.inflate(R.menu.menu_notes_list, menu)

        val searchManager: SearchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView: SearchView = menu?.findItem(R.id.search)?.actionView as SearchView

        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.componentName))
        searchView.isSubmitButtonEnabled = true
        searchView.isQueryRefinementEnabled = true
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)

        if(sharedPreferences.getBoolean(isListLinear, false)) {
            menu?.findItem(R.id.list_style)?.isVisible = false
            menu?.findItem(R.id.grid_style)?.isVisible = true
        } else {
            menu?.findItem(R.id.list_style)?.isVisible = true
            menu?.findItem(R.id.grid_style)?.isVisible = false
        }

        when(sortBy) {
            ORDER_LAST_UPDATED -> menu?.findItem(R.id.sortUpdated)?.isChecked = true
            ORDER_NEWEST -> menu?.findItem(R.id.sortNewest)?.isChecked = true
            ORDER_OLDEST -> menu?.findItem(R.id.sortOldest)?.isChecked = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.sortUpdated -> { changeSortBy(ORDER_LAST_UPDATED); item.isChecked = true }
            R.id.sortNewest -> { changeSortBy(ORDER_NEWEST); item.isChecked = true }
            R.id.sortOldest -> { changeSortBy(ORDER_OLDEST); item.isChecked = true }

            R.id.list_style -> rvNotesList.layoutManager = linearLayoutManager
            R.id.grid_style -> rvNotesList.layoutManager = staggeredGridLayoutManager
        }

        setScroll()
        sharedPreferences.edit().putBoolean(isListLinear, item?.itemId == R.id.list_style).apply()

        return super.onOptionsItemSelected(item)
    }

    private fun changeSortBy(sortBy: Int) {
        notesRecyclerAdapter.changeSortBy(sortBy)
        NotesFragment.sortBy = sortBy

        sharedPreferences.edit().putInt(sortStyle, sortBy).apply()
    }

    private fun setScroll() {
        rvNotesList.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                try {
                    (rvNotesList.layoutManager as StaggeredGridLayoutManager).invalidateSpanAssignments()
                } catch (e: ClassCastException) {
                    e.printStackTrace()
                }
            }
        })
    }

    companion object {
        private const val isListLinear = "IsListLinear"
        private const val sortStyle = "SortBy"
        private var sortBy = 0

        fun newInstance(
                context: Context,
                showOnlyStarred: Boolean,
                showOnlyDeleted: Boolean,
                onFABClickListener: View.OnClickListener,
                onNoteClickListener: OnNoteClickListener
        ): NotesFragment {
            val fragment = NotesFragment()

            fragment.fragmentContext = context
            fragment.showOnlyStarred = showOnlyStarred
            fragment.showOnlyDeleted = showOnlyDeleted

            fragment.onFABClickListener = onFABClickListener
            fragment.onNoteClickListener = onNoteClickListener

            return fragment
        }

        const val ORDER_LAST_UPDATED = 0
        const val ORDER_NEWEST = 1
        const val ORDER_OLDEST = 3
    }
}
