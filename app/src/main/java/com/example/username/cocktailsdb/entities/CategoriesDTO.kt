package com.example.username.cocktailsdb.entities

import com.google.gson.annotations.SerializedName

data class CategoriesDTO (
    @SerializedName("drinks") val drinks: List<CategoryDTO>
        )
