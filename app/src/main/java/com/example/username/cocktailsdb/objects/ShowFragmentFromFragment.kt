package com.example.username.cocktailsdb.objects

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

object ShowFragmentFromFragment {

    fun showFragment(
        idContainer: Int,
        activity: FragmentActivity,
        fragment: Fragment,
        tag: String,
        typeAlcoholic: String? = null,
        typeGlass: String? = null,
        typeCategory: String? = null,
        idDrink: String? = null,
        idIngredient: String? = null,
        ingredientName: String? = null,
        letter: String? = null
    ) {
        val bundle = Bundle()
        bundle.putInt("idContainer", idContainer)
        if (typeAlcoholic != null) { bundle.putString("Kind", typeAlcoholic) }
        if (typeGlass != null) { bundle.putString("Glass", typeGlass) }
        if (typeCategory != null) { bundle.putString("Category", typeCategory) }
        if (idDrink != null) { bundle.putString("idDrink", idDrink) }
        if (idIngredient != null) { bundle.putInt("idIngredient", Integer.parseInt(idIngredient)) }
        if (ingredientName != null) { bundle.putString("Ingredient", ingredientName) }
        if (letter != null) { bundle.putString("letter", letter) }
        fragment.arguments = bundle
        activity.supportFragmentManager
            .beginTransaction()
            .replace(idContainer, fragment, tag)
            .addToBackStack(tag)
            .commit()

    }
}