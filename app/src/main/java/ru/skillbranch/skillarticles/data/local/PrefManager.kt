package ru.skillbranch.skillarticles.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import ru.skillbranch.skillarticles.App
import ru.skillbranch.skillarticles.data.delegates.PrefDelegate
import ru.skillbranch.skillarticles.data.models.AppSettings

object PrefManager{
    lateinit var  authLiveData :  MutableLiveData<Boolean>

    internal val preferences: SharedPreferences by lazy{
        PreferenceManager.getDefaultSharedPreferences(App.applicationContext())
    }

    fun cleatAll(){
        preferences.edit().clear().apply()
    }

    fun getAppSettings():LiveData<AppSettings>{
        return MutableLiveData(AppSettings())
    }

    fun isAuth(): MutableLiveData<Boolean> = authLiveData


    fun setAuth(auth: Boolean):Unit{
        authLiveData.value = auth
    }
}