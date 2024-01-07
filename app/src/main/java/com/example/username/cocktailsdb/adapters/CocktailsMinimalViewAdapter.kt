package com.example.username.cocktailsdb.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.username.cocktailsdb.R
import com.example.username.cocktailsdb.databinding.ItemDrinkMinimalViewBinding
import com.example.username.cocktailsdb.entities.CocktailSimpleDTO

class CocktailsMinimalViewAdapter (private var listCocktails: List<CocktailSimpleDTO>, private val context: Context, private val onClick: (CocktailSimpleDTO) -> Unit): RecyclerView.Adapter<CocktailsMinimalViewAdapter.CocktailsMinimalViewHolder>() {


    inner class CocktailsMinimalViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val binding = ItemDrinkMinimalViewBinding.bind(view)

        fun bind(c: CocktailSimpleDTO, context: Context, onClick: (CocktailSimpleDTO) -> Unit) {
            Glide.with(context).load(c.strDrinkThumb).placeholder(R.drawable.placeholder_60_x_60).into(binding.ivCocktail)
            binding.tvCocktail.text = c.strDrink
            binding.cvCocktail.setOnClickListener { onClick(c) }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CocktailsMinimalViewHolder {
        return CocktailsMinimalViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_drink_minimal_view, parent, false))
    }

    override fun getItemCount(): Int {
        return listCocktails.size
    }

    override fun onBindViewHolder(holder: CocktailsMinimalViewHolder, position: Int) {
        holder.bind(listCocktails[position], context, onClick)
    }

}