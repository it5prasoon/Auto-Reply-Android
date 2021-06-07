package com.matrix.autoreply.fragment

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.matrix.autoreply.AlertDialogHelper
import com.matrix.autoreply.NotificationListener
import com.matrix.autoreply.R
import com.matrix.autoreply.activity.MsgLogViewerActivity
import java.io.File


private const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0


class DeletedMessageFragment : Fragment() {

    private val msgLogFileName = "msgLog.txt"
    private val signalMsgLogFileName = "signalMsgLog.txt"
    private val w4bMsgLogFileName = "waBusMsgLog.txt"


    private val checkEmoji = String(Character.toChars(0x2714))
    private val crossEmoji = String(Character.toChars(0x274C))

    @Nullable
    override fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_deleted_message, container, false)

        // Widgets
        val msgLogStatus = view.findViewById<TextView>(R.id.msg_log_status)
        val viewWALogBtn = view.findViewById<Button>(R.id.view_wa_log_btn)
        val viewSignalLogBtn = view.findViewById<Button>(R.id.view_signal_log_btn)
        val viewW4bLogBtn = view.findViewById<Button>(R.id.view_wabus_log_btn)
        val notificationListenerSwitch = view.findViewById<SwitchMaterial>(R.id.notification_listener_switch)
        val test = view.findViewById<LinearLayout>(R.id.test)


        // TextView
        msgLogStatus.text = getString(R.string.msg_log_status_str,
                if (File(requireActivity().filesDir, msgLogFileName).exists()
                        && File(requireActivity().filesDir, signalMsgLogFileName).exists()) checkEmoji else crossEmoji)

        // Button
        // DRY
        viewWALogBtn.setOnClickListener {
            val intent = Intent(requireActivity(), MsgLogViewerActivity::class.java)
            intent.putExtra("app", "whatsapp")
            startActivity(intent)
        }

        viewSignalLogBtn.setOnClickListener {
            val intent = Intent(requireActivity(), MsgLogViewerActivity::class.java)
            intent.putExtra("app", "signal")
            startActivity(intent)
        }

        viewW4bLogBtn.setOnClickListener {
            val intent = Intent(requireActivity(), MsgLogViewerActivity::class.java)
            intent.putExtra("app", "w4b")
            startActivity(intent)
        }

        notificationListenerSwitch.isChecked = requireActivity().isServiceRunning(NotificationListener::class.java)
        notificationListenerSwitch.isClickable = false
        test.setOnClickListener {
            if (notificationListenerSwitch.isChecked) {
                AlertDialogHelper.showDialog(
                        requireContext(),
                        "Turn off",
                        "Settings > Apps  & notifications > Special app access > " +
                                "Notification Access > Auto Reply Msg Log > Turn Off",
                        getString(R.string.ok),
                        null
                ) { dialog, _ -> dialog.cancel() }
            }
            else {
                AlertDialogHelper.showDialog(
                        requireContext(),
                        "Turn on",
                        "Settings > Apps & notifications > Special app access > " +
                                "Notification Access > Auto Reply Msg Log > Allow",
                        getString(R.string.ok),
                        null
                ) { dialog, _ -> dialog.cancel() }
            }
        }

        return view
    }

    @Suppress("DEPRECATION")
    private fun <T> Context.isServiceRunning(service: Class<T>): Boolean {
        return (getSystemService(ACTIVITY_SERVICE) as ActivityManager)
                .getRunningServices(Integer.MAX_VALUE)
                .any { it.service.className == service.name }
    }

    private fun deleteRecursive(f: File) {
        if (f.isDirectory) {
            for (child in f.listFiles()) {
                if (!child.deleteRecursively())
                    Toast.makeText(requireContext(),
                            getString(R.string.unable_to_delete, child.toString()),
                            Toast.LENGTH_SHORT).show()
            }
        }
    }
}