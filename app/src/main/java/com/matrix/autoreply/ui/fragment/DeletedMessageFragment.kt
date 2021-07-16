package com.matrix.autoreply.ui.fragment

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.matrix.autoreply.NotificationListener
import com.matrix.autoreply.R
import com.matrix.autoreply.ui.activity.MsgLogViewerActivity


open class DeletedMessageFragment : Fragment() {

    private val msgLogFileName = "msgLog.txt"
    private val signalMsgLogFileName = "signalMsgLog.txt"
    private val w4bMsgLogFileName = "waBusMsgLog.txt"
    private val REQ_NOTIFICATION_LISTENER = 1000

    private val checkEmoji = String(Character.toChars(0x2714))
    private val crossEmoji = String(Character.toChars(0x274C))
    private var notificationListenerSwitch: SwitchMaterial? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    @Nullable
    override fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_deleted_message, container, false)

        // Widgets
        val msgLogStatus = view.findViewById<TextView>(R.id.msg_log_status)
        val viewWALogBtn = view.findViewById<Button>(R.id.view_wa_log_btn)
        val viewSignalLogBtn = view.findViewById<Button>(R.id.view_signal_log_btn)
        val viewW4bLogBtn = view.findViewById<Button>(R.id.view_wabus_log_btn)
        notificationListenerSwitch = view.findViewById(R.id.notification_listener_switch)
        val test = view.findViewById<LinearLayout>(R.id.test)

        msgLogStatus.text = getString(R.string.msg_log_status_str, checkEmoji)

//        msgLogStatus.text = getString(R.string.msg_log_status_str,
//                    if (File(requireActivity().filesDir, msgLogFileName).exists()
//                            && File(requireActivity().filesDir, signalMsgLogFileName).exists()
//                            && File(requireActivity().filesDir, w4bMsgLogFileName).exists()) checkEmoji else crossEmoji)

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

        notificationListenerSwitch?.isClickable = false
        test.setOnClickListener {
            if (notificationListenerSwitch?.isChecked == true) {
                openSomeActivityForResult()
            } else {
                openSomeActivityForResult()
            }
        }
        notificationListenerSwitch?.isChecked = requireActivity().isServiceRunning(NotificationListener::class.java)
        return view
    }

    @Suppress("DEPRECATION")
    private fun <T> Context.isServiceRunning(service: Class<T>): Boolean {
        return (getSystemService(ACTIVITY_SERVICE) as ActivityManager)
                .getRunningServices(Integer.MAX_VALUE)
                .any { it.service.className == service.name }
    }


    //https://stackoverflow.com/questions/20141727/check-if-user-has-granted-notificationlistener-access-to-my-app/28160115
    //TODO: Use in UI to verify if it needs enabling or restarting
    open fun isListenerEnabled(context: Context, notificationListenerCls: Class<*>?): Boolean {
        val cn = ComponentName(context, notificationListenerCls!!)
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(cn.flattenToString())
    }


    private fun openSomeActivityForResult() {
        val NOTIFICATION_LISTENER_SETTINGS: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
        } else {
            "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
        }
        val i = Intent(NOTIFICATION_LISTENER_SETTINGS)
        startActivityForResult(i, REQ_NOTIFICATION_LISTENER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_NOTIFICATION_LISTENER) {
            if (isListenerEnabled(requireContext(), NotificationListener::class.java)) {
                Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_LONG).show()
                if (requireActivity().isServiceRunning(NotificationListener::class.java))
                    notificationListenerSwitch?.isChecked = true
            } else {
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_LONG).show()
                notificationListenerSwitch?.isChecked = false
            }
        }
    }
}