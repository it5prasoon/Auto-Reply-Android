package com.matrix.autoreply.ui.activity.notification;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.matrix.autoreply.R;
import com.matrix.autoreply.ui.activity.BaseActivity;
import com.matrix.autoreply.model.utils.NotificationHelper;

public class NotificationIntentActivity extends BaseActivity {

    private static final String TAG = NotificationIntentActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_intent_activity); //dummy layout

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.getString("package") != null)
            {
                String packageName = extras.getString("package");
                NotificationHelper.getInstance(getApplicationContext()).markNotificationDismissed(packageName);
                launchApp(packageName);
            }
        }
    }

    private void launchApp(String packageName){
        Intent intent;
        PackageManager pm = getPackageManager();

        intent = pm.getLaunchIntentForPackage(packageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);

        finish();
    }
}
