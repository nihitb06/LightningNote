package com.dev.nihitb06.newlightningnote.notes.noteutils

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.util.SparseArray
import android.util.SparseBooleanArray
import android.view.View
import com.dev.nihitb06.newlightningnote.R

class MultiSelectionModeHelper (private val context: Context) {
    private val checkStates = SparseBooleanArray()
    private val checkedViews = SparseArray<View>()
    private var checkedItemCount = 0

    fun setItemChecked(view: View, position: Int) {
        checkedItemCount++
        checkStates.put(position, true)
        addCheckedView(view, position)
    }
    private fun removeItemChecked(position: Int) {
        checkedItemCount--
        checkStates.delete(position)
        removeCheckedView(position)
    }
    fun toggleItemChecked(view: View, position: Int)
            = if(checkStates.get(position)) removeItemChecked(position) else setItemChecked(view, position)

    private fun addCheckedView(view: View, position: Int) {
        checkedViews.put(position, view)
        view.findViewById<CardView>(R.id.root).setBackgroundColor(ContextCompat.getColor(context, R.color.semi_transparent))
    }
    private fun removeCheckedView(position: Int) {
        val view = checkedViews.get(position)
        checkedViews.remove(position)
        view.findViewById<CardView>(R.id.root).setBackgroundColor(ContextCompat.getColor(context, android.R.color.background_light))
    }
    private fun deselectAllViews() {
        for(i in checkedViews.size()-1 downTo 0) {
            removeCheckedView(checkedViews.keyAt(i))
        }
    }

    fun getCheckedItemCount() = checkedItemCount
    fun getCheckedItemFirstPosition() = checkStates.keyAt(0)
    fun getCheckedItemLastPosition() = checkStates.keyAt(checkStates.size()-1)

    fun getSelected() = Array(checkedItemCount) { checkStates.keyAt(it) }

    fun clearChoices() {
        deselectAllViews()
        checkStates.clear()
        checkedViews.clear()
        checkedItemCount = 0
    }
}