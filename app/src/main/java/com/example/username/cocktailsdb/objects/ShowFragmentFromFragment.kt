package com.example.username.cocktailsdb.objects

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.username.cocktailsdb.R

object ShowFragmentFromFragment {

    fun showFragment(
        idContainer: Int,
        activity: FragmentActivity,
        fragment: Fragment,
        tag: String,
        typeKind: String? = null,
        typeGlass: String? = null,
        typeCategory: String? = null,
        idDrink: String? = null,
        idIngredient: String? = null,
        ingredientName: String? = null,
        cocktailName: String? = null,
        cocktailsSaved: Boolean? = null,
        letter: String?= null
    ) {
        val bundle = Bundle()
        bundle.putInt("idContainer", idContainer)
        if (typeKind != null) { bundle.putString("Kind", typeKind) }
        if (typeGlass != null) { bundle.putString("Glass", typeGlass) }
        if (typeCategory != null) { bundle.putString("Category", typeCategory) }
        if (idDrink != null) { bundle.putString("idDrink", idDrink) }
        if (idIngredient != null) { bundle.putInt("idIngredient", Integer.parseInt(idIngredient)) }
        if (ingredientName != null) { bundle.putString("Ingredient", ingredientName) }
        if (cocktailName != null) { bundle.putString("cocktailName", cocktailName) }
        if (cocktailsSaved != null) { bundle.putBoolean("cocktailsSaved", cocktailsSaved) }
        if (letter != null) { bundle.putString("letter", letter) }
        fragment.arguments = bundle
        activity.supportFragmentManager
            .beginTransaction()
            .replace(idContainer, fragment, tag)
            .addToBackStack(tag)
            .commit()

    }
}