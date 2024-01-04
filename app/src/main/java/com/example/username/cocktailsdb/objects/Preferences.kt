package com.example.username.cocktailsdb.objects

import android.content.SharedPreferences

object Preferences {


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

}