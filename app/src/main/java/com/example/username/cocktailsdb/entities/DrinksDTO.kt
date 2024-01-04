package com.example.username.cocktailsdb.entities

import com.example.username.cocktailsdb.entities.DrinkDTO
import com.google.gson.annotations.SerializedName

data class DrinksDTO (@SerializedName("drinks") val Drinks: List<DrinkDTO>)