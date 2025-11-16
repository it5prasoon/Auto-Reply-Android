package com.matrix.autoreply.ui.fragment

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.switchmaterial.SwitchMaterial
import com.matrix.autoreply.R
import com.matrix.autoreply.constants.Constants
import com.matrix.autoreply.constants.Constants.MAX_DAYS
import com.matrix.autoreply.constants.Constants.MIN_DAYS
import com.matrix.autoreply.databinding.FragmentDeletedMessageBinding
import com.matrix.autoreply.databinding.FragmentMainBinding
import com.matrix.autoreply.model.App
import com.matrix.autoreply.model.CustomRepliesData
import com.matrix.autoreply.model.CustomRepliesData.Companion.getInstance
import com.matrix.autoreply.preferences.PreferencesManager
import com.matrix.autoreply.preferences.PreferencesManager.Companion.getPreferencesInstance
import com.matrix.autoreply.ui.CustomDialog
import com.matrix.autoreply.ui.activity.replyEditor.CustomReplyEditorActivity
import com.matrix.autoreply.utils.NotificationListenerUtil

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private var replyOptionsCard: CardView? = null
    private var customTextOptionCard: CardView? = null
    private var smartRepliesOptionCard: CardView? = null
    private var timePickerCard: CardView? = null
    private var customTextPreview: TextView? = null
    private var timeSelectedTextPreview: TextView? = null
    private var timePickerSubTitleTextPreview: TextView? = null
    private var customRepliesData: CustomRepliesData? = null
    private var autoReplyTextPlaceholder: String? = null
    private var mainAutoReplySwitch: SwitchMaterial? = null
    private var groupReplySwitch: SwitchMaterial? = null
    private var supportedAppsCard: CardView? = null
    private var preferencesManager: PreferencesManager? = null
    private var days = 0
    private var imgMinus: ImageView? = null
    private var imgPlus: ImageView? = null
    private var supportedAppsLayout: LinearLayout? = null
    private var selectAppsLabel: TextView? = null
    private val supportedAppsCheckboxes: MutableList<MaterialCheckBox> = ArrayList()
    private val supportedAppsDummyViews: MutableList<View> = ArrayList()
    private var dailyRepliesText: TextView? = null
    private var totalRepliesText: TextView? = null
    private var aiVsCustomText: TextView? = null
    private var mActivity: Activity? = null
    private lateinit var notificationListenerUtil: NotificationListenerUtil
    private lateinit var notificationListenerPermissionLauncher: ActivityResultLauncher<Intent>
    private lateinit var adView: AdView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        mActivity = activity
        setHasOptionsMenu(true)
        initializeAdView(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        customRepliesData = getInstance(mActivity!!)
        preferencesManager = getPreferencesInstance(mActivity!!)
        notificationListenerUtil = NotificationListenerUtil(mActivity!!)

        // Initialize the ActivityResultLauncher
        notificationListenerPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val granted = notificationListenerUtil.isNotificationServiceEnabled()
                handleNotificationListenerPermissionResult(granted)
            }

        if (!notificationListenerUtil.isNotificationServiceEnabled()) showPermissionsDialog()

        // Assign Views
        mainAutoReplySwitch = binding.mainAutoReplySwitch
        groupReplySwitch = binding.groupReplySwitch
        replyOptionsCard = binding.replyOptionsCardView
        customTextOptionCard = binding.customTextOptionCard
        smartRepliesOptionCard = binding.smartRepliesOptionCard
        customTextPreview = binding.customTextPreview
        supportedAppsLayout = binding.supportedPlatformsLayout
        supportedAppsCard = binding.supportedAppsSelectorCardView
        selectAppsLabel = binding.selectAppsLabel
        autoReplyTextPlaceholder = resources.getString(R.string.mainAutoReplyTextPlaceholder)
        timePickerCard = binding.replyFrequencyTimePickerCardView
        timePickerSubTitleTextPreview = binding.timePickerSubTitle
        timeSelectedTextPreview = binding.timeSelectedText
        imgMinus = binding.imgMinus
        imgPlus = binding.imgPlus
        dailyRepliesText = binding.dailyRepliesCount
        totalRepliesText = binding.totalRepliesCount
        aiVsCustomText = binding.aiVsCustomCount
        handleReplyOptionsCard()

        customTextPreview?.text = customRepliesData!!.getTextToSendOrElse(autoReplyTextPlaceholder)

        groupReplySwitch?.isEnabled = mainAutoReplySwitch!!.isChecked


        // Handling of components
        handleMainAutoReplySwitch()
        handleGroupReplySwitch()
        handleReplyFrequency()

        setNumDays()
        createSupportedAppCheckboxes()
    }

    private fun initializeAdView(context: Context) {
        // Initialize the MobileAds SDK
        MobileAds.initialize(context)
        adView = binding.adView

        // Load the ad
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun handleReplyOptionsCard() {
        customTextOptionCard?.setOnClickListener { v: View -> openCustomReplyEditorActivity(v) }
        smartRepliesOptionCard?.setOnClickListener {
            val intent = Intent(requireContext(), com.matrix.autoreply.ui.activity.AiSettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleMainAutoReplySwitch() {
        mainAutoReplySwitch?.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked && !notificationListenerUtil.isNotificationServiceEnabled()) {
                Toast.makeText(mActivity, Constants.RESTART_SERVICE_TOAST, Toast.LENGTH_LONG).show()
            } else {
                preferencesManager!!.setAutoReplyPref(isChecked)
                mainAutoReplySwitch!!.setText(
                    if (isChecked) R.string.mainAutoReplySwitchOnLabel else R.string.mainAutoReplySwitchOffLabel
                )
                setSwitchState()

                groupReplySwitch!!.isEnabled = isChecked
            }
        }
    }

    private fun handleGroupReplySwitch() {
        groupReplySwitch?.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->

            if (preferencesManager!!.isGroupReplyEnabled == isChecked) {
                return@setOnCheckedChangeListener
            }
            if (isChecked) {
                Toast.makeText(mActivity, R.string.group_reply_on_info_message, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(mActivity, R.string.group_reply_off_info_message, Toast.LENGTH_SHORT).show()
            }
            preferencesManager!!.setGroupReplyPref(isChecked)
        }
    }

    private fun handleReplyFrequency() {
        imgMinus?.setOnClickListener {
            if (days > MIN_DAYS) {
                days--
                saveNumDays()
            }
        }

        imgPlus?.setOnClickListener {
            if (days < MAX_DAYS) {
                days++
                saveNumDays()
            }
        }
    }

    private fun enableOrDisableEnabledAppsCheckboxes(enabled: Boolean) {
        selectAppsLabel?.alpha = if (enabled) 1.0f else 0.5f
        for (checkbox in supportedAppsCheckboxes) {
            checkbox.isEnabled = enabled
        }
        for (dummyView in supportedAppsDummyViews) {
            dummyView.visibility = if (enabled) View.GONE else View.VISIBLE
        }
    }

    private fun createSupportedAppCheckboxes() {
        supportedAppsLayout!!.removeAllViews()

        //inflate the views
        val inflater = layoutInflater
        for (supportedApp in Constants.SUPPORTED_APPS) {
            val view = inflater.inflate(R.layout.enable_app_main_layout, null)
            val checkBox = view.findViewById<MaterialCheckBox>(R.id.platform_checkbox)
            checkBox.text = supportedApp.name
            checkBox.tag = supportedApp
            checkBox.isChecked = preferencesManager!!.isAppEnabled(supportedApp)
            checkBox.isEnabled = mainAutoReplySwitch!!.isChecked
            checkBox.setOnCheckedChangeListener(supportedAppsCheckboxListener)
            supportedAppsCheckboxes.add(checkBox)
            val platformDummyView = view.findViewById<View>(R.id.platform_dummy_view)
            if (mainAutoReplySwitch!!.isChecked) {
                platformDummyView.visibility = View.GONE
            }
            platformDummyView.setOnClickListener { v: View? ->
                if (!mainAutoReplySwitch!!.isChecked) {
                    Toast.makeText(
                        mActivity,
                        resources.getString(R.string.enable_auto_reply_switch_msg),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            supportedAppsDummyViews.add(platformDummyView)
            supportedAppsLayout!!.addView(view)
        }
    }

    private val supportedAppsCheckboxListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        if (!isChecked && preferencesManager!!.enabledApps.size <= 1) { // Keep at-least one app selected
            Toast.makeText(
                mActivity,
                resources.getString(R.string.error_atleast_single_app_must_be_selected),
                Toast.LENGTH_SHORT
            ).show()
            buttonView.isChecked = true
        } else {
            preferencesManager!!.saveEnabledApps((buttonView.tag as App), isChecked)
        }
    }

    private fun saveNumDays() {
        preferencesManager!!.autoReplyDelay = (days * 24 * 60 * 60 * 1000).toLong() //Save in Milliseconds
        setNumDays()
    }

    private fun setNumDays() {
        val timeDelay = preferencesManager!!.autoReplyDelay / (60 * 1000) //convert back to minutes
        days = timeDelay.toInt() / (60 * 24) //convert back to days
        if (days == 0) {
            timeSelectedTextPreview!!.text = "•"
            timePickerSubTitleTextPreview!!.setText(R.string.time_picker_sub_title_default)
        } else {
            timeSelectedTextPreview!!.text = "" + days
            timePickerSubTitleTextPreview!!.text =
                String.format(resources.getString(R.string.time_picker_sub_title), days)
        }
    }

    override fun onResume() {
        super.onResume()
        //If user directly goes to Settings and removes notifications permission
        //when app is launched check for permission and set appropriate app state
        if (!notificationListenerUtil.isNotificationServiceEnabled()) {
            preferencesManager!!.setAutoReplyPref(false)
        }
        setSwitchState()

        // set group chat switch state
        groupReplySwitch!!.isChecked = preferencesManager!!.isGroupReplyEnabled

        // Set user auto reply text
        customTextPreview!!.text = customRepliesData!!.getTextToSendOrElse(autoReplyTextPlaceholder)
        
        // Update analytics
        updateAnalytics()
    }
    
    private fun updateAnalytics() {
        dailyRepliesText?.text = preferencesManager?.getDailyReplyCount().toString()
        totalRepliesText?.text = preferencesManager?.getTotalReplyCount().toString()
        val aiCount = preferencesManager?.getAiReplyCount() ?: 0
        val customCount = preferencesManager?.getCustomReplyCount() ?: 0
        aiVsCustomText?.text = "$aiCount / $customCount"
    }

    private fun setSwitchState() {
        mainAutoReplySwitch!!.isChecked = preferencesManager!!.isAutoReplyEnabled
        groupReplySwitch!!.isEnabled = preferencesManager!!.isAutoReplyEnabled
        enableOrDisableEnabledAppsCheckboxes(mainAutoReplySwitch!!.isChecked)
    }

    private fun openCustomReplyEditorActivity(v: View) {
        val intent = Intent(mActivity, CustomReplyEditorActivity::class.java)
        startActivity(intent)
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
                //Decline
                setSwitchState()
            } else {
                //Accept
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
