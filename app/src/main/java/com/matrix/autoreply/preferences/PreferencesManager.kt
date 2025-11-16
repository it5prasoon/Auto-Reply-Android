package com.matrix.autoreply.preferences

import android.content.Context
import android.content.SharedPreferences
import com.matrix.autoreply.R
import com.matrix.autoreply.model.App
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import android.content.pm.PackageManager
import androidx.preference.PreferenceManager
import com.matrix.autoreply.constants.Constants
import java.util.*

class PreferencesManager private constructor(private val thisAppContext: Context) {

    private val KEY_SERVICE_ENABLED = "pref_service_enabled"
    private val KEY_AUTO_REPLY_ENABLED = "pref_auto_reply_enabled"
    private val KEY_MESSAGE_LOGS_ENABLED = "pref_message_logs_enabled"
    private val KEY_GROUP_REPLY_ENABLED = "pref_group_reply_enabled"
    private val KEY_AUTO_REPLY_THROTTLE_TIME_MS = "pref_auto_reply_throttle_time_ms"
    private val KEY_SELECTED_APPS_ARR = "pref_selected_apps_arr"
    private val KEY_IS_APPEND_AUTOREPLY_ATTRIBUTION = "pref_is_append_watomatic_attribution"
    private val KEY_PURGE_MESSAGE_LOGS_LAST_TIME = "pref_purge_message_logs_last_time"
    private val KEY_PLAY_STORE_RATING_STATUS = "pref_play_store_rating_status"
    private var KEY_IS_SHOW_NOTIFICATIONS_ENABLED: String? = null
    private var KEY_SELECTED_APP_LANGUAGE: String? = null
    private val _sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(thisAppContext)


    private fun init() {
        KEY_SELECTED_APP_LANGUAGE = thisAppContext.getString(R.string.key_pref_app_language)
        KEY_IS_SHOW_NOTIFICATIONS_ENABLED = thisAppContext.getString(R.string.pref_show_notification_replied_msg)

        val newInstall = (!_sharedPrefs.contains(KEY_SERVICE_ENABLED)
                && !_sharedPrefs.contains(KEY_SELECTED_APPS_ARR))
        if (newInstall) {
            setAppsAsEnabled(Constants.SUPPORTED_APPS)

            setShowNotificationPref(true)
        }
        if (isFirstInstall(thisAppContext)) {
            if (!_sharedPrefs.contains(KEY_IS_APPEND_AUTOREPLY_ATTRIBUTION)) {
                setAppendAutoreplyAttribution(true)
            }
        } else {
            updateLegacyLanguageKey()
        }
    }

    val isServiceEnabled: Boolean
        get() = _sharedPrefs.getBoolean(KEY_SERVICE_ENABLED, false)

    fun setServicePref(enabled: Boolean) {
        val editor = _sharedPrefs.edit()
        editor.putBoolean(KEY_SERVICE_ENABLED, enabled)
        editor.apply()
    }

    val isAutoReplyEnabled: Boolean
        get() = _sharedPrefs.getBoolean(KEY_AUTO_REPLY_ENABLED, false)

    fun setAutoReplyPref(enabled: Boolean) {
        val editor = _sharedPrefs.edit()
        editor.putBoolean(KEY_AUTO_REPLY_ENABLED, enabled)
        editor.apply()
    }

    val isMessageLogsEnabled: Boolean
        get() = _sharedPrefs.getBoolean(KEY_MESSAGE_LOGS_ENABLED, false)

    fun setMessageLogsPref(enabled: Boolean) {
        val editor = _sharedPrefs.edit()
        editor.putBoolean(KEY_MESSAGE_LOGS_ENABLED, enabled)
        editor.apply()
    }

    val isGroupReplyEnabled: Boolean
        get() = _sharedPrefs.getBoolean(KEY_GROUP_REPLY_ENABLED, false)

    fun setGroupReplyPref(enabled: Boolean) {
        val editor = _sharedPrefs.edit()
        editor.putBoolean(KEY_GROUP_REPLY_ENABLED, enabled)
        editor.apply()
    }

    var autoReplyDelay: Long
        get() = _sharedPrefs.getLong(KEY_AUTO_REPLY_THROTTLE_TIME_MS, 0)
        set(delay) {
            val editor = _sharedPrefs.edit()
            editor.putLong(KEY_AUTO_REPLY_THROTTLE_TIME_MS, delay)
            editor.apply()
        }


    val enabledApps: MutableSet<String>
        get() {
            var enabledAppsJsonStr = _sharedPrefs.getString(KEY_SELECTED_APPS_ARR, null)

            if (enabledAppsJsonStr == null || enabledAppsJsonStr == "[]") {
                enabledAppsJsonStr = setAppsAsEnabled(mutableListOf(App("WhatsApp", "com.whatsapp")))
            }
            val type = object : TypeToken<Set<String?>?>() {}.type
            return Gson().fromJson(enabledAppsJsonStr, type)
        }

    fun isAppEnabled(thisApp: App): Boolean {
        return enabledApps.contains(thisApp.packageName)
    }

    private fun serializeAndSetEnabledPackageList(packageList: Collection<String>): String {
        val jsonStr = Gson().toJson(packageList)
        val editor = _sharedPrefs.edit()
        editor.putString(KEY_SELECTED_APPS_ARR, jsonStr)
        editor.apply()
        return jsonStr
    }

    fun setAppsAsEnabled(apps: List<App>): String {
        val packageNames: MutableSet<String> = HashSet()
        for ((_, packageName) in apps) {
            packageNames.add(packageName)
        }
        return serializeAndSetEnabledPackageList(packageNames)
    }

    fun saveEnabledApps(app: App, isSelected: Boolean): String {
        val enabledPackages = enabledApps
        if (!isSelected) {
            //remove the given platform
            enabledPackages.remove(app.packageName)
        } else {
            //add the given platform
            enabledPackages.add(app.packageName)
        }
        return serializeAndSetEnabledPackageList(enabledPackages)
    }

    fun setAppendAutoreplyAttribution(enabled: Boolean) {
        val editor = _sharedPrefs.edit()
        editor.putBoolean(KEY_IS_APPEND_AUTOREPLY_ATTRIBUTION, enabled)
        editor.apply()
    }

    val isAppendAutoreplyAttributionEnabled: Boolean
        get() = _sharedPrefs.getBoolean(KEY_IS_APPEND_AUTOREPLY_ATTRIBUTION, false)

    fun getSelectedLanguageStr(defaultLangStr: String?): String? {
        return _sharedPrefs.getString(KEY_SELECTED_APP_LANGUAGE, defaultLangStr)
    }

    fun setLanguageStr(languageStr: String?) {
        val editor = _sharedPrefs.edit()
        editor.putString(KEY_SELECTED_APP_LANGUAGE, languageStr)
        editor.apply()
    }

    val selectedLocale: Locale
        get() {
            val thisLangStr = getSelectedLanguageStr(null)
            if (thisLangStr == null || thisLangStr.isEmpty()) {
                return Locale.getDefault()
            }
            val languageSplit = thisLangStr.split("-").toTypedArray()
            return if (languageSplit.size == 2) Locale(languageSplit[0], languageSplit[1]) else Locale(
                languageSplit[0]
            )
        }

    fun updateLegacyLanguageKey() {
        val thisLangStr = getSelectedLanguageStr(null)
        if (thisLangStr == null || thisLangStr.isEmpty()) {
            return
        }
        val languageSplit = thisLangStr.split("-").toTypedArray()
        if (languageSplit.size == 2) {
            if (languageSplit[1].length == 3) {
                val newLangStr = thisLangStr.replace("-r", "-")
                setLanguageStr(newLangStr)
            }
        }
    }

    val isShowNotificationEnabled: Boolean
        get() = _sharedPrefs.getBoolean(KEY_IS_SHOW_NOTIFICATIONS_ENABLED, false)

    fun setShowNotificationPref(enabled: Boolean) {
        val editor = _sharedPrefs.edit()
        editor.putBoolean(KEY_IS_SHOW_NOTIFICATIONS_ENABLED, enabled)
        editor.apply()
    }

    val lastPurgedTime: Long
        get() = _sharedPrefs.getLong(KEY_PURGE_MESSAGE_LOGS_LAST_TIME, 0)

    fun setPurgeMessageTime(purgeMessageTime: Long) {
        val editor = _sharedPrefs.edit()
        editor.putLong(KEY_PURGE_MESSAGE_LOGS_LAST_TIME, purgeMessageTime)
        editor.apply()
    }

    fun setPlayStoreRatingStatus(status: String?) {
        val editor = _sharedPrefs.edit()
        editor.putString(KEY_PLAY_STORE_RATING_STATUS, status)
        editor.apply()
    }

    // AI Integration preferences
    private val KEY_AI_ENABLED = "pref_ai_enabled"
    private val KEY_AI_PROVIDER = "pref_ai_provider"
    private val KEY_AI_API_KEY = "pref_ai_api_key"
    private val KEY_AI_SELECTED_MODEL = "pref_ai_selected_model"
    private val KEY_AI_LAST_ERROR_MESSAGE = "pref_ai_last_error_message"
    private val KEY_AI_LAST_ERROR_TIMESTAMP = "pref_ai_last_error_timestamp"
    private val KEY_AI_SYSTEM_MESSAGE = "pref_ai_system_message"
    private val KEY_AI_PROMPT_TEMPLATE_ID = "pref_ai_prompt_template_id"
    
    var isAiEnabled: Boolean
        get() = _sharedPrefs.getBoolean(KEY_AI_ENABLED, false)
        set(enabled) {
            val editor = _sharedPrefs.edit()
            editor.putBoolean(KEY_AI_ENABLED, enabled)
            editor.apply()
        }
    
    var aiApiKey: String?
        get() = _sharedPrefs.getString(KEY_AI_API_KEY, null)
        set(apiKey) {
            val editor = _sharedPrefs.edit()
            editor.putString(KEY_AI_API_KEY, apiKey)
            editor.apply()
        }
    
    var aiProvider: String
        get() = _sharedPrefs.getString(KEY_AI_PROVIDER, "groq") ?: "groq"
        set(provider) {
            val editor = _sharedPrefs.edit()
            editor.putString(KEY_AI_PROVIDER, provider)
            editor.apply()
        }
    
    var aiSelectedModel: String
        get() = _sharedPrefs.getString(KEY_AI_SELECTED_MODEL, getDefaultModel()) ?: getDefaultModel()
        set(model) {
            val editor = _sharedPrefs.edit()
            editor.putString(KEY_AI_SELECTED_MODEL, model)
            editor.apply()
        }
    
    private fun getDefaultModel(): String {
        return when (aiProvider) {
            "groq" -> "llama-3.1-70b-versatile"
            "openai" -> "gpt-3.5-turbo"
            else -> "llama-3.1-70b-versatile"
        }
    }
    
    fun saveAiLastError(message: String, timestamp: Long) {
        val editor = _sharedPrefs.edit()
        editor.putString(KEY_AI_LAST_ERROR_MESSAGE, message)
        editor.putLong(KEY_AI_LAST_ERROR_TIMESTAMP, timestamp)
        editor.apply()
    }
    
    val aiLastErrorMessage: String?
        get() = _sharedPrefs.getString(KEY_AI_LAST_ERROR_MESSAGE, null)
    
    val aiLastErrorTimestamp: Long
        get() = _sharedPrefs.getLong(KEY_AI_LAST_ERROR_TIMESTAMP, 0L)
    
    fun clearAiLastError() {
        val editor = _sharedPrefs.edit()
        editor.remove(KEY_AI_LAST_ERROR_MESSAGE)
        editor.remove(KEY_AI_LAST_ERROR_TIMESTAMP)
        editor.apply()
    }
    
    var aiSystemMessage: String
        get() = _sharedPrefs.getString(KEY_AI_SYSTEM_MESSAGE, "You are a helpful assistant. Keep your replies concise and friendly.") ?: "You are a helpful assistant. Keep your replies concise and friendly."
        set(message) {
            val editor = _sharedPrefs.edit()
            editor.putString(KEY_AI_SYSTEM_MESSAGE, message)
            editor.apply()
        }
    
    var aiPromptTemplateId: String?
        get() = _sharedPrefs.getString(KEY_AI_PROMPT_TEMPLATE_ID, "friendly")
        set(templateId) {
            val editor = _sharedPrefs.edit()
            editor.putString(KEY_AI_PROMPT_TEMPLATE_ID, templateId)
            editor.apply()
        }

    companion object {
        private var _instance: PreferencesManager? = null
        @JvmStatic
        fun getPreferencesInstance(context: Context): PreferencesManager? {
            if (_instance == null) {
                _instance = PreferencesManager(context.applicationContext)
            }
            return _instance
        }

        /**
         * Check if it is first install on this device.
         * ref: https://stackoverflow.com/a/34194960
         * @param context
         * @return true if first install or else false if it is installed from an update
         */
        fun isFirstInstall(context: Context): Boolean {
            return try {
                val firstInstallTime = context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
                val lastUpdateTime = context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
                firstInstallTime == lastUpdateTime
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                true
            }
        }
    }

    init {
        init()
    }
}
