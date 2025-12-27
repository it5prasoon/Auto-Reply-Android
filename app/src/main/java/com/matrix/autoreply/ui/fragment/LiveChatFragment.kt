package com.matrix.autoreply.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.matrix.autoreply.R
import com.matrix.autoreply.helpers.AlertDialogHelper
import com.matrix.autoreply.model.App
import com.matrix.autoreply.preferences.LiveChatPreferencesManager
import com.matrix.autoreply.services.LiveChatAccessibilityService

/**
 * Live Chat Fragment
 * Provides UI for the new Live Chat feature using Accessibility Service
 */
class LiveChatFragment : Fragment() {

    private lateinit var preferencesManager: LiveChatPreferencesManager
    private var mInterstitialAd: InterstitialAd? = null
    
    // UI Components
    private lateinit var mainSwitch: SwitchCompat
    private lateinit var statusCard: CardView
    private lateinit var statusText: TextView
    private lateinit var statusIcon: ImageView
    private lateinit var permissionCard: CardView
    private lateinit var permissionButton: Button
    private lateinit var permissionStatusIcon: ImageView
    private lateinit var setupProgressBar: ProgressBar
    private lateinit var setupProgressText: TextView
    private lateinit var appsContainer: LinearLayout
    private lateinit var replyDelaySeekBar: SeekBar
    private lateinit var replyDelayText: TextView
    private lateinit var aiReplySwitch: SwitchCompat
    private lateinit var groupChatSwitch: SwitchCompat
    private lateinit var testInstructionsCard: CardView

    companion object {
        private const val TAG = "LiveChatFragment"
        
        // Supported apps
        private val SUPPORTED_APPS = listOf(
            App("WhatsApp", "com.whatsapp"),
            App("WhatsApp Business", "com.whatsapp.w4b"),
            App("Messenger", "com.facebook.orca"),
            App("Instagram", "com.instagram.android")
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_live_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        preferencesManager = LiveChatPreferencesManager.getInstance(requireContext())
        
        initializeViews(view)
        setupListeners()
        loadInterstitialAd()
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun initializeViews(view: View) {
        mainSwitch = view.findViewById(R.id.live_chat_main_switch)
        statusCard = view.findViewById(R.id.status_card)
        statusText = view.findViewById(R.id.status_text)
        statusIcon = view.findViewById(R.id.status_icon)
        permissionCard = view.findViewById(R.id.permission_card)
        permissionButton = view.findViewById(R.id.permission_button)
        permissionStatusIcon = view.findViewById(R.id.permission_status_icon)
        setupProgressBar = view.findViewById(R.id.setup_progress_bar)
        setupProgressText = view.findViewById(R.id.setup_progress_text)
        appsContainer = view.findViewById(R.id.apps_container)
        replyDelaySeekBar = view.findViewById(R.id.reply_delay_seekbar)
        replyDelayText = view.findViewById(R.id.reply_delay_text)
        aiReplySwitch = view.findViewById(R.id.ai_reply_switch)
        groupChatSwitch = view.findViewById(R.id.group_chat_switch)
        testInstructionsCard = view.findViewById(R.id.test_instructions_card)
    }

    private fun setupListeners() {
        // Main switch
        mainSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isAccessibilityServiceEnabled()) {
                // Show dialog to enable accessibility
                showAccessibilityRequiredDialog()
                mainSwitch.isChecked = false
            } else {
                preferencesManager.isLiveChatEnabled = isChecked
                if (isChecked) {
                    // Show interstitial ad when enabling Live Chat
                    showInterstitialAd()
                }
                updateUI()
            }
        }

        // Permission button
        permissionButton.setOnClickListener {
            openAccessibilitySettings()
        }

        // Reply delay
        replyDelaySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val delaySeconds = progress + 1L // 1-10 seconds
                replyDelayText.text = "$delaySeconds seconds"
                if (fromUser) {
                    preferencesManager.replyDelaySeconds = delaySeconds
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // AI reply switch
        aiReplySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Check if main AI is enabled in settings
                val mainPrefs = com.matrix.autoreply.preferences.PreferencesManager.getPreferencesInstance(requireContext())
                val mainAiEnabled = mainPrefs?.isAiEnabled ?: false
                
                if (!mainAiEnabled) {
                    // Main AI is disabled - show dialog
                    aiReplySwitch.isChecked = false
                    showAiDisabledDialog()
                } else if (preferencesManager.aiApiKey.isNullOrEmpty()) {
                    // AI not configured - show setup dialog
                    aiReplySwitch.isChecked = false
                    showAiSetupDialog()
                } else {
                    // AI is configured and enabled - enable it for Live Chat
                    preferencesManager.isAiEnabled = true
                }
            } else {
                // Disable AI replies
                preferencesManager.isAiEnabled = false
            }
        }

        // Group chat switch
        groupChatSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.isGroupChatEnabled = isChecked
        }

        // Setup app checkboxes
        setupAppCheckboxes()
    }

    private fun setupAppCheckboxes() {
        appsContainer.removeAllViews()
        
        for (app in SUPPORTED_APPS) {
            val checkBox = CheckBox(requireContext()).apply {
                text = app.name
                isChecked = preferencesManager.isAppEnabled(app.packageName)
                textSize = 16f
                setPadding(16, 16, 16, 16)
                
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        preferencesManager.addEnabledApp(app.packageName)
                    } else {
                        preferencesManager.removeEnabledApp(app.packageName)
                    }
                    updateSetupProgress()
                }
            }
            
            appsContainer.addView(checkBox)
        }
    }

    private fun updateUI() {
        val isServiceRunning = LiveChatAccessibilityService.isServiceRunning
        val isEnabled = preferencesManager.isLiveChatEnabled
        val hasPermission = isAccessibilityServiceEnabled()
        
        // Check main AI settings
        val mainPrefs = com.matrix.autoreply.preferences.PreferencesManager.getPreferencesInstance(requireContext())
        val mainAiEnabled = mainPrefs?.isAiEnabled ?: false
        
        // Update main switch
        mainSwitch.isChecked = isEnabled
        
        // Update status card
        if (isServiceRunning && isEnabled) {
            statusText.text = "Live Chat is Active"
            statusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
            statusIcon.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
            statusCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_light))
        } else {
            statusText.text = "Live Chat is Inactive"
            statusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
            statusIcon.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
            statusCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        }
        
        // Update permission status
        if (hasPermission) {
            permissionStatusIcon.setImageResource(android.R.drawable.checkbox_on_background)
            permissionStatusIcon.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
            permissionButton.text = "Permission Granted ✓"
            permissionButton.isEnabled = false
            preferencesManager.isAccessibilityPermissionGranted = true
        } else {
            permissionStatusIcon.setImageResource(android.R.drawable.ic_dialog_alert)
            permissionStatusIcon.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark))
            permissionButton.text = "Grant Permission"
            permissionButton.isEnabled = true
            preferencesManager.isAccessibilityPermissionGranted = false
        }
        
        // Update setup progress
        updateSetupProgress()
        
        // Update settings
        replyDelaySeekBar.progress = (preferencesManager.replyDelaySeconds - 1).toInt()
        replyDelayText.text = "${preferencesManager.replyDelaySeconds} seconds"
        
        // Sync AI switch with main AI settings
        if (!mainAiEnabled) {
            // If main AI is disabled, Live Chat AI should also be disabled
            aiReplySwitch.isChecked = false
            preferencesManager.isAiEnabled = false
        } else {
            aiReplySwitch.isChecked = preferencesManager.isAiEnabled
        }
        
        groupChatSwitch.isChecked = preferencesManager.isGroupChatEnabled
        
        // Show/hide test instructions
        testInstructionsCard.visibility = if (isServiceRunning && isEnabled) View.VISIBLE else View.GONE
    }

    private fun updateSetupProgress() {
        val progress = preferencesManager.getSetupProgress()
        setupProgressBar.progress = progress
        setupProgressText.text = "$progress% Complete"
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val serviceName = "${requireContext().packageName}/${LiveChatAccessibilityService::class.java.name}"
        val enabledServices = Settings.Secure.getString(
            requireContext().contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        
        return enabledServices.contains(serviceName)
    }

    private fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            
            Toast.makeText(
                requireContext(),
                "Find 'Auto Reply Live Chat' and enable it",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Unable to open accessibility settings",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showAccessibilityRequiredDialog() {
        AlertDialogHelper.showDialog(
            requireContext(),
            "Accessibility Permission Required",
            "Live Chat requires Accessibility Service permission to:\n\n" +
                    "• Read messages from chat screens\n" +
                    "• Type replies in the message input field\n" +
                    "• Send messages automatically\n\n" +
                    "This permission is essential for Live Chat to function.\n\n" +
                    "Would you like to grant this permission now?",
            "Grant Permission",
            "Cancel"
        ) { dialog, which ->
            if (which == -1) { // Positive button
                openAccessibilitySettings()
            }
            dialog.dismiss()
        }
    }

    private fun showAiDisabledDialog() {
        AlertDialogHelper.showDialog(
            requireContext(),
            "AI Replies Disabled",
            "AI replies are currently disabled in your main app settings.\n\n" +
                    "To use AI-powered replies in Live Chat, you must first enable AI in the main settings.\n\n" +
                    "Would you like to go to AI settings now?",
            "Go to Settings",
            "Cancel"
        ) { dialog, which ->
            if (which == -1) { // Positive button
                // Navigate to AI settings
                val intent = Intent(requireContext(), com.matrix.autoreply.ui.activity.AiSettingsActivity::class.java)
                startActivity(intent)
                Toast.makeText(
                    requireContext(),
                    "Enable AI and configure it, then return here",
                    Toast.LENGTH_LONG
                ).show()
            }
            dialog.dismiss()
        }
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        val adUnitId = getString(R.string.live_chat_enable_interstitial)

        InterstitialAd.load(requireContext(), adUnitId, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }
            })
    }

    private fun showInterstitialAd() {
        mInterstitialAd?.show(requireActivity())
    }

    private fun showAiSetupDialog() {
        AlertDialogHelper.showDialog(
            requireContext(),
            "AI Configuration Required",
            "AI-powered replies require proper configuration:\n\n" +
                    "1. AI API Key must be set\n" +
                    "2. AI must be enabled in settings\n\n" +
                    "Live Chat uses your main AI settings from the app.\n\n" +
                    "Would you like to configure AI now?",
            "Configure AI",
            "Cancel"
        ) { dialog, which ->
            if (which == -1) { // Positive button
                // Navigate to AI settings
                val intent = Intent(requireContext(), com.matrix.autoreply.ui.activity.AiSettingsActivity::class.java)
                startActivity(intent)
                Toast.makeText(
                    requireContext(),
                    "After setting up AI, return here and enable AI replies",
                    Toast.LENGTH_LONG
                ).show()
            }
            dialog.dismiss()
        }
    }
}
