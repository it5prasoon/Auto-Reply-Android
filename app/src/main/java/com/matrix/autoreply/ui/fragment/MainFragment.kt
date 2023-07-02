package com.matrix.autoreply.ui.fragment

import com.matrix.autoreply.model.CustomRepliesData.Companion.getInstance
import com.matrix.autoreply.model.preferences.PreferencesManager.Companion.getPreferencesInstance
import androidx.cardview.widget.CardView
import com.matrix.autoreply.model.CustomRepliesData
import com.google.android.material.switchmaterial.SwitchMaterial
import com.matrix.autoreply.model.preferences.PreferencesManager
import com.google.android.material.checkbox.MaterialCheckBox
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.matrix.autoreply.R
import com.matrix.autoreply.services.ForegroundNotificationService
import com.matrix.autoreply.model.App
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.matrix.autoreply.ui.activity.replyEditor.CustomReplyEditorActivity
import com.matrix.autoreply.model.utils.CustomDialog
import android.content.DialogInterface
import android.os.Build
import android.content.pm.PackageManager
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.matrix.autoreply.model.utils.Constants
import com.matrix.autoreply.model.utils.Constants.MAX_DAYS
import com.matrix.autoreply.model.utils.Constants.MIN_DAYS
import java.util.*

class MainFragment : Fragment() {

    private val MINUTE_FACTOR = 60
    var autoReplyTextPreviewCard: CardView? = null
    var timePickerCard: CardView? = null
    var autoReplyTextPreview: TextView? = null
    var timeSelectedTextPreview: TextView? = null
    var timePickerSubTitleTextPreview: TextView? = null
    var customRepliesData: CustomRepliesData? = null
    var autoReplyTextPlaceholder: String? = null
    var mainAutoReplySwitch: SwitchMaterial? = null
    var groupReplySwitch: SwitchMaterial? = null
    var supportedAppsCard: CardView? = null
    private var preferencesManager: PreferencesManager? = null
    private var days = 0
    private var imgMinus: ImageView? = null
    private var imgPlus: ImageView? = null
    private var supportedAppsLayout: LinearLayout? = null
    private val supportedAppsCheckboxes: MutableList<MaterialCheckBox> = ArrayList()
    private val supportedAppsDummyViews: MutableList<View> = ArrayList()
    private var mActivity: Activity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        setHasOptionsMenu(true)
        mActivity = activity
        customRepliesData = getInstance(mActivity!!)
        preferencesManager = getPreferencesInstance(mActivity!!)

        // Assign Views
        mainAutoReplySwitch = view.findViewById(R.id.mainAutoReplySwitch)
        groupReplySwitch = view.findViewById(R.id.groupReplySwitch)
        autoReplyTextPreviewCard = view.findViewById(R.id.mainAutoReplyTextCardView)
        autoReplyTextPreview = view.findViewById(R.id.textView4)
        supportedAppsLayout = view.findViewById(R.id.supportedPlatformsLayout)
        supportedAppsCard = view.findViewById(R.id.supportedAppsSelectorCardView)
        autoReplyTextPlaceholder = resources.getString(R.string.mainAutoReplyTextPlaceholder)
        timePickerCard = view.findViewById(R.id.replyFrequencyTimePickerCardView)
        timePickerSubTitleTextPreview = view.findViewById(R.id.timePickerSubTitle)
        timeSelectedTextPreview = view.findViewById(R.id.timeSelectedText)
        imgMinus = view.findViewById(R.id.imgMinus)
        imgPlus = view.findViewById(R.id.imgPlus)
        autoReplyTextPreviewCard?.setOnClickListener({ v: View -> openCustomReplyEditorActivity(v) })

        autoReplyTextPreview?.text = customRepliesData!!.getTextToSendOrElse(autoReplyTextPlaceholder)

        groupReplySwitch?.isEnabled = mainAutoReplySwitch!!.isChecked

        mainAutoReplySwitch?.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (isChecked && !isListenerEnabled(mActivity, ForegroundNotificationService::class.java)) {
                showPermissionsDialog()
            } else {
                preferencesManager!!.setServicePref(isChecked)
                enableService(isChecked)
                mainAutoReplySwitch!!.setText(
                    if (isChecked) R.string.mainAutoReplySwitchOnLabel else R.string.mainAutoReplySwitchOffLabel
                )
                setSwitchState()

                groupReplySwitch!!.isEnabled = isChecked
            }
        }

        groupReplySwitch?.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->

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

        imgMinus?.setOnClickListener { v: View? ->
            if (days > MIN_DAYS) {
                days--
                saveNumDays()
            }
        }

        imgPlus?.setOnClickListener { v: View? ->
            if (days < MAX_DAYS) {
                days++
                saveNumDays()
            }
        }

        setNumDays()
        createSupportedAppCheckboxes()
        return view
    }

    private fun enableOrDisableEnabledAppsCheckboxes(enabled: Boolean) {
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
        if (!isListenerEnabled(mActivity, ForegroundNotificationService::class.java)) {
            preferencesManager!!.setServicePref(false)
        }
        if (!preferencesManager!!.isServiceEnabled) {
            enableService(false)
        }
        setSwitchState()

        // set group chat switch state
        groupReplySwitch!!.isChecked = preferencesManager!!.isGroupReplyEnabled

        // Set user auto reply text
        autoReplyTextPreview!!.text = customRepliesData!!.getTextToSendOrElse(autoReplyTextPlaceholder)
    }

    private fun setSwitchState() {
        mainAutoReplySwitch!!.isChecked = preferencesManager!!.isServiceEnabled
        groupReplySwitch!!.isEnabled = preferencesManager!!.isServiceEnabled
        enableOrDisableEnabledAppsCheckboxes(mainAutoReplySwitch!!.isChecked)
    }

    //https://stackoverflow.com/questions/20141727/check-if-user-has-granted-notificationlistener-access-to-my-app/28160115
    //TODO: Use in UI to verify if it needs enabling or restarting
    fun isListenerEnabled(context: Context?, notificationListenerCls: Class<*>?): Boolean {
        val cn = ComponentName(requireContext(), notificationListenerCls!!)
        val flat = Settings.Secure.getString(context?.contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(cn.flattenToString())
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
        customDialog.showDialog(bundle, null) { dialog: DialogInterface?, which: Int ->
            if (which == -2) {
                //Decline
                showPermissionDeniedDialog()
            } else {
                //Accept
                launchNotificationAccessSettings()
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        val customDialog = CustomDialog(mActivity!!)
        val bundle = Bundle()
        bundle.putString(Constants.PERMISSION_DIALOG_DENIED_TITLE, getString(R.string.permission_dialog_denied_title))
        bundle.putString(Constants.PERMISSION_DIALOG_DENIED_MSG, getString(R.string.permission_dialog_denied_msg))
        bundle.putBoolean(Constants.PERMISSION_DIALOG_DENIED, true)
        customDialog.showDialog(bundle, null) { dialog: DialogInterface?, which: Int ->
            if (which == -2) {
                //Decline
                setSwitchState()
            } else {
                //Accept
                launchNotificationAccessSettings()
            }
        }
    }

    private fun launchNotificationAccessSettings() {
        enableService(true)
        val NOTIFICATION_LISTENER_SETTINGS: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
        } else {
            "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
        }
        val i = Intent(NOTIFICATION_LISTENER_SETTINGS)
        startActivityForResult(i, REQ_NOTIFICATION_LISTENER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_NOTIFICATION_LISTENER) {
            if (isListenerEnabled(mActivity, ForegroundNotificationService::class.java)) {
                Toast.makeText(mActivity, "Permission Granted", Toast.LENGTH_LONG).show()
                preferencesManager!!.setServicePref(true)
                setSwitchState()
            } else {
                Toast.makeText(mActivity, "Permission Denied", Toast.LENGTH_LONG).show()
                preferencesManager!!.setServicePref(false)
                setSwitchState()
            }
        }
    }

    private fun enableService(enable: Boolean) {
        val packageManager = mActivity!!.packageManager
        val componentName = ComponentName(mActivity!!, ForegroundNotificationService::class.java)
        val settingCode =
            if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        // enable dummyActivity (as it is disabled in the manifest.xml)
        packageManager.setComponentEnabledSetting(componentName, settingCode, PackageManager.DONT_KILL_APP)
    }

    companion object {
        private const val REQ_NOTIFICATION_LISTENER = 100

        //REF: https://stackoverflow.com/questions/37539949/detect-if-an-app-is-installed-from-play-store
        fun isAppInstalledFromStore(context: Context): Boolean {
            // A list with valid installers package name
            val validInstallers: List<String> =
                ArrayList(Arrays.asList("com.android.vending", "com.google.android.feedback"))

            // The package name of the app that has installed your app
            val installer = context.packageManager.getInstallerPackageName(context.packageName)

            // true if your app has been downloaded from Play Store
            return installer != null && validInstallers.contains(installer)
        }
    }
}