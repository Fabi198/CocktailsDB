package com.example.username.cocktailsdb.fragments


import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.username.cocktailsdb.R
import com.example.username.cocktailsdb.databinding.FragmentCocktailFullViewBinding


class CocktailFullViewFragment : Fragment(R.layout.fragment_cocktail_full_view) {


    private lateinit var binding: FragmentCocktailFullViewBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCocktailFullViewBinding.bind(view)
        val idContainer = arguments?.getInt(getString(R.string.idcontainer_tag))

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)

        binding.collapsingToolbarLayout.title = " "

    }

}