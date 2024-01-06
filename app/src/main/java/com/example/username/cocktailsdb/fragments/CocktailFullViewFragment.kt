package com.example.username.cocktailsdb.fragments


import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.username.cocktailsdb.R
import com.example.username.cocktailsdb.adapters.IngredientsAdapter
import com.example.username.cocktailsdb.databinding.FragmentCocktailFullViewBinding
import com.example.username.cocktailsdb.entities.IngredientSimplifyView
import com.example.username.cocktailsdb.objects.Preferences.getLanguagePreference
import com.example.username.cocktailsdb.retrofit.RetrofitCocktail
import com.example.username.cocktailsdb.translator.TranslateService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CocktailFullViewFragment : Fragment(R.layout.fragment_cocktail_full_view) {


    private lateinit var binding: FragmentCocktailFullViewBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCocktailFullViewBinding.bind(view)
        val idContainer = arguments?.getInt(getString(R.string.idcontainer_tag))
        val idDrink = arguments?.getString(getString(R.string.iddrink_tag))

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)

        binding.collapsingToolbarLayout.title = " "

        setupUI(idContainer, idDrink)
    }

    private fun setupUI(idContainer: Int?, idDrink: String?) {
        if (idContainer != null && idDrink != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                val responseService =
                    RetrofitCocktail.APICOCKTAILS.getPopularDrinks("lookup.php?i=$idDrink")
                val cocktail = responseService.body()?.Drinks?.get(0)
                withContext(Dispatchers.Main) {
                    if(cocktail != null) {
                        binding.pbDrink.visibility = View.GONE
                        binding.appBarLayout.visibility = View.VISIBLE
                        binding.nestedScrollView.visibility = View.VISIBLE
                        val instructionsToQuery = when (getLanguagePreference(requireActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE))) {
                            getString(R.string.english) -> cocktail.strInstructionsEN
                            getString(R.string.spanish) -> cocktail.strInstructionsES
                            getString(R.string.german) -> cocktail.strInstructionsDE
                            getString(R.string.french) -> cocktail.strInstructionsFR
                            getString(R.string.italian) -> cocktail.strInstructionsIT
                            else -> cocktail.strInstructionsEN
                        }
                        val translator = TranslateService
                        if (instructionsToQuery != null && getLanguagePreference(requireActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE)) != getString(R.string.english)) {
                            binding.pbDrinkInstructions.visibility = View.GONE
                            binding.tvInstructions.text = instructionsToQuery
                        } else {
                            cocktail.strInstructionsEN?.let {
                                translator.englishSpanishTranslator.translate(it)
                                    .addOnSuccessListener { itES ->
                                        binding.pbDrinkInstructions.visibility = View.GONE
                                        binding.tvInstructions.text = itES
                                        binding.tvTranslatedByGoogle.visibility = View.VISIBLE
                                    }
                            }
                        }
                        Glide.with(requireContext())
                            .load(cocktail.strDrinkThumb)
                            .into(binding.ivDrink)
                        binding.tvTitleDrink.text = cocktail.strDrink
                        binding.tvGlass.text = cocktail.strGlass
                        binding.cvGlass.setOnClickListener {  }
                        binding.tvKind.text = cocktail.strAlcoholic
                        binding.cvKind.setOnClickListener {  }
                        binding.tvCategory.text = cocktail.strCategory
                        binding.cvCategory.setOnClickListener {  }
                        binding.rvIngredients.layoutManager =
                            LinearLayoutManager(requireContext())
                        val listIngredients = ArrayList<IngredientSimplifyView>()
                        if (cocktail.strIngredient1 != null) {
                            val ingredient1 = IngredientSimplifyView(
                                cocktail.strIngredient1,
                                cocktail.strMeasure1,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient1}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient2 != null) {
                            val ingredient1 = IngredientSimplifyView(
                                cocktail.strIngredient2,
                                cocktail.strMeasure2,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient2}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient3 != null) {
                            val ingredient1 = IngredientSimplifyView(
                                cocktail.strIngredient3,
                                cocktail.strMeasure3,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient3}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient4 != null) {
                            val ingredient1 = IngredientSimplifyView(
                                cocktail.strIngredient4,
                                cocktail.strMeasure4,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient4}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient5 != null) {
                            val ingredient1 = IngredientSimplifyView(
                                cocktail.strIngredient5,
                                cocktail.strMeasure5,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient5}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient6 != null) {
                            val ingredient1 = IngredientSimplifyView(
                                cocktail.strIngredient6,
                                cocktail.strMeasure6,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient6}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient7 != null) {
                            val ingredient1 = IngredientSimplifyView(
                                cocktail.strIngredient7,
                                cocktail.strMeasure7,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient7}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient8 != null) {
                            val ingredient1 = IngredientSimplifyView(
                                cocktail.strIngredient8,
                                cocktail.strMeasure8,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient8}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient9 != null) {
                            val ingredient1 = IngredientSimplifyView(
                                cocktail.strIngredient9,
                                cocktail.strMeasure9,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient9}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient10 != null) {
                            val ingredient1 = IngredientSimplifyView(
                                cocktail.strIngredient10,
                                cocktail.strMeasure10,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient10}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient11 != null) {
                            val ingredient1 = IngredientSimplifyView(
                                cocktail.strIngredient11,
                                cocktail.strMeasure11,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient11}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient12 != null) {
                            val ingredient1 = IngredientSimplifyView(
                                cocktail.strIngredient12,
                                cocktail.strMeasure12,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient12}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient13 != null) {
                            val ingredient1 = IngredientSimplifyView(
                                cocktail.strIngredient13,
                                cocktail.strMeasure13,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient13}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient14 != null) {
                            val ingredient1 = IngredientSimplifyView(
                                cocktail.strIngredient14,
                                cocktail.strMeasure14,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient14}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient15 != null) {
                            val ingredient1 = IngredientSimplifyView(
                                cocktail.strIngredient15,
                                cocktail.strMeasure15,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient15}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        /*
                        val adapter = PopularDrinksOnClickedAdapter(listIngredients) {
                            showFragment(
                                idContainer,
                                requireActivity(),
                                IngredientFullView(),
                                "IngredientFullViewFragment",
                                ingredientName = it
                            )
                        }

                         */
                        val adapter = IngredientsAdapter(listIngredients, requireContext()) {

                        }
                        binding.rvIngredients.adapter = adapter
                    }
                }
            }
        }
    }

}