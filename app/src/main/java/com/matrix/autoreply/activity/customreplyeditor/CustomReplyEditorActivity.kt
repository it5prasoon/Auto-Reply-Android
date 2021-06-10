package com.matrix.autoreply.activity.customreplyeditor

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.appcompat.app.ActionBar
import com.google.android.material.textfield.TextInputEditText
import com.matrix.autoreply.R
import com.matrix.autoreply.activity.BaseActivity
import com.matrix.autoreply.model.CustomRepliesData
import com.matrix.autoreply.model.preferences.PreferencesManager


class CustomReplyEditorActivity : BaseActivity() {

    var autoReplyText: TextInputEditText? = null
    var saveAutoReplyTextBtn: Button? = null
    var customRepliesData: CustomRepliesData? = null
    var preferencesManager: PreferencesManager? = null
    var appendAttribution: CheckBox? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_reply_editor)
        window.statusBarColor = resources.getColor(R.color.colorPrimary)

        val actionBar: ActionBar? = supportActionBar
        val colorDrawable = ColorDrawable(Color.parseColor("#171D3B"))
        actionBar!!.setBackgroundDrawable(colorDrawable)


        customRepliesData = CustomRepliesData.getInstance(this)
        preferencesManager = PreferencesManager.getPreferencesInstance(this)
        autoReplyText = findViewById(R.id.autoReplyTextInputEditText)
        saveAutoReplyTextBtn = findViewById(R.id.saveCustomReplyBtn)
        appendAttribution = findViewById(R.id.appendAttribution)

        val intent = intent
        val action = intent.action
        val data = intent.data
        autoReplyText?.setText(if (data != null) data.getQueryParameter("message") else customRepliesData?.get())
        autoReplyText?.requestFocus()
        autoReplyText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable) {
                // Disable save button if text does not satisfy requirements
                saveAutoReplyTextBtn?.isEnabled = CustomRepliesData.isValidCustomReply(editable)
            }
        })
        saveAutoReplyTextBtn?.setOnClickListener { view: View? ->
            val setString = customRepliesData?.set(autoReplyText?.text)
            if (setString != null) {
                onNavigateUp()
            }
        }
        preferencesManager?.isAppendAutoreplyAttributionEnabled?.let { appendAttribution?.setChecked(it) }
        appendAttribution?.setOnCheckedChangeListener { compoundButton: CompoundButton?, isChecked: Boolean -> preferencesManager?.setAppendAutoreplyAttribution(isChecked) }
    }
}