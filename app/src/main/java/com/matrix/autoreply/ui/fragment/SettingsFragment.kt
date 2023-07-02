package com.matrix.autoreply.ui.fragment

import com.matrix.autoreply.helpers.AutoStartHelper.Companion.instance
import androidx.preference.PreferenceFragmentCompat
import android.os.Bundle
import com.matrix.autoreply.R
import android.content.Intent
import android.os.Build
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.matrix.autoreply.ui.activity.TabbedActivity

class SettingsFragment : PreferenceFragmentCompat() {

    private var showNotificationPref: SwitchPreference? = null
    private var autoStartPref: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey)
        showNotificationPref = findPreference(getString(R.string.pref_show_notification_replied_msg))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            showNotificationPref!!.title = getString(R.string.show_notification_label) + "(Beta)"
        }
        autoStartPref = findPreference(getString(R.string.pref_auto_start_permission))
        autoStartPref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference: Preference? ->
            checkAutoStartPermission()
            true
        }
    }

    private fun checkAutoStartPermission() {
        if (activity != null) {
            instance.getAutoStartPermission(requireActivity())
        }
    }

    private fun restartApp() {
        val intent = Intent(activity, TabbedActivity::class.java)
        requireActivity().startActivity(intent)
        requireActivity().finishAffinity()
    }
}