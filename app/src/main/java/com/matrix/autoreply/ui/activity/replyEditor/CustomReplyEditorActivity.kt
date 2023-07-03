package com.matrix.autoreply.ui.activity.replyEditor

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
import com.matrix.autoreply.databinding.ActivityCustomReplyEditorBinding
import com.matrix.autoreply.ui.activity.BaseActivity
import com.matrix.autoreply.model.CustomRepliesData
import com.matrix.autoreply.preferences.PreferencesManager

/**
 * Activity for editing custom auto-reply messages.
 */
class CustomReplyEditorActivity : BaseActivity() {

    private lateinit var binding: ActivityCustomReplyEditorBinding
    private var autoReplyText: TextInputEditText? = null
    private var saveAutoReplyTextBtn: Button? = null
    private var customRepliesData: CustomRepliesData? = null
    private var preferencesManager: PreferencesManager? = null
    private var appendAttribution: CheckBox? = null

    companion object {
        private const val MESSAGE_STRING = "message"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomReplyEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = resources.getColor(R.color.colorPrimary)

        // Customize action bar
        val actionBar: ActionBar? = supportActionBar
        val colorDrawable = ColorDrawable(Color.parseColor("#171D3B"))
        actionBar!!.setBackgroundDrawable(colorDrawable)

        // Initialize data and views
        customRepliesData = CustomRepliesData.getInstance(this)
        preferencesManager = PreferencesManager.getPreferencesInstance(this)
        autoReplyText = binding.autoReplyTextInputEditText
        saveAutoReplyTextBtn = binding.saveCustomReplyBtn
        appendAttribution = binding.appendAttribution

        // Get intent data and set auto-reply text
        val intent = intent
        intent.action
        val data = intent.data
        autoReplyText?.setText(if (data != null) data.getQueryParameter(MESSAGE_STRING) else customRepliesData?.get())
        autoReplyText?.requestFocus()

        // Text change listener for auto-reply text
        autoReplyText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable) {
                // Disable save button if text does not satisfy requirements
                saveAutoReplyTextBtn?.isEnabled = CustomRepliesData.isValidCustomReply(editable)
            }
        })

        // Save button click listener
        saveAutoReplyTextBtn?.setOnClickListener { view: View? ->
            val setString = customRepliesData?.set(autoReplyText?.text)
            if (setString != null) {
                onNavigateUp()
            }
        }

        // Append attribution checkbox listener
        preferencesManager?.isAppendAutoreplyAttributionEnabled?.let { appendAttribution?.isChecked = it }
        appendAttribution?.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            preferencesManager?.setAppendAutoreplyAttribution(
                isChecked
            )
        }
    }
}
