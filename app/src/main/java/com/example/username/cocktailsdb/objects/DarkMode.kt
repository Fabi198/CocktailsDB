package com.example.username.cocktailsdb.objects

import android.content.Context
import android.content.res.Configuration

object DarkMode {

    fun isDarkModeEnabled(context: Context): Boolean {
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

}