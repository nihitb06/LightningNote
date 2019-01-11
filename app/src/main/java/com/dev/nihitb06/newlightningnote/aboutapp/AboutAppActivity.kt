package com.dev.nihitb06.newlightningnote.aboutapp

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import com.dev.nihitb06.newlightningnote.BuildConfig
import com.dev.nihitb06.newlightningnote.R
import com.dev.nihitb06.newlightningnote.themeutils.ThemeActivity
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kotlinx.android.synthetic.main.activity_about_app.*
import java.net.MalformedURLException

class AboutAppActivity : ThemeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setNoActionBarTheme()
        setContentView(R.layout.activity_about_app)

        setToolbar()

        val versionName = BuildConfig.VERSION_NAME
        appVersion.text = getString(R.string.version_placeholder, versionName)

        openSourceLicenses.setOnClickListener {
            startActivity(Intent(this@AboutAppActivity, OssLicensesMenuActivity::class.java))
        }
        assetsUsed.setOnClickListener {
            val dialog = Dialog(this@AboutAppActivity)

            dialog.setContentView(R.layout.layout_image_assets_used)

            dialog.findViewById<ConstraintLayout>(R.id.assetItem).setOnClickListener { openLink(getString(R.string.asset_link)) }

            dialog.show()
        }
        privacyPolicy.setOnClickListener { openLink(getString(R.string.policy_link)) }
        githubLink.setOnClickListener { openLink(getString(R.string.github_link)) }
    }

    private fun setToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun openLink(url: String) = try {
        startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)))
    } catch (e: MalformedURLException) {
        e.printStackTrace()
        Snackbar.make(root, "Something Went Wrong", Snackbar.LENGTH_SHORT).show()
    } catch (e: ActivityNotFoundException) {
        Snackbar.make(root, "No Activity can Handle the Action", Snackbar.LENGTH_SHORT).show()
    }
}
