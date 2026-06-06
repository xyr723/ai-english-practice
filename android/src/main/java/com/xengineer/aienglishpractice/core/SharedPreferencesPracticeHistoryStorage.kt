package com.xengineer.aienglishpractice.core

import android.content.Context

class SharedPreferencesPracticeHistoryStorage(context: Context) : PracticeHistoryStorage {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun read(): String? = preferences.getString(KEY_ENTRIES, null)

    override fun write(value: String) {
        preferences.edit().putString(KEY_ENTRIES, value).apply()
    }

    override fun clear() {
        preferences.edit().remove(KEY_ENTRIES).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "practice_history"
        const val KEY_ENTRIES = "entries_payload"
    }
}
