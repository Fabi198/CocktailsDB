package com.example.username.cocktailsdb.objects

import android.content.SharedPreferences

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

    fun isThereAlreadyOnFavorites(sharedPrefs: SharedPreferences, idDrink: String): Boolean {
        return getFavoritesCocktailsIDs(sharedPrefs).contains(idDrink)
    }

    fun resetFavoriteDrink(sharedPrefs: SharedPreferences, idDrink: String) {
        getFavoritesCocktailsIDs(sharedPrefs).forEachIndexed { index, s ->
            if (idDrink == s) {
                val editor = sharedPrefs.edit()
                editor.remove(when (index) {
                    0 -> "popDrink1"
                    1 -> "popDrink2"
                    2 -> "popDrink3"
                    3 -> "popDrink4"
                    4 -> "popDrink5"
                    5 -> "popDrink6"
                    6 -> "popDrink7"
                    7 -> "popDrink8"
                    else -> ""
                })
                editor.apply()
            }
        }
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

    fun setFavoriteCocktailID(sharedPrefs: SharedPreferences, position: Int, idDrink: String) {
        val popDrink = when (position) {
            0 -> "popDrink1"
            1 -> "popDrink2"
            2 -> "popDrink3"
            3 -> "popDrink4"
            4 -> "popDrink5"
            5 -> "popDrink6"
            6 -> "popDrink7"
            7 -> "popDrink8"
            else -> ""
        }
        val editor = sharedPrefs.edit()
        editor.putString(popDrink, idDrink)
        editor.apply()
    }

    fun restoreFavoriteCocktailsDefault(sharedPrefs: SharedPreferences) {
        val editor = sharedPrefs.edit()
        editor.putString("popDrink1", "11000")
        editor.putString("popDrink2", "11001")
        editor.putString("popDrink3", "11002")
        editor.putString("popDrink4", "11003")
        editor.putString("popDrink5", "11004")
        editor.putString("popDrink6", "11005")
        editor.putString("popDrink7", "11006")
        editor.putString("popDrink8", "11007")
        editor.apply()
    }

}