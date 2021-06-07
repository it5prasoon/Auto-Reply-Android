package com.matrix.autoreply.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.matrix.autoreply.AutoStartHelper;
import com.matrix.autoreply.R;
import com.matrix.autoreply.activity.MainActivity;

public class SettingsFragment extends PreferenceFragmentCompat {
    private SwitchPreference showNotificationPref;
    private Preference autoStartPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey);


        showNotificationPref = findPreference(getString(R.string.pref_show_notification_replied_msg));
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            showNotificationPref.setTitle(getString(R.string.show_notification_label) + "(Beta)");
        }

        autoStartPref = findPreference(getString(R.string.pref_auto_start_permission));
        autoStartPref.setOnPreferenceClickListener(preference -> {
            checkAutoStartPermission();
            return true;
        });
    }

    private void checkAutoStartPermission() {
        if(getActivity() != null) {
            AutoStartHelper.getInstance().getAutoStartPermission(getActivity());
        }
    }

    private void restartApp() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        getActivity().startActivity(intent);
        getActivity().finishAffinity();
    }
}
