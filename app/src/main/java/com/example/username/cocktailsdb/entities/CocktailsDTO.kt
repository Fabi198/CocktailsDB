package com.example.username.cocktailsdb.entities

import com.google.gson.annotations.SerializedName

data class CocktailsDTO (@SerializedName("drinks") val cocktails: List<CocktailDTO>)