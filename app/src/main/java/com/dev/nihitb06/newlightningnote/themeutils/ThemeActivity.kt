package com.dev.nihitb06.newlightningnote.themeutils

import android.app.ActivityManager
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import com.dev.nihitb06.newlightningnote.R

abstract class ThemeActivity : AppCompatActivity() {

    private lateinit var currentTheme: String

    override fun onCreate(savedInstanceState: Bundle?) {
        if(!::currentTheme.isInitialized)
            initializeCurrentTheme()

        setCurrentTheme()

        super.onCreate(savedInstanceState)
    }

    private fun initializeCurrentTheme() {
        currentTheme = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString(getString(R.string.key_theme), getString(R.string.theme_default))
    }

    private fun setCurrentTheme() {
        var appTheme = R.style.AppTheme

        when (currentTheme) {
            getString(R.string.theme_dark) -> appTheme = R.style.AppTheme_Dark
            getString(R.string.theme_red) -> appTheme = R.style.AppTheme_Red
            getString(R.string.theme_teal) -> appTheme = R.style.AppTheme_Teal
            getString(R.string.theme_green) -> appTheme = R.style.AppTheme_Green
            getString(R.string.theme_blue) -> appTheme = R.style.AppTheme_Blue
            getString(R.string.theme_purple) -> appTheme = R.style.AppTheme_Purple
        }

        setTheme(appTheme)
    }

    override fun setTheme(resId: Int) {
        super.setTheme(resId)

        if(!::currentTheme.isInitialized)
            initializeCurrentTheme()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTaskDescription(ActivityManager.TaskDescription(
                    getString(R.string.app_name),
                    BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher),
                    ContextCompat.getColor(this, getColorPrimary())
            ))
        }
    }

    private fun getColorPrimary(): Int {
        var color = R.color.colorPrimary

        when (currentTheme) {
            getString(R.string.theme_dark) -> color = R.color.colorDarkPrimary
            getString(R.string.theme_red) -> color = R.color.colorRedPrimary
            getString(R.string.theme_teal) -> color = R.color.colorTealPrimary
            getString(R.string.theme_green) -> color = R.color.colorGreenPrimary
            getString(R.string.theme_blue) -> color = R.color.colorBluePrimary
            getString(R.string.theme_purple) -> color = R.color.colorPurplePrimary
        }

        return color
    }

    /*
    * Sets the NoActionBar Derivative of the Selected App Theme
    * Used for Activities that make Use of a Toolbar
    * And do not require a System generated Action Bar
    */
    protected fun setNoActionBarTheme() {
        var appTheme = R.style.AppTheme_NoActionBar

        when (currentTheme) {
            getString(R.string.theme_dark) -> appTheme = R.style.AppTheme_Dark_NoActionBar
            getString(R.string.theme_red) -> appTheme = R.style.AppTheme_Red_NoActionBar
            getString(R.string.theme_teal) -> appTheme = R.style.AppTheme_Teal_NoActionBar
            getString(R.string.theme_green) -> appTheme = R.style.AppTheme_Green_NoActionBar
            getString(R.string.theme_blue) -> appTheme = R.style.AppTheme_Blue_NoActionBar
            getString(R.string.theme_purple) -> appTheme = R.style.AppTheme_Purple_NoActionBar
        }

        setTheme(appTheme)
    }

    fun isThemeDark(): Boolean = currentTheme == getString(R.string.theme_dark)
}
