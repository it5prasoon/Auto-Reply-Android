package com.matrix.autoreply.ui.activity

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.matrix.autoreply.helpers.AlertDialogHelper
import com.matrix.autoreply.ui.adapters.MsgLogAdapter
import com.matrix.autoreply.R
import com.matrix.autoreply.databinding.ActivityMsgLogViewerBinding
import java.io.File
import java.io.PrintWriter
import java.lang.Exception

/**
 * Activity to view and manage message logs.
 */
class MsgLogViewerActivity : BaseActivity() {

    private lateinit var binding: ActivityMsgLogViewerBinding
    private var msgLogFileName = ""

    companion object {
        // Constants for message log file names
        private const val MSG_LOG_FILE_NAME_WHATSAPP = "msgLog.txt"
        private const val MSG_LOG_FILE_NAME_SIGNAL = "signalMsgLog.txt"
        private const val MSG_LOG_FILE_NAME_WABUS = "waBusMsgLog.txt"
        private const val MESSAGE_LOGS_ACTION_BAR_TITLE = "Message Logs"
        private const val ACTION_FILTER_TOAST_MESSAGE = "The feature is under progress.."
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMsgLogViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = resources.getColor(R.color.colorPrimary)

        val actionBar: ActionBar? = supportActionBar
        val colorDrawable = ColorDrawable(Color.parseColor("#171D3B"))
        actionBar!!.setBackgroundDrawable(colorDrawable)

        // Set the action bar title
        actionBar.title = MESSAGE_LOGS_ACTION_BAR_TITLE

        // Determine the appropriate message log file based on the app (WhatsApp, Signal, or WABus)
        val app = intent.getStringExtra("app")
        msgLogFileName = when (app) {
            "whatsapp" -> MSG_LOG_FILE_NAME_WHATSAPP
            "signal" -> MSG_LOG_FILE_NAME_SIGNAL
            else -> MSG_LOG_FILE_NAME_WABUS
        }

        // Set up RecyclerView to display message logs
        val msgLogRecyclerView = binding.msgLogRecyclerView
        msgLogRecyclerView.adapter = MsgLogAdapter(readFile(File(filesDir, msgLogFileName)))
        msgLogRecyclerView.layoutManager = LinearLayoutManager(this)
        msgLogRecyclerView.setHasFixedSize(true)

        // Set up SwipeRefreshLayout for refreshing the message log
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshMsgLog()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    // Read the contents of the message log file and return it as a list of strings (lines)
    private fun readFile(fileName: File): List<String> = fileName.bufferedReader().readLines().asReversed()

    // Refresh the message log RecyclerView with the updated contents of the file
    private fun refreshMsgLog() {
        binding.msgLogRecyclerView.adapter = MsgLogAdapter(readFile(File(filesDir, msgLogFileName)))
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
                refreshMsgLog()
                true
            }

            R.id.action_filter -> {
                // Handle the "Filter" action (Not implemented yet)
                Toast.makeText(
                    this, ACTION_FILTER_TOAST_MESSAGE,
                    Toast.LENGTH_LONG
                ).show()
                true
            }

            R.id.action_clear -> {
                // Handle the "Clear" action: show a confirmation dialog and clear the message log file
                AlertDialogHelper.showDialog(
                    this@MsgLogViewerActivity,
                    getString(R.string.clear_msg_log),
                    getString(R.string.clear_msg_log_confirm),
                    getString(R.string.yes),
                    getString(R.string.cancel)
                ) { _, _ ->
                    try {
                        PrintWriter(File(filesDir, msgLogFileName)).use { out -> out.println("") }
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.cleared),
                            Toast.LENGTH_SHORT
                        ).show()
                        refreshMsgLog()
                    } catch (e: Exception) {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.clear_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
