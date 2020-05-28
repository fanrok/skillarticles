package ru.skillbranch.skillarticles.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import ru.skillbranch.skillarticles.data.delegates.PrefDelegate

class PrefManager (context: Context){
    val preferences: SharedPreferences by lazy { PreferenceManager(context).sharedPreferences }

    var storedInt by PrefDelegate(false)
    var storedLong by PrefDelegate("data")
    var storedFloat by PrefDelegate(Int.MAX_VALUE)
    var storedString by PrefDelegate(Long.MAX_VALUE)
    var storedBoolean by PrefDelegate(1f)

    fun clearAll(){
        preferences.edit().clear().apply()
    }
}