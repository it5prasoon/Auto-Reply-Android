package com.matrix.autoreply.model

import com.matrix.autoreply.preferences.PreferencesManager
import android.app.Activity
import android.content.Context
import com.matrix.autoreply.R
import org.json.JSONArray
import android.content.SharedPreferences
import android.text.Editable
import org.json.JSONException

/**
 * Manages user entered custom auto reply text data.
 */
class CustomRepliesData {
    private var thisAppContext: Context? = null
    private var preferencesManager: PreferencesManager? = null

    private constructor() {}
    private constructor(context: Context) {
        thisAppContext = context.applicationContext
        _sharedPrefs = context.applicationContext
            .getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE)
        preferencesManager = PreferencesManager.getPreferencesInstance(thisAppContext!!)
        init()
    }

    /**
     * Execute this code when the singleton is first created. All the tasks that needs to be done
     * when the instance is first created goes here. For example, set specific keys based on new install
     * or app upgrade, etc.
     */
    private fun init() {
        // Set default auto reply message on first install
        if (!_sharedPrefs!!.contains(KEY_CUSTOM_REPLY_ALL)) {
            set(thisAppContext!!.getString(R.string.auto_reply_default_message))
        }
    }

    /**
     * Stores given auto reply text to the database and sets it as current
     * @param customReply String that needs to be set as current auto reply
     * @return String that is stored in the database as current custom reply
     */
    fun set(customReply: String?): String? {
        if (!isValidCustomReply(customReply)) {
            return null
        }
        val previousCustomReplies = all
        previousCustomReplies.put(customReply)
        if (previousCustomReplies.length() > MAX_NUM_CUSTOM_REPLY) {
            previousCustomReplies.remove(0)
        }
        val editor = _sharedPrefs!!.edit()
        editor.putString(KEY_CUSTOM_REPLY_ALL, previousCustomReplies.toString())
        editor.apply()
        return customReply
    }

    /**
     * Stores given auto reply text to the database and sets it as current
     * @param customReply Editable that needs to be set as current auto reply
     * @return String that is stored in the database as current custom reply
     */
    fun set(customReply: Editable?): String? {
        return if (customReply != null) set(customReply.toString()) else null
    }

    /**
     * Get last set auto reply text
     * Prefer using [::getOrElse][CustomRepliesData] to avoid `null`
     * @return Auto reply text or `null` if not set
     */
    fun get(): String? {
        val allCustomReplies = all
        try {
            return if (allCustomReplies.length() > 0) allCustomReplies[allCustomReplies.length() - 1] as String else null
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Get last set auto reply text if present or else return {@param defaultText}
     * @param defaultText default auto reply text
     * @return Return auto reply text if present or else return given {@param defaultText}
     */
    fun getOrElse(defaultText: String?): String {
        val currentText = get()
        return currentText ?: defaultText!!
    }

    fun getTextToSendOrElse(defaultTextToSend: String?): String {
        var currentText = get()
        if (preferencesManager!!.isAppendAutoreplyAttributionEnabled) {
            currentText += """
                
                
                ${thisAppContext!!.getString(R.string.sent_using_autoreply)}
                """.trimIndent()
        }
        return currentText ?: defaultTextToSend!!
    }

    private val all: JSONArray
        private get() {
            var allCustomReplies = JSONArray()
            try {
                allCustomReplies = JSONArray(_sharedPrefs!!.getString(KEY_CUSTOM_REPLY_ALL, "[]"))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return allCustomReplies
        }

    companion object {
        const val KEY_CUSTOM_REPLY_ALL = "user_custom_reply_all"
        const val MAX_NUM_CUSTOM_REPLY = 10
        const val MAX_STR_LENGTH_CUSTOM_REPLY = 500
        const val RTL_ALIGN_INVISIBLE_CHAR = " \u200F\u200F\u200E " // https://android.stackexchange.com/a/190024
        private val APP_SHARED_PREFS = CustomRepliesData::class.java.simpleName
        private var _sharedPrefs: SharedPreferences? = null
        private var _INSTANCE: CustomRepliesData? = null
        @JvmStatic
        fun getInstance(context: Context): CustomRepliesData? {
            if (_INSTANCE == null) {
                _INSTANCE = CustomRepliesData(context)
            }
            return _INSTANCE
        }

        fun isValidCustomReply(userInput: String?): Boolean {
            return !userInput.isNullOrEmpty() &&
                    userInput.length <= MAX_STR_LENGTH_CUSTOM_REPLY
        }

        fun isValidCustomReply(userInput: Editable?): Boolean {
            return userInput != null &&
                    isValidCustomReply(userInput.toString())
        }
    }
}