package com.example.username.cocktailsdb.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.username.cocktailsdb.R
import com.example.username.cocktailsdb.databinding.ItemIngredientBinding
import com.example.username.cocktailsdb.entities.IngredientSimpleDTO

class IngredientsMinimalViewAdapter (private val listIngredients: ArrayList<IngredientSimpleDTO>, private val context: Context, private val onClick: (IngredientSimpleDTO) -> Unit): RecyclerView.Adapter<IngredientsMinimalViewAdapter.IngredientsViewHolder>() {

    inner class IngredientsViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val binding = ItemIngredientBinding.bind(view)

        @SuppressLint("SetTextI18n")
        fun bind(i: IngredientSimpleDTO, context: Context, onClick: (IngredientSimpleDTO) -> Unit) {
            Glide.with(context)
                .load(i.strImageSource)
                .into(binding.ivIngredient)
            binding.tvNameIngredient.text = "${i.strIngredient} ${i.strMeasure}"
            binding.cvMain.setOnClickListener { if (i.strIngredient != "Cherry") { onClick(i) } else { Toast.makeText(context, "Ingrediente no disponible", Toast.LENGTH_SHORT).show() } }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientsViewHolder {
        return IngredientsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_ingredient, parent, false))
    }

    override fun getItemCount(): Int {
        return listIngredients.size
    }

    override fun onBindViewHolder(holder: IngredientsViewHolder, position: Int) {
        holder.bind(listIngredients[position], context, onClick)
    }


}