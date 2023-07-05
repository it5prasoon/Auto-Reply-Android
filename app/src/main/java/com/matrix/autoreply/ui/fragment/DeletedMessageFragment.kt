package com.matrix.autoreply.ui.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.matrix.autoreply.R
import com.matrix.autoreply.constants.Constants
import com.matrix.autoreply.databinding.FragmentDeletedMessageBinding
import com.matrix.autoreply.preferences.PreferencesManager
import com.matrix.autoreply.ui.activity.logsViewer.MsgLogViewerActivity
import com.matrix.autoreply.utils.NotificationListenerUtil


open class DeletedMessageFragment : Fragment() {

    private var _binding: FragmentDeletedMessageBinding? = null
    private val binding get() = _binding!!
    private val checkEmoji = String(Character.toChars(0x2714))
    private var preferencesManager: PreferencesManager? = null
    private var mActivity: Activity? = null
    private lateinit var notificationListenerUtil: NotificationListenerUtil
    private lateinit var adView: AdView

    companion object {
        private const val WHATSAPP = "whatsapp";
        private const val APP = "app";
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDeletedMessageBinding.inflate(inflater, container, false)
        mActivity = activity
        initializeAdView(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        preferencesManager = PreferencesManager.getPreferencesInstance(mActivity!!)
        binding.msgLogStatus.text = getString(R.string.msg_log_status_str, checkEmoji)

        notificationListenerUtil = NotificationListenerUtil(mActivity!!)

        // WhatsApp Message Logs viewer
        binding.viewWaLogBtn.setOnClickListener {
            val intent = Intent(requireActivity(), MsgLogViewerActivity::class.java)
            intent.putExtra(APP, WHATSAPP)
            startActivity(intent)
        }

        handleEnableMessageLogsSwitch()
    }

    override fun onResume() {
        super.onResume()
        //If user directly goes to Settings and removes notifications permission
        //when app is launched check for permission and set appropriate app state
        if (!notificationListenerUtil.isNotificationServiceEnabled()) {
            preferencesManager!!.setMessageLogsPref(false)
        }
        setSwitchState()
    }

    private fun initializeAdView(context: Context) {
        // Initialize the MobileAds SDK
        MobileAds.initialize(context)
        adView = binding.adView

        // Load the ad
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

    }

    private fun handleEnableMessageLogsSwitch() {
        binding.enableMessageLogsSwitch.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (isChecked && !notificationListenerUtil.isNotificationServiceEnabled()) {
                Toast.makeText(mActivity, Constants.RESTART_SERVICE_TOAST, Toast.LENGTH_LONG).show()
            } else {
                preferencesManager!!.setMessageLogsPref(isChecked)
                binding.enableMessageLogsSwitch.setText(
                    if (isChecked) R.string.mainAutoReplySwitchOnLabel else R.string.mainAutoReplySwitchOffLabel
                )
                setSwitchState()
            }
        }
    }

    private fun setSwitchState() {
        binding.enableMessageLogsSwitch.isChecked = preferencesManager!!.isMessageLogsEnabled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
