package com.matrix.autoreply.ui.fragment

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.matrix.autoreply.R
import com.matrix.autoreply.constants.Constants
import com.matrix.autoreply.helpers.AutoStartHelper.Companion.instance
import com.matrix.autoreply.preferences.PreferencesManager
import com.matrix.autoreply.ui.CustomDialog
import com.matrix.autoreply.utils.NotificationListenerUtil

class SettingsFragment : PreferenceFragmentCompat() {

    private var showNotificationPref: SwitchPreference? = null
    private var restartServicePref: Preference? = null
    private var autoStartPref: Preference? = null
    private var preferencesManager: PreferencesManager? = null
    private var mActivity: Activity? = null
    private lateinit var notificationListenerUtil: NotificationListenerUtil
    private lateinit var notificationListenerPermissionLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val SERVICE_ALREADY_ENABLED = "Service is already enabled"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey)
        showNotificationPref = findPreference(getString(R.string.pref_show_notification_replied_msg))

        mActivity = activity
        preferencesManager = PreferencesManager.getPreferencesInstance(mActivity!!)
        notificationListenerUtil = NotificationListenerUtil(mActivity!!)

        restartServicePref = findPreference(getString(R.string.pref_notification_listener_permission))
        restartServicePref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startNotificationListenerService()
            true
        }

        autoStartPref = findPreference(getString(R.string.pref_auto_start_permission))
        autoStartPref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            checkAutoStartPermission()
            true
        }

        // Initialize the ActivityResultLauncher
        notificationListenerPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val granted = notificationListenerUtil.isNotificationServiceEnabled()
                handleNotificationListenerPermissionResult(granted)
            }
    }

    private fun checkAutoStartPermission() {
        if (activity != null) {
            instance.getAutoStartPermission(requireActivity())
        }
    }

    private fun startNotificationListenerService() {
        if (notificationListenerUtil.isNotificationServiceEnabled()) {
            Toast.makeText(requireContext(), SERVICE_ALREADY_ENABLED, Toast.LENGTH_SHORT).show()
        } else {
            showPermissionsDialog()
        }
    }

    private fun showPermissionsDialog() {
        val customDialog = CustomDialog(mActivity!!)
        val bundle = Bundle()
        bundle.putString(Constants.PERMISSION_DIALOG_TITLE, getString(R.string.permission_dialog_title))
        bundle.putString(Constants.PERMISSION_DIALOG_MSG, getString(R.string.permission_dialog_msg))
        customDialog.showDialog(bundle, null) { _: DialogInterface?, which: Int ->
            if (which == -2) {
                //Decline
                showPermissionDeniedDialog()
            } else {
                //Accept
                requestNotificationListenerPermission()
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        val customDialog = CustomDialog(mActivity!!)
        val bundle = Bundle()
        bundle.putString(Constants.PERMISSION_DIALOG_DENIED_TITLE, getString(R.string.permission_dialog_denied_title))
        bundle.putString(Constants.PERMISSION_DIALOG_DENIED_MSG, getString(R.string.permission_dialog_denied_msg))
        bundle.putBoolean(Constants.PERMISSION_DIALOG_DENIED, true)
        customDialog.showDialog(bundle, null) { _: DialogInterface?, which: Int ->
            if (which == -2) {
                // Decline
                Toast.makeText(mActivity, Constants.PERMISSION_DENIED, Toast.LENGTH_LONG).show()
            } else {
                // Accept
                requestNotificationListenerPermission()
            }
        }
    }

    private fun requestNotificationListenerPermission() {
        notificationListenerUtil.requestNotificationListenerPermission(notificationListenerPermissionLauncher)
    }

    private fun handleNotificationListenerPermissionResult(granted: Boolean) {
        if (granted) {
            preferencesManager!!.setServicePref(true)
            Toast.makeText(requireActivity(), Constants.PERMISSION_GRANTED, Toast.LENGTH_SHORT).show()
        } else {
            preferencesManager!!.setServicePref(false)
            Toast.makeText(requireActivity(), Constants.PERMISSION_DENIED, Toast.LENGTH_SHORT).show()
        }
    }
}
