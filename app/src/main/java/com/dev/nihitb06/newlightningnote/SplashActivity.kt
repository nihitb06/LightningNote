package com.dev.nihitb06.newlightningnote

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import com.dev.nihitb06.newlightningnote.apptour.AppTourActivity
import com.dev.nihitb06.newlightningnote.themeutils.ThemeActivity

class SplashActivity : ThemeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkUpgradeRun()
    }

    private fun checkUpgradeRun() {
        val preferences = getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
        val currentVersionCode = BuildConfig.VERSION_CODE
        val savedVersionCode = preferences.getInt(PREFERENCE_KEY, DEFAULT_VERSION_NUMBER)

        when {
            savedVersionCode == DEFAULT_VERSION_NUMBER -> {
                Log.d("TAG", savedVersionCode.toString())
                editPreference(preferences, currentVersionCode)
                goToAppTourActivity()
            }
            savedVersionCode < currentVersionCode -> {
                editPreference(preferences, currentVersionCode)
                goToMainActivity()
            }
            else -> goToMainActivity()
        }

        finish()
    }

    private fun editPreference(preferences: SharedPreferences, currentVersionCode: Int) {
        preferences.edit().putInt(PREFERENCE_KEY, currentVersionCode).apply()
    }
    private fun goToMainActivity() = startActivity(Intent(this, MainActivity::class.java))
    private fun goToAppTourActivity() = startActivity(Intent(this, AppTourActivity::class.java))

    companion object {
        private const val PREFERENCES_FILE_NAME = "com.dev.nihitb06.lightningnote"
        private const val PREFERENCE_KEY = "VersionNumber"
        private const val DEFAULT_VERSION_NUMBER = -1
    }
}
