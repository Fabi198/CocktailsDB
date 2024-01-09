package com.example.username.cocktailsdb.fragments


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.username.cocktailsdb.R
import com.example.username.cocktailsdb.adapters.IngredientsMinimalViewAdapter
import com.example.username.cocktailsdb.databinding.CustomAlertDialogOuncesCalculatorBinding
import com.example.username.cocktailsdb.databinding.FragmentCocktailFullViewBinding
import com.example.username.cocktailsdb.entities.IngredientSimpleDTO
import com.example.username.cocktailsdb.objects.DarkMode.isDarkModeEnabled
import com.example.username.cocktailsdb.objects.Preferences.getLanguagePreference
import com.example.username.cocktailsdb.objects.ShowFragmentFromFragment.showFragment
import com.example.username.cocktailsdb.retrofit.RetrofitCocktail
import com.example.username.cocktailsdb.translator.TranslateService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt


class CocktailFullViewFragment : Fragment(R.layout.fragment_cocktail_full_view) {


    private lateinit var binding: FragmentCocktailFullViewBinding
    private var cocktailSaved = false

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
                    RetrofitCocktail.APICOCKTAILS.getCocktailsList("lookup.php?i=$idDrink")
                val cocktail = responseService.body()?.cocktails?.get(0)
                withContext(Dispatchers.Main) {
                    if(cocktail != null) {
                        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
                        val userId: String? = account!!.id
                        val usersReference = FirebaseFirestore.getInstance().collection("users").document(userId!!)

                        usersReference.get()
                            .addOnSuccessListener { documentSnapshot ->
                                if (documentSnapshot.exists()) {
                                    // Verificamos si el campo 'cocktailIDs' contiene el idDrink a consultar
                                    val cocktailIDsMap = documentSnapshot["cocktailIDs"] as? Map<String, Boolean>
                                    if (cocktailIDsMap != null && cocktailIDsMap.containsKey(idDrink)) {
                                        Log.i("Portet", "El idDrink '$idDrink' está presente en cocktailIDs")
                                        binding.btnSaveCocktail.setImageResource(R.drawable.baseline_bookmark_24)
                                        cocktailSaved = true
                                    } else {
                                        Log.i("Portet", "El idDrink '$idDrink' NO está presente en cocktailIDs")
                                    }
                                } else {
                                    Log.e("Firebase", "El documento 'users' no existe")
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firebase", "Error al obtener el documento 'users': $e")
                            }
                        if (!isDarkModeEnabled(requireContext())) {
                            binding.tvTitleDrink.setTextColor(resources.getColor(R.color.white, null))
                            binding.tvInstructions.setTextColor(resources.getColor(R.color.white, null))
                            binding.tvTranslatedByGoogle.setTextColor(resources.getColor(R.color.white, null))
                        }
                        //val saveCocktailDrawable =
                        //binding.btnSaveCocktail.setImageResource()
                        binding.btnSaveCocktail.setOnClickListener {
                            if (!cocktailSaved) {
                                binding.btnSaveCocktail.setImageResource(R.drawable.baseline_bookmark_24)
                                usersReference.update("cocktailIDs.$idDrink", true)
                                    .addOnSuccessListener {
                                        Log.i("Portet", "Cocktail '$idDrink' agregado exitosamente")
                                        cocktailSaved = true
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Firebase", "Error al agregar el cocktail '$idDrink': $e")
                                        Toast.makeText(requireContext(), "Error al agregar el cocktail '$idDrink': $e", Toast.LENGTH_SHORT).show()
                                        binding.btnSaveCocktail.setImageResource(R.drawable.baseline_bookmark_border_24)
                                    }
                            } else {
                                binding.btnSaveCocktail.setImageResource(R.drawable.baseline_bookmark_border_24)
                                usersReference.update("cocktailIDs.$idDrink", FieldValue.delete())
                                    .addOnSuccessListener {
                                        Log.i("Portet", "Cocktail '$idDrink' eliminado exitosamente")
                                        cocktailSaved = false
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Firebase", "Error al eliminar el cocktail '$idDrink': $e")
                                        binding.btnSaveCocktail.setImageResource(R.drawable.baseline_bookmark_24)
                                    }
                            }


                        }
                        binding.btnAddToFavorites.setOnClickListener {

                        }
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
                            .into(binding.ivCocktail)
                        binding.tvTitleDrink.text = cocktail.strDrink
                        binding.tvGlass.text = cocktail.strGlass
                        binding.cvGlass.setOnClickListener { showFragment(idContainer, requireActivity(), CocktailsListedFragment(), getString(R.string.cocktailslistedfragment_tag), typeGlass = binding.tvGlass.text.toString().replace(" ", "_")) }
                        binding.tvKind.text = cocktail.strAlcoholic
                        binding.cvKind.setOnClickListener { showFragment(idContainer, requireActivity(), CocktailsListedFragment(), getString(R.string.cocktailslistedfragment_tag), typeKind = binding.tvKind.text.toString().replace(" ", "_")) }
                        binding.tvCategory.text = cocktail.strCategory
                        binding.cvCategory.setOnClickListener { showFragment(idContainer, requireActivity(), CocktailsListedFragment(), getString(R.string.cocktailslistedfragment_tag), typeCategory = binding.tvCategory.text.toString().replace(" ", "_")) }
                        binding.btnCalculator.setOnClickListener { showCalculatorDialog() }
                        binding.rvIngredients.layoutManager =
                            LinearLayoutManager(requireContext())
                        val listIngredients = ArrayList<IngredientSimpleDTO>()
                        if (cocktail.strIngredient1 != null) {
                            val ingredient1 = IngredientSimpleDTO(
                                cocktail.strIngredient1,
                                cocktail.strMeasure1,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient1}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient2 != null) {
                            val ingredient1 = IngredientSimpleDTO(
                                cocktail.strIngredient2,
                                cocktail.strMeasure2,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient2}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient3 != null) {
                            val ingredient1 = IngredientSimpleDTO(
                                cocktail.strIngredient3,
                                cocktail.strMeasure3,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient3}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient4 != null) {
                            Log.i("Portet", "${cocktail.strIngredient4}, ${cocktail.strMeasure4}")
                            val ingredient1 = IngredientSimpleDTO(
                                cocktail.strIngredient4,
                                cocktail.strMeasure4,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient4}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient5 != null) {
                            val ingredient1 = IngredientSimpleDTO(
                                cocktail.strIngredient5,
                                cocktail.strMeasure5,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient5}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient6 != null) {
                            val ingredient1 = IngredientSimpleDTO(
                                cocktail.strIngredient6,
                                cocktail.strMeasure6,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient6}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient7 != null) {
                            val ingredient1 = IngredientSimpleDTO(
                                cocktail.strIngredient7,
                                cocktail.strMeasure7,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient7}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient8 != null) {
                            val ingredient1 = IngredientSimpleDTO(
                                cocktail.strIngredient8,
                                cocktail.strMeasure8,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient8}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient9 != null) {
                            val ingredient1 = IngredientSimpleDTO(
                                cocktail.strIngredient9,
                                cocktail.strMeasure9,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient9}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient10 != null) {
                            val ingredient1 = IngredientSimpleDTO(
                                cocktail.strIngredient10,
                                cocktail.strMeasure10,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient10}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient11 != null) {
                            val ingredient1 = IngredientSimpleDTO(
                                cocktail.strIngredient11,
                                cocktail.strMeasure11,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient11}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient12 != null) {
                            val ingredient1 = IngredientSimpleDTO(
                                cocktail.strIngredient12,
                                cocktail.strMeasure12,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient12}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient13 != null) {
                            val ingredient1 = IngredientSimpleDTO(
                                cocktail.strIngredient13,
                                cocktail.strMeasure13,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient13}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient14 != null) {
                            val ingredient1 = IngredientSimpleDTO(
                                cocktail.strIngredient14,
                                cocktail.strMeasure14,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient14}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        if (cocktail.strIngredient15 != null) {
                            val ingredient1 = IngredientSimpleDTO(
                                cocktail.strIngredient15,
                                cocktail.strMeasure15,
                                "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient15}-Small.png"
                            )
                            listIngredients.add(ingredient1)
                        }
                        val adapter = IngredientsMinimalViewAdapter(listIngredients, requireContext()) {
                            Log.i("Portet", "${it.strIngredient}, ${it.strMeasure}, ${it.strImageSource}")
                            showFragment(
                                idContainer,
                                requireActivity(),
                                IngredientFullViewFragment(),
                                getString(R.string.ingredientfullviewfragment_tag),
                                ingredientName = it.strIngredient
                            )
                        }
                        binding.rvIngredients.adapter = adapter
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showCalculatorDialog() {
        val binding = CustomAlertDialogOuncesCalculatorBinding.inflate(LayoutInflater.from(requireContext()))
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setCancelable(true)
            .create()
        binding.etOunces.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isNotEmpty() && binding.etOunces.text.toString() != ".") {
                    val result = (binding.etOunces.text.toString().toDouble() * 29.574)
                    val roundoff = (result * 100.0).roundToInt().toDouble() / 100.0
                    binding.tvResult.setText("$roundoff ml")
                } else {
                    binding.tvResult.setText(getString(R.string.ml))
                }
            }

        })
        binding.etOunces.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                if (binding.etOunces.text.toString().isNotEmpty()) {
                    val imm = requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.llMain.windowToken, 0)
                    val result = (binding.etOunces.text.toString().toDouble() * 29.574)
                    val roundoff = (result * 100.0).roundToInt().toDouble() / 100.0
                    binding.tvResult.setText("$roundoff ml")
                    binding.etOunces.setText("")
                }
                return@setOnKeyListener true }
            false
        }
        alertDialog.show()
    }

}