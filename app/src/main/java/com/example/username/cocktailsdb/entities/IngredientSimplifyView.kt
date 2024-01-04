package com.example.username.cocktailsdb.entities

import com.google.gson.annotations.SerializedName

data class IngredientSimplifyView (
    @SerializedName("strIngredient") val strIngredient: String?,
    @SerializedName("strMeasure") val strMeasure: String?,
    @SerializedName("strImageSource") val strImageSource: String
        )