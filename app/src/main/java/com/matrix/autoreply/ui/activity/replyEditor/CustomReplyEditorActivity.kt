package com.matrix.autoreply.ui.activity.replyEditor

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.appcompat.app.ActionBar
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.textfield.TextInputEditText
import com.matrix.autoreply.R
import com.matrix.autoreply.databinding.ActivityCustomReplyEditorBinding
import com.matrix.autoreply.model.CustomRepliesData
import com.matrix.autoreply.preferences.PreferencesManager
import com.matrix.autoreply.ui.activity.BaseActivity


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
    private var mInterstitialAd: InterstitialAd? = null
    private var TAG = "CustomReplyEditorActivity"

    companion object {
        private const val MESSAGE_STRING = "message"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeAd()

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

        handleSaveReply()
    }

    private fun handleSaveReply() {
        // Save button click listener
        saveAutoReplyTextBtn?.setOnClickListener {
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

    private fun showFullAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
        }
    }

    private fun initializeAd() {
        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        val adUnitId = getString(R.string.save_custom_reply_interstitial)

        InterstitialAd.load(this, adUnitId, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adError.message.let { Log.d(TAG, it) }
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Interstitial Ad Loaded")
                    mInterstitialAd = interstitialAd
                    showFullAd()
                    showFullscreenAdCallback()
                }
            })
    }

    private fun showFullscreenAdCallback() {
        mInterstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                // Set the ad reference to null, so you don't show the ad a second time.
                Log.d(TAG, "Ad dismissed fullscreen content.")
                mInterstitialAd = null
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Called when ad fails to show.
                Log.e(TAG, "Ad failed to show fullscreen content.")
                mInterstitialAd = null
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad showed fullscreen content.")
            }
        }
    }

}
