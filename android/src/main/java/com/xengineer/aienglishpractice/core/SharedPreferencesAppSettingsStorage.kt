package com.xengineer.aienglishpractice.core

import android.content.Context

class SharedPreferencesAppSettingsStorage(context: Context) : AppSettingsStorage {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun read(key: String): String? = preferences.getString(key, null)

    override fun write(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "app_settings"
    }
}
