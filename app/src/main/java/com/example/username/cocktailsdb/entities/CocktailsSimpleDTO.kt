package com.example.username.cocktailsdb.entities

import com.google.gson.annotations.SerializedName

data class CocktailsSimpleDTO(@SerializedName("drinks") val cocktails: List<CocktailSimpleDTO>)
