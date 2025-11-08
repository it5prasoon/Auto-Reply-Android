package com.matrix.autoreply.ui.activity

import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.matrix.autoreply.R
import com.matrix.autoreply.databinding.ActivityTabbedBinding
import com.matrix.autoreply.helpers.AlertDialogHelper
import com.matrix.autoreply.ui.activity.main.SectionsPagerAdapter

class TabbedActivity : AppCompatActivity() {
    private var appUpdateManager: AppUpdateManager? = null
    private lateinit var binding: ActivityTabbedBinding

    companion object {
        private const val TOOLBAR_TITLE_TEXT = "Auto Reply"
        private const val IMMEDIATE_APP_UPDATE_REQ_CODE = 124
        private const val TAG = "AppUpdate"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Fix window insets to prevent excessive spacing
        WindowCompat.setDecorFitsSystemWindows(window, true)
        
        binding = ActivityTabbedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        binding.toolbar.title = TOOLBAR_TITLE_TEXT
        setSupportActionBar(binding.toolbar)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        binding.viewPager.adapter = sectionsPagerAdapter
        binding.tabs.setupWithViewPager(binding.viewPager)
    }

    override fun onResume() {
        super.onResume()
        Log.i("MAIN ACTIVITY:: ", "OnResume")
        checkUpdate()
    }

    private fun checkUpdate() {
        val appUpdateInfoTask = appUpdateManager!!.appUpdateInfo
        Log.d(TAG, "Checking for updates")
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                startUpdateFlow(appUpdateInfo)
                Log.d(TAG, "Update available")
            } else {
                Log.d(TAG, "No Update available")
            }
        }
    }

    private fun startUpdateFlow(appUpdateInfo: AppUpdateInfo) {
        try {
            appUpdateManager!!.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, IMMEDIATE_APP_UPDATE_REQ_CODE)
        } catch (e: SendIntentException) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMMEDIATE_APP_UPDATE_REQ_CODE) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(applicationContext, "Update canceled by user! Result Code: $resultCode", Toast.LENGTH_LONG).show()
            } else if (resultCode == RESULT_OK) {
                Toast.makeText(applicationContext, "Update success! Result Code: $resultCode", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(applicationContext, "Update Failed! Result Code: $resultCode", Toast.LENGTH_LONG).show()
                checkUpdate()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_light_dark, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggle_theme -> {

                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_NO -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                    Configuration.UI_MODE_NIGHT_YES -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                }

                return true
            }

            R.id.help_menu -> {
                AlertDialogHelper.showDialog(
                        this,
                        "Help",
                        "1. Give the required permissions.\n" +
                                "2. Set custom text for auto reply.\n" +
                                "3. Select the applications for which you want auto reply.\n" +
                                "4. If you want group chat reply then turn on that option.\n" +
                                "5. If you increase the reply frequency then one person gets reply only the set value times.\n" +
                                "6. In MSG LOGS section you can see all the messages of the respective application (BETA).\n" +
                                "7. Enjoy!!\n\n" +
                                "This application is still under development so kindly post any bugs to github issues section you find on settings tab.",
                        getString(R.string.ok),
                        null
                ) { dialog, _ -> dialog.cancel() }

                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}