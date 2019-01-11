package com.dev.nihitb06.newlightningnote.appsettings

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.provider.SearchRecentSuggestions
import android.provider.Settings
import android.support.design.widget.Snackbar
import com.dev.nihitb06.newlightningnote.aboutapp.AboutAppActivity

import com.dev.nihitb06.newlightningnote.R
import com.dev.nihitb06.newlightningnote.apptour.AppTourActivity
import com.dev.nihitb06.newlightningnote.search.RecentSuggestionProvider.Companion.AUTHORITY
import com.dev.nihitb06.newlightningnote.search.RecentSuggestionProvider.Companion.MODE
import com.dev.nihitb06.newlightningnote.services.FloatingWidgetService
import com.dev.nihitb06.newlightningnote.services.ShakeToNoteService

class SettingsFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity.title = getString(R.string.settings)

        addPreferencesFromResource(R.xml.settings)

        PreferenceManager.getDefaultSharedPreferences(activity).registerOnSharedPreferenceChangeListener(this)

        findPreference(getString(R.string.key_clear_history)).setOnPreferenceClickListener {
            val searchRecentSuggestions = SearchRecentSuggestions(activity, AUTHORITY,  MODE)
            searchRecentSuggestions.clearHistory()

            Snackbar.make(activity.findViewById(R.id.fragmentContainer), "Your Search History has been cleared", Snackbar.LENGTH_LONG).show()

            return@setOnPreferenceClickListener true
        }
        findPreference(getString(R.string.key_floating_widget)).setOnPreferenceClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
                activity.startActivityForResult(
                        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+activity.packageName)),
                        REQUEST_CODE_DRAW_OVERLAY
                )
            } else {
                activity.startService(Intent(activity, FloatingWidgetService::class.java))
            }
            return@setOnPreferenceClickListener true
        }
        findPreference(getString(R.string.key_about_app)).setOnPreferenceClickListener {
            activity.startActivity(Intent(activity, AboutAppActivity::class.java))
            return@setOnPreferenceClickListener true
        }
        findPreference(getString(R.string.key_app_tour)).setOnPreferenceClickListener {
            activity.startActivity(Intent(activity, AppTourActivity::class.java))
            return@setOnPreferenceClickListener true
        }
    }

    override fun onDestroy() {
        activity.title = getString(R.string.app_name)

        PreferenceManager.getDefaultSharedPreferences(activity).unregisterOnSharedPreferenceChangeListener(this)

        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, key: String?) {
        when(key) {
            getString(R.string.key_theme) -> activity.recreate()
            getString(R.string.key_shake_to_note) -> {
                if(p0?.getBoolean(key, true) == true)
                    activity.startService(Intent(activity, ShakeToNoteService::class.java))
                else
                    activity.stopService(Intent(activity, ShakeToNoteService::class.java))
            }
        }
    }

    companion object {
        const val REQUEST_CODE_DRAW_OVERLAY = 106

        @JvmStatic
        fun newInstance() = SettingsFragment()
    }
}
