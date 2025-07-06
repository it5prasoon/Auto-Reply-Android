package com.matrix.autoreply.ui.fragment

import android.os.Bundle
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.matrix.autoreply.R
import com.matrix.autoreply.network.model.ai.AiModel
import com.matrix.autoreply.preferences.PreferencesManager
import com.matrix.autoreply.utils.AiHelper
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.AdView

class AiSettingsFragment : PreferenceFragmentCompat() {

    private var aiModelPreference: ListPreference? = null
    private var aiProviderPreference: ListPreference? = null
    private var aiStatusPreference: Preference? = null
    private var preferencesManager: PreferencesManager? = null
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.fragment_ai_settings, rootKey)
        
        preferencesManager = PreferencesManager.getPreferencesInstance(requireActivity())
        
        setupAiEnabledPreference()
        setupProviderPreference()
        setupApiKeyPreference()
        setupModelPreference()
        setupGetApiKeyPreference()
        setupStatusPreference()
        loadInterstitialAd()
        setupBannerAd()
    }
    
    private fun setupAiEnabledPreference() {
        val aiEnabledPref = findPreference<SwitchPreferenceCompat>("pref_ai_enabled")
        aiEnabledPref?.let { pref ->
            pref.isChecked = preferencesManager?.isAiEnabled ?: false
            pref.setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                preferencesManager?.isAiEnabled = enabled
                if (enabled) {
                    loadAiModels()
                    showInterstitialAd()
                }
                updateStatusDisplay()
                true
            }
        }
    }
    
    private fun setupProviderPreference() {
        aiProviderPreference = findPreference("pref_ai_provider")
        aiProviderPreference?.let { pref ->
            pref.value = preferencesManager?.aiProvider ?: "groq"
            pref.setOnPreferenceChangeListener { _, newValue ->
                val provider = newValue as String
                preferencesManager?.aiProvider = provider
                
                // Reset model selection when provider changes
                preferencesManager?.aiSelectedModel = when (provider) {
                    "groq" -> "llama-3.1-70b-versatile"
                    "openai" -> "gpt-3.5-turbo"
                    else -> "llama-3.1-70b-versatile"
                }
                
                // Reload models for new provider
                AiHelper.invalidateCache()
                loadAiModels()
                
                // Clear previous errors
                preferencesManager?.clearAiLastError()
                updateStatusDisplay()
                true
            }
        }
    }
    
    private fun setupApiKeyPreference() {
        val apiKeyPref = findPreference<EditTextPreference>("pref_ai_api_key")
        apiKeyPref?.let { pref ->
            updateApiKeySummary(pref, preferencesManager?.aiApiKey)
            pref.setOnPreferenceChangeListener { _, newValue ->
                val newApiKey = newValue as String
                preferencesManager?.aiApiKey = newApiKey
                updateApiKeySummary(pref, newApiKey)
                
                // Invalidate cache and reload models
                AiHelper.invalidateCache()
                loadAiModels()
                
                // Clear previous errors
                preferencesManager?.clearAiLastError()
                updateStatusDisplay()
                true
            }
        }
    }
    
    private fun setupModelPreference() {
        aiModelPreference = findPreference("pref_ai_model")
        aiModelPreference?.let { pref ->
            pref.summaryProvider = null
            pref.summary = getString(R.string.pref_ai_model_loading)
            pref.isEnabled = false
            pref.setOnPreferenceChangeListener { _, newValue ->
                val modelId = newValue as String
                preferencesManager?.aiSelectedModel = modelId
                
                // Clear previous errors
                preferencesManager?.clearAiLastError()
                updateStatusDisplay()
                true
            }
            loadAiModels()
        }
    }
    
    private fun setupGetApiKeyPreference() {
        val getApiKeyPref = findPreference<Preference>("pref_ai_get_api_key")
        getApiKeyPref?.setOnPreferenceClickListener {
            val provider = preferencesManager?.aiProvider ?: "groq"
            showApiKeyGuide(provider)
            true
        }
    }
    
    private fun showApiKeyGuide(provider: String) {
        val url = when (provider) {
            "groq" -> "https://console.groq.com/keys"
            "openai" -> "https://platform.openai.com/api-keys"
            else -> "https://console.groq.com/keys"
        }
        
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
        startActivity(intent)
        
        val message = when (provider) {
            "groq" -> "Opening Groq Console. Steps:\n1. Sign up/Login\n2. Go to API Keys\n3. Create New Key\n4. Copy and paste in app"
            "openai" -> "Opening OpenAI Platform. Steps:\n1. Sign up/Login\n2. Go to API Keys\n3. Create New Secret Key\n4. Copy and paste in app"
            else -> "Follow the website instructions to create your API key"
        }
        
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
    }
    
    private fun setupStatusPreference() {
        aiStatusPreference = findPreference("pref_ai_status_display")
        aiStatusPreference?.setOnPreferenceClickListener {
            val lastError = preferencesManager?.aiLastErrorMessage
            if (!lastError.isNullOrEmpty()) {
                preferencesManager?.clearAiLastError()
                updateStatusDisplay()
                Toast.makeText(requireActivity(), getString(R.string.pref_ai_error_dismiss), Toast.LENGTH_SHORT).show()
            }
            true
        }
        updateStatusDisplay()
    }
    
    private fun loadAiModels() {
        val pref = aiModelPreference ?: return
        val prefManager = preferencesManager ?: return
        
        pref.summaryProvider = null
        pref.summary = getString(R.string.pref_ai_model_loading)
        pref.isEnabled = false
        
        if (!prefManager.isAiEnabled || prefManager.aiApiKey.isNullOrEmpty()) {
            pref.summaryProvider = null
            pref.summary = getString(R.string.pref_ai_model_summary_default)
            pref.isEnabled = false
            pref.entries = arrayOf()
            pref.entryValues = arrayOf()
            return
        }
        
        AiHelper.fetchModels(requireActivity(), object : AiHelper.ModelFetchCallback {
            override fun onModelsFetched(models: List<AiModel>) {
                if (activity == null) return
                
                val entries = mutableListOf<CharSequence>()
                val entryValues = mutableListOf<CharSequence>()
                var foundSelected = false
                val selectedModelId = prefManager.aiSelectedModel
                
                // Filter models based on provider
                val provider = prefManager.aiProvider
                val filteredModels = when (provider) {
                    "groq" -> models.filter { it.id.contains("llama") || it.id.contains("mixtral") || it.id.contains("gemma") }
                    "openai" -> models.filter { it.id.contains("gpt") }
                    else -> models.filter { it.id.contains("llama") || it.id.contains("mixtral") }
                }
                
                filteredModels.forEach { model ->
                    entries.add(model.id)
                    entryValues.add(model.id)
                    if (model.id == selectedModelId) {
                        foundSelected = true
                    }
                }
                
                if (entries.isEmpty()) {
                    // Fallback to all models if no filtered models found
                    models.forEach { model ->
                        entries.add(model.id)
                        entryValues.add(model.id)
                        if (model.id == selectedModelId) {
                            foundSelected = true
                        }
                    }
                }
                
                if (entries.isNotEmpty()) {
                    pref.entries = entries.toTypedArray()
                    pref.entryValues = entryValues.toTypedArray()
                    
                    val valueToSet = if (foundSelected) selectedModelId else entryValues[0].toString()
                    pref.value = valueToSet
                    prefManager.aiSelectedModel = valueToSet
                    
                    pref.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
                    pref.isEnabled = true
                } else {
                    pref.summaryProvider = null
                    pref.summary = getString(R.string.pref_ai_model_error)
                    pref.isEnabled = false
                }
            }
            
            override fun onError(errorMessage: String) {
                if (activity == null) return
                pref.summaryProvider = null
                pref.summary = errorMessage
                pref.isEnabled = false
                pref.entries = arrayOf()
                pref.entryValues = arrayOf()
            }
        })
    }
    
    private fun updateApiKeySummary(preference: EditTextPreference, keyValue: String?) {
        preference.summary = if (keyValue.isNullOrEmpty()) {
            getString(R.string.pref_ai_api_key_summary_not_set)
        } else {
            getString(R.string.pref_ai_api_key_summary_set)
        }
    }
    
    private fun updateStatusDisplay() {
        val statusPref = aiStatusPreference ?: return
        val prefManager = preferencesManager ?: return
        
        val errorMessage = prefManager.aiLastErrorMessage
        val errorTimestamp = prefManager.aiLastErrorTimestamp
        val sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L
        
        if (!errorMessage.isNullOrEmpty() && 
            (System.currentTimeMillis() - errorTimestamp < sevenDaysInMillis)) {
            statusPref.title = "AI Alert (Click to dismiss)"
            statusPref.summary = errorMessage
            statusPref.isVisible = true
        } else {
            statusPref.title = getString(R.string.pref_ai_status_title)
            statusPref.summary = getString(R.string.pref_ai_status_ok)
            statusPref.isVisible = true
        }
    }
    
    override fun onResume() {
        super.onResume()
        activity?.title = "AI Settings"
        loadAiModels()
        updateStatusDisplay()
    }
    
    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        val adUnitId = getString(R.string.ai_enable_interstitial)
        
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
    
    private fun setupBannerAd() {
        val adView = view?.findViewById<AdView>(R.id.ai_settings_ad_view)
        adView?.let {
            val adRequest = AdRequest.Builder().build()
            it.loadAd(adRequest)
        }
    }
}