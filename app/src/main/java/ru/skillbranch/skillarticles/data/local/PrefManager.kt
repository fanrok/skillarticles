package ru.skillbranch.skillarticles.data.local

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import ru.skillbranch.skillarticles.App
import ru.skillbranch.skillarticles.data.delegates.PrefDelegate


object PrefManager {
    internal val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(App.appContext())
    }

    var isAuthorized by PrefDelegate(false)

    var isDarkMode by PrefDelegate(false)
    var isBigText by PrefDelegate(false)

    fun clearAll() {
        preferences.edit()
                .clear()
                .apply()
    }
    }