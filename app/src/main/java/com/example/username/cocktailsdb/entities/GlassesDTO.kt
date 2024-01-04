package com.example.username.cocktailsdb.entities

import com.example.username.cocktailsdb.entities.GlassDTO
import com.google.gson.annotations.SerializedName

data class GlassesDTO (
    @SerializedName("drinks") val drinksDTO: List<GlassDTO>
        )
