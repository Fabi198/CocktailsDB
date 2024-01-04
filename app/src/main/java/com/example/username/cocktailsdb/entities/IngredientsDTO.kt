package com.example.username.cocktailsdb.entities

import com.example.username.cocktailsdb.entities.IngredientDTO
import com.google.gson.annotations.SerializedName

data class IngredientsDTO (
    @SerializedName("ingredients") val ingredient: List<IngredientDTO>
        )