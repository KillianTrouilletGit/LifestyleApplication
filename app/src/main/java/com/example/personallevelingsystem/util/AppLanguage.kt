package com.example.personallevelingsystem.util

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList

/** Per-app language through the framework LocaleManager (Android 13+). */
object AppLanguage {
    const val SYSTEM = "system"
    val choices = listOf(SYSTEM, "en", "fr")

    val isSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    fun current(context: Context): String {
        if (!isSupported) return SYSTEM
        val tags = context.getSystemService(LocaleManager::class.java).applicationLocales.toLanguageTags()
        return when {
            tags.startsWith("fr") -> "fr"
            tags.startsWith("en") -> "en"
            else -> SYSTEM
        }
    }

    fun set(context: Context, code: String) {
        if (!isSupported) return
        val manager = context.getSystemService(LocaleManager::class.java)
        manager.applicationLocales =
            if (code == SYSTEM) LocaleList.getEmptyLocaleList() else LocaleList.forLanguageTags(code)
    }
}
