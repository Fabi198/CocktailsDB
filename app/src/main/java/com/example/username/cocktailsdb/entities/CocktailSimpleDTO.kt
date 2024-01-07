package com.example.username.cocktailsdb.entities

import com.google.gson.annotations.SerializedName

data class CocktailSimpleDTO(
    @SerializedName("idDrink") val idDrink: String?,
    @SerializedName("strDrink") val strDrink: String?,
    @SerializedName("strDrinkThumb") val strDrinkThumb: String?
)
