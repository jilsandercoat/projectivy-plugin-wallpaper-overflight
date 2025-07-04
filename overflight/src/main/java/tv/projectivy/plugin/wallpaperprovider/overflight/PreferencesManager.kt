package tv.projectivy.plugin.wallpaperprovider.overflight

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import com.google.gson.reflect.TypeToken

object PreferencesManager {
    lateinit var preferences: SharedPreferences
    const val DEFAULT_MEDIA_SOURCE_URL = "https://raw.githubusercontent.com/jilsandercoat/projectivy-plugin-wallpaper-overflight/refs/heads/main/videos.json"
    const val DEFAULT_CACHE_DURATION_HOURS = 24

    fun init(context: Context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }

    operator fun set(key: String, value: Any?) =
        when (value) {
            is String? -> preferences.edit { it.putString(key, value) }
            is Int -> preferences.edit { it.putInt(key, value) }
            is Boolean -> preferences.edit { it.putBoolean(key, value) }
            is Float -> preferences.edit { it.putFloat(key, value) }
            is Long -> preferences.edit { it.putLong(key, value) }
            else -> throw UnsupportedOperationException("Not yet implemented")
        }

    inline operator fun <reified T : Any> get(
        key: String,
        defaultValue: T? = null
    ): T =
        when (T::class) {
            String::class -> preferences.getString(key, defaultValue as String? ?: "") as T
            Int::class -> preferences.getInt(key, defaultValue as? Int ?: -1) as T
            Boolean::class -> preferences.getBoolean(key, defaultValue as? Boolean == true) as T
            Float::class -> preferences.getFloat(key, defaultValue as? Float ?: -1f) as T
            Long::class -> preferences.getLong(key, defaultValue as? Long ?: -1) as T
            else -> throw UnsupportedOperationException("Not yet implemented")
        }

    var video_4k: Boolean
        get() = PreferencesManager["video-4k", false]
        set(value) { PreferencesManager["video-4k"]=value }

    var video_hdr: Boolean
        get() = PreferencesManager["video-hdr", false]
        set(value) { PreferencesManager["video-hdr"]=value }

    var fallback: Boolean
        get() = PreferencesManager["fallback", true]
        set(value) { PreferencesManager["fallback"]=value }

    var mediaSourceUrl: String
        get() = PreferencesManager["video-source-url", DEFAULT_MEDIA_SOURCE_URL].takeIf { it.isNotEmpty() } ?: DEFAULT_MEDIA_SOURCE_URL
        set(value) { PreferencesManager["video-source-url"]=value }

    var cacheDurationHours: Int
        get() = PreferencesManager["cache-duration-hours", DEFAULT_CACHE_DURATION_HOURS].takeIf { it>= 0 } ?: DEFAULT_CACHE_DURATION_HOURS
        set(value) { PreferencesManager["cache-duration-hours"]=value }

    fun export(): String {
        return Gson().toJson(preferences.all)
    }

    fun import(prefs: String): Boolean {
        val gson = GsonBuilder()
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .create()

        try {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val map = gson.fromJson<Map<String, Any>>(prefs, type)
            val editor = preferences.edit()
            editor.clear()
            map.forEach { (key: String, value: Any) ->
                when(value) {
                    is Boolean -> editor.putBoolean(key, value)
                    is Double -> editor.putFloat(key, value.toFloat())
                    is Float -> editor.putFloat(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putInt(key, value.toInt())
                    is String -> editor.putString(key, value)
                    is ArrayList<*> -> editor.putStringSet(key, java.util.HashSet(value as java.util.ArrayList<String>))
                    is Set<*> -> editor.putStringSet(key, value as Set<String>)
                }
            }
            editor.apply()
        } catch (ex: Exception) {
            ex.printStackTrace()
            return false
        }
        return true
    }
}
