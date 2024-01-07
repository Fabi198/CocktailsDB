package com.example.username.cocktailsdb.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.username.cocktailsdb.R
import com.example.username.cocktailsdb.adapters.CocktailsMinimalViewAdapter
import com.example.username.cocktailsdb.databinding.FragmentCocktailsListedBinding
import com.example.username.cocktailsdb.entities.CocktailSimpleDTO
import com.example.username.cocktailsdb.objects.ShowFragmentFromFragment.showFragment
import com.example.username.cocktailsdb.retrofit.RetrofitCocktail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CocktailsListedFragment : Fragment(R.layout.fragment_cocktails_listed) {

    private lateinit var binding: FragmentCocktailsListedBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCocktailsListedBinding.bind(view)
        val idContainer = arguments?.getInt(getString(R.string.idcontainer_tag))
        val cocktailName = arguments?.getString(getString(R.string.cocktailname_tag))
        val typeGlass = arguments?.getString(getString(R.string.glass_tag))
        val typeKind = arguments?.getString(getString(R.string.kind_tag))
        val typeCategory = arguments?.getString(getString(R.string.category_tag))

        if (idContainer != null) {
            if (cocktailName != null) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val responseService = RetrofitCocktail.APICOCKTAILS.getCocktailsSimpleList("search.php?s=$cocktailName")
                    val cocktails = responseService.body()?.cocktails
                    withContext(Dispatchers.Main) {
                        onCocktailsQuery(idContainer, cocktails)
                    }
                }
            } else if (typeGlass != null) {
                binding.tvKindOrGlassOrCategory.text = typeGlass.replace("_", " ")
                binding.tvKindOrGlassOrCategory.visibility = View.VISIBLE
                lifecycleScope.launch(Dispatchers.IO) {
                    val responseService = RetrofitCocktail.APICOCKTAILS.getCocktailsSimpleList("filter.php?g=$typeGlass")
                    val cocktails = responseService.body()?.cocktails
                    withContext(Dispatchers.Main) {
                        onCocktailsQuery(idContainer, cocktails)
                    }
                }
            } else if (typeKind != null) {
                binding.tvKindOrGlassOrCategory.text = typeKind.replace("_", " ")
                binding.tvKindOrGlassOrCategory.visibility = View.VISIBLE
                lifecycleScope.launch(Dispatchers.IO) {
                    val responseService = RetrofitCocktail.APICOCKTAILS.getCocktailsSimpleList("filter.php?a=$typeKind")
                    val cocktails = responseService.body()?.cocktails
                    withContext(Dispatchers.Main) {
                        onCocktailsQuery(idContainer, cocktails)
                    }
                }
            } else if (typeCategory != null) {
                binding.tvKindOrGlassOrCategory.text = typeCategory.replace("_", " ")
                binding.tvKindOrGlassOrCategory.visibility = View.VISIBLE
                lifecycleScope.launch(Dispatchers.IO) {
                    val responseService = RetrofitCocktail.APICOCKTAILS.getCocktailsSimpleList("filter.php?c=$typeCategory")
                    val cocktails = responseService.body()?.cocktails
                    withContext(Dispatchers.Main) {
                        onCocktailsQuery(idContainer, cocktails)
                    }
                }
            }
        }




    }

    private fun onCocktailsQuery(idContainer: Int, cocktails: List<CocktailSimpleDTO>?) {
        binding.pbCocktailList.visibility = View.GONE
        if (cocktails != null) {
            val adapter = CocktailsMinimalViewAdapter(cocktails, requireContext()) {
                showFragment(
                    idContainer,
                    requireActivity(),
                    CocktailFullViewFragment(),
                    getString(R.string.cocktailfullviewfragment_tag),
                    idDrink = it.idDrink
                )
            }
            binding.rvCocktailsByIngredient.adapter = adapter
            binding.rvCocktailsByIngredient.visibility = View.VISIBLE
        } else {
            binding.tvNoResults.visibility = View.VISIBLE
        }
    }

}