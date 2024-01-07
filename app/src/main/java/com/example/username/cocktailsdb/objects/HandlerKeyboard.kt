package com.example.username.cocktailsdb.objects

import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity

object HandlerKeyboard {

    fun hideKeyboardOnFragment(fragmentActivity: FragmentActivity, view: View) {
        val imm = fragmentActivity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}