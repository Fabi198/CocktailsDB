package com.example.username.cocktailsdb.objects

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity

object Preferences {

    fun saveGoogleUserData(sharedPrefs: SharedPreferences, email: String, providerName: String) {
        val prefsEd = sharedPrefs.edit()
        prefsEd.putString("email", email)
        prefsEd.putString("provider", providerName)
        prefsEd.apply()
    }

    fun saveLanguagePreference(sharedPrefs: SharedPreferences, language: String) {
        val editor = sharedPrefs.edit()
        editor.putString("language", language)
        editor.apply()
    }

    fun getLanguagePreference(sharedPrefs: SharedPreferences): String {
        return sharedPrefs.getString("language", "english") ?: "english"
    }

    fun setToastTwoBackShowed(sharedPrefs: SharedPreferences, b: Boolean) {
        val editor = sharedPrefs.edit()
        editor.putBoolean("isToastShown", b)
        editor.apply()
    }

    fun getToastTwoBackShowed(sharedPrefs: SharedPreferences): Boolean {
        return sharedPrefs.getBoolean("isToastShown", false)
    }

    fun isSessionSaved(sharedPrefs: SharedPreferences): Boolean {
        val email = sharedPrefs.getString("email", null)
        val provider = sharedPrefs.getString("provider", null)
        return email != null && provider != null
    }

    fun getFavoritesCocktailsIDs(sharedPrefs: SharedPreferences): Array<String?> {
        return arrayOf(
            sharedPrefs.getString("popDrink1", "11000"),
            sharedPrefs.getString("popDrink2", "11001"),
            sharedPrefs.getString("popDrink3", "11002"),
            sharedPrefs.getString("popDrink4", "11003"),
            sharedPrefs.getString("popDrink5", "11004"),
            sharedPrefs.getString("popDrink6", "11005"),
            sharedPrefs.getString("popDrink7", "11006"),
            sharedPrefs.getString("popDrink8", "11007")
        )
    }

    fun getFavoriteCocktailID(sharedPrefs: SharedPreferences, position: Int): String {
        return when (position) {
            0 -> { sharedPrefs.getString("popDrink1", "11000")!! }
            1 -> { sharedPrefs.getString("popDrink2", "11001")!! }
            2 -> { sharedPrefs.getString("popDrink3", "11002")!! }
            3 -> { sharedPrefs.getString("popDrink4", "11003")!! }
            4 -> { sharedPrefs.getString("popDrink5", "11004")!! }
            5 -> { sharedPrefs.getString("popDrink6", "11005")!! }
            6 -> { sharedPrefs.getString("popDrink7", "11006")!! }
            7 -> { sharedPrefs.getString("popDrink8", "11007")!! }
            else -> { "" }
        }
    }

}