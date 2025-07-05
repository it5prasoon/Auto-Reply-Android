package com.matrix.autoreply.ui.activity.logsViewer

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.matrix.autoreply.R
import com.matrix.autoreply.databinding.ActivityMsgLogViewerBinding
import com.matrix.autoreply.ui.activity.BaseActivity

/**
 * Activity to view and manage message logs.
 */
class MsgLogViewerActivity : BaseActivity() {

    private lateinit var binding: ActivityMsgLogViewerBinding
    private var activeFragment: RefreshListener? = null

    companion object {
        // Constants for message log file names
        private const val MESSAGE_LOGS_ACTION_BAR_TITLE = "Message Logs"
        private const val FEATURE_IN_PROGRESS_TOAST_MESSAGE = "The feature building is in progress.."
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMsgLogViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = resources.getColor(R.color.colorPrimary)

        val actionBar: ActionBar? = supportActionBar
        val colorDrawable = ColorDrawable(Color.parseColor("#171D3B"))
        actionBar!!.setBackgroundDrawable(colorDrawable)

        // Set the action bar title and enable back button
        actionBar.title = MESSAGE_LOGS_ACTION_BAR_TITLE
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)

        // Set up initial fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, ContactNameFragment())
                .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_msg_log_viewer, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                // Handle the "Refresh" action: refresh the message log
                activeFragment?.onRefresh()
                true
            }

            R.id.action_filter -> {
                // Handle the "Filter" action (Not implemented yet)
                Toast.makeText(
                    this, FEATURE_IN_PROGRESS_TOAST_MESSAGE,
                    Toast.LENGTH_LONG
                ).show()
                true
            }

            R.id.action_clear -> {
                // Handle the "Clear" action: show a confirmation dialog and clear the message log file
                Toast.makeText(
                    this, FEATURE_IN_PROGRESS_TOAST_MESSAGE,
                    Toast.LENGTH_LONG
                ).show()
                true
            }

            android.R.id.home -> {
                // Handle the back button press
                onBackPressed()
                true
            }
            
            else -> super.onOptionsItemSelected(item)
        }
    }
}
