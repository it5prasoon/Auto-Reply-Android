package com.matrix.autoreply.activity.customreplyeditor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;

import com.google.android.material.textfield.TextInputEditText;
import com.matrix.autoreply.R;
import com.matrix.autoreply.activity.BaseActivity;
import com.matrix.autoreply.model.CustomRepliesData;
import com.matrix.autoreply.model.preferences.PreferencesManager;

public class CustomReplyEditorActivity extends BaseActivity {
    TextInputEditText autoReplyText;
    Button saveAutoReplyTextBtn;
    CustomRepliesData customRepliesData;
    PreferencesManager preferencesManager;
    CheckBox appendAttribution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_reply_editor);

        customRepliesData = CustomRepliesData.getInstance(this);
        preferencesManager = PreferencesManager.getPreferencesInstance(this);

        autoReplyText = findViewById(R.id.autoReplyTextInputEditText);
        saveAutoReplyTextBtn = findViewById(R.id.saveCustomReplyBtn);
        appendAttribution = findViewById(R.id.appendAttribution);

        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();

        autoReplyText.setText ((data != null)
                ? data.getQueryParameter("message")
                : customRepliesData.get());

        autoReplyText.requestFocus();
        autoReplyText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable editable) {
                // Disable save button if text does not satisfy requirements
                saveAutoReplyTextBtn.setEnabled(CustomRepliesData.isValidCustomReply(editable));
            }
        });

        saveAutoReplyTextBtn.setOnClickListener(view -> {
            String setString = customRepliesData.set(autoReplyText.getText());
            if (setString != null) {
                this.onNavigateUp();
            }
        });

        appendAttribution.setChecked(preferencesManager.isAppendWatomaticAttributionEnabled());
        appendAttribution.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            preferencesManager.setAppendWatomaticAttribution(isChecked);
        });
    }
}