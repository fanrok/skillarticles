package ru.skillbranch.skillarticles.data.delegates

import ru.skillbranch.skillarticles.data.local.PrefManager
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PrefDelegate<T>(private val defaultValue: T) {
    private var value: T? = null

    operator fun provideDelegate(
        thisRef: PrefManager,
        prop: KProperty<*>
    ): ReadWriteProperty<PrefManager, T?> {
        val k = prop.name
        return object : ReadWriteProperty<PrefManager, T?> {
            override fun getValue(thisRef: PrefManager, property: KProperty<*>): T? {
                if (value == null) {
                    @Suppress("UNCHECKED_CAST")
                    value = when (defaultValue) {
                        is Int -> thisRef.preferences.getInt(k, defaultValue as Int) as T
                        is Long -> thisRef.preferences.getLong(k, defaultValue as Long) as T
                        is Float -> thisRef.preferences.getFloat(k, defaultValue as Float) as T
                        is String -> thisRef.preferences.getString(k, defaultValue as String) as T
                        is Boolean -> thisRef.preferences.getBoolean(
                            k,
                            defaultValue as Boolean
                        ) as T
                        else -> throw  IllegalArgumentException("Bad type")
                    }
                }
                return value
            }

            override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T?) {
                with(thisRef.preferences.edit()) {
                    when (value) {
                        is Int -> putInt(k, value)
                        is Long -> putLong(k, value)
                        is Float -> putFloat(k, value)
                        is String -> putString(k, value)
                        is Boolean -> putBoolean(k, value)
                        else -> throw  IllegalArgumentException("Bad type")
                    }
                    apply()
                }
                this@PrefDelegate.value = value
            }
        }
    }
}