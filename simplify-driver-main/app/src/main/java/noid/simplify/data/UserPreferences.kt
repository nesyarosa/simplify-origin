package noid.simplify.data

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(@ApplicationContext context : Context){

    private val sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)

    var authToken: String?
        get() = sharedPreferences.getString(AUTH_TOKEN, null)
        set(value) {
            sharedPreferences.edit {
                putString(AUTH_TOKEN, value)
            }
        }

    var currentUser: String?
        get() = sharedPreferences.getString(CURRENT_USER, null)
        set(value) {
            sharedPreferences.edit {
                putString(CURRENT_USER, value)
            }
        }

    var latitude: String?
        get() = sharedPreferences.getString(LATITUDE, null)
        set(value) {
            sharedPreferences
                .edit()
                .putString(LATITUDE, value)
                .apply()
        }

    var longitude: String?
        get() = sharedPreferences.getString(LONGITUDE, null)
        set(value) {
            sharedPreferences
                .edit()
                .putString(LONGITUDE, value)
                .apply()
        }

    fun clearValues() {
        val editor = sharedPreferences.edit()
        editor?.remove(AUTH_TOKEN)
        editor?.remove(CURRENT_USER)
        editor?.remove(LATITUDE)
        editor?.remove(LONGITUDE)
        editor?.apply()
    }

    companion object {
        private const val SHARED_PREF_NAME = "DB::USER_PREFERENCES"
        private const val AUTH_TOKEN = "AUTH_TOKEN"
        private const val CURRENT_USER = "CURRENT_USER"
        private const val LATITUDE = "user_pref.justgo_driver.LATITUDE"
        private const val LONGITUDE = "user_pref.justgo_driver.LONGITUDE"
    }
}