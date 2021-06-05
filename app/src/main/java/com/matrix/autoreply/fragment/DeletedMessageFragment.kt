package com.matrix.autoreply.fragment

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.matrix.autoreply.activity.MsgLogViewerActivity
import com.matrix.autoreply.R
import java.io.File


private const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0


class DeletedMessageFragment : Fragment() {

    private val msgLogFileName = "msgLog.txt"
    private val signalMsgLogFileName = "signalMsgLog.txt"
    private val w4bMsgLogFileName = "waBusMsgLog.txt"
    private val whatsDeleted = File(Environment.getExternalStorageDirectory(),
            "WhatsDeleted${File.separator}WhatsDeleted Images")

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

        // Request Storage Permission
        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)


        return view
    }

    private fun createBackups() {
        if (!whatsDeleted.exists()) {
            if (!whatsDeleted.mkdirs())
                Toast.makeText(requireContext(), getString(R.string.create_backup_dir_failed),
                        Toast.LENGTH_SHORT).show()
        }
        if (!File(requireActivity().filesDir, msgLogFileName).exists()) {
            if (!File(requireActivity().filesDir, msgLogFileName).createNewFile())
                Toast.makeText(requireContext(), getString(R.string.create_msg_log_failed),
                        Toast.LENGTH_SHORT).show()
        }

        if (!File(requireActivity().filesDir, signalMsgLogFileName).exists()) {
            if (!File(requireActivity().filesDir, signalMsgLogFileName).createNewFile())
                Toast.makeText(requireContext(), getString(R.string.create_msg_log_failed),
                        Toast.LENGTH_SHORT).show()
        }

        if (!File(requireActivity().filesDir, w4bMsgLogFileName).exists()) {
            if (!File(requireActivity().filesDir, w4bMsgLogFileName).createNewFile())
                Toast.makeText(requireContext(), getString(R.string.create_msg_log_failed),
                        Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("SameParameterValue")
    private fun requestPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(requireActivity(),
                        permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(permission), requestCode)
        } else {
            createBackups()
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(requireContext(), getString(R.string.create_backup_dir), Toast.LENGTH_SHORT).show()
                createBackups()
            } else {
                Toast.makeText(requireContext(), getString(R.string.allow_storage_permission_msg), Toast.LENGTH_LONG).show()
            }
            return
        }
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