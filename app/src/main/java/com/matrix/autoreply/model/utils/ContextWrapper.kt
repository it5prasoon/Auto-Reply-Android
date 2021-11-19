package com.matrix.autoreply.model.utils

import android.content.Context
import android.os.Build
import android.os.LocaleList
import java.util.*

class ContextWrapper(base: Context?) : android.content.ContextWrapper(base) {
    companion object {
        //REF: https://medium.com/swlh/android-app-specific-language-change-programmatically-using-kotlin-d650a5392220
        @JvmStatic
        fun wrap(context: Context, locale: Locale?): ContextWrapper {
            var context = context
            val res = context.resources
            val configuration = res.configuration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLocale(locale)
                val localeList = LocaleList(locale)
                LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)
                context = context.createConfigurationContext(configuration)
                res.updateConfiguration(configuration, res.displayMetrics)
            } else {
                configuration.locale = locale
                res.updateConfiguration(configuration, res.displayMetrics)
            }
            return ContextWrapper(context)
        }
    }
}