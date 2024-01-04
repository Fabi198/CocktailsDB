package com.example.username.cocktailsdb.entities

import com.google.gson.annotations.SerializedName

data class IngredientDTO (
    @SerializedName("idIngredient") val idIngredient : String?,
    @SerializedName("strIngredient") val strIngredient : String?,
    @SerializedName("strDescription") val strDescription : String?,
    @SerializedName("strType") val strType : String?,
    @SerializedName("strAlcohol") val strAlcohol : String?,
    @SerializedName("strABV") val strABV : String?
        )