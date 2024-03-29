package com.example.username.cocktailsdb.fragments


import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.username.cocktailsdb.R
import com.example.username.cocktailsdb.adapters.IngredientsMinimalViewAdapter
import com.example.username.cocktailsdb.databinding.CustomAlertDialogAddToFavoritesBinding
import com.example.username.cocktailsdb.databinding.CustomAlertDialogOuncesCalculatorBinding
import com.example.username.cocktailsdb.databinding.FragmentCocktailFullViewBinding
import com.example.username.cocktailsdb.entities.IngredientSimpleDTO
import com.example.username.cocktailsdb.objects.DarkMode.isDarkModeEnabled
import com.example.username.cocktailsdb.objects.NetworkManager.isNetworkAvailable
import com.example.username.cocktailsdb.objects.Preferences.getFavoriteCocktailID
import com.example.username.cocktailsdb.objects.Preferences.getLanguagePreference
import com.example.username.cocktailsdb.objects.Preferences.isThereAlreadyOnFavorites
import com.example.username.cocktailsdb.objects.Preferences.resetFavoriteDrink
import com.example.username.cocktailsdb.objects.Preferences.setFavoriteCocktailID
import com.example.username.cocktailsdb.objects.ShowFragmentFromFragment.showFragment
import com.example.username.cocktailsdb.retrofit.RetrofitCocktail
import com.example.username.cocktailsdb.translator.TranslateService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt


class CocktailFullViewFragment : Fragment(R.layout.fragment_cocktail_full_view) {

    private val maxRecentCocktails = 11
    private lateinit var binding: FragmentCocktailFullViewBinding
    private var cocktailSaved = false
    private var textOriginalShowed = false
    private lateinit var sharedPrefs: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCocktailFullViewBinding.bind(view)
        val idContainer = arguments?.getInt(getString(R.string.idcontainer_tag))
        val idDrink = arguments?.getString(getString(R.string.iddrink_tag))
        val random = arguments?.getBoolean(getString(R.string.random_tag))
        sharedPrefs = requireActivity().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)

        binding.collapsingToolbarLayout.title = " "

        setupUI(idContainer, idDrink, random)
    }

    private fun setupUI(idContainer: Int?, idDrink: String?, random: Boolean?) {
        if (idContainer != null && (idDrink != null || random != null)) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val link = if (random != null && random == true) { "random.php" } else { "lookup.php?i=$idDrink" }
                    val responseService = RetrofitCocktail.APICOCKTAILS.getCocktailsList(link)

                    if (responseService.isSuccessful) {
                        val cocktail = responseService.body()?.cocktails?.get(0)

                        withContext(Dispatchers.Main) {
                            if(cocktail != null) {
                                val account = GoogleSignIn.getLastSignedInAccount(requireContext())
                                if (account != null) {
                                    binding.btnSaveCocktail.visibility = View.VISIBLE
                                    val userId: String? = account.id
                                    val usersReference = FirebaseFirestore.getInstance().collection("users").document(userId!!)

                                    checkIfCocktailAlreadyVisited(usersReference, cocktail.idDrink!!)

                                    usersReference.get()
                                        .addOnSuccessListener { documentSnapshot ->
                                            if (documentSnapshot.exists()) {
                                                // Verificamos si el campo 'cocktailIDs' contiene el idDrink a consultar
                                                val cocktailIDsMap = documentSnapshot["cocktailIDs"] as? Map<String, Boolean>
                                                if (cocktailIDsMap != null && cocktailIDsMap.containsKey(idDrink)) {
                                                    Log.i("Portet", "El idDrink '${cocktail.idDrink}' está presente en cocktailIDs")
                                                    binding.btnSaveCocktail.setImageResource(R.drawable.baseline_bookmark_24)
                                                    cocktailSaved = true
                                                } else {
                                                    Log.i("Portet", "El idDrink '${cocktail.idDrink}' NO está presente en cocktailIDs")
                                                }
                                            } else {
                                                Log.e("Firebase", "El documento 'users' no existe")
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("Firebase", "Error al obtener el documento 'users': $e")
                                        }
                                    binding.btnSaveCocktail.setOnClickListener {
                                        if (isNetworkAvailable(requireActivity())) {
                                            if (!cocktailSaved) {
                                                binding.btnSaveCocktail.setImageResource(R.drawable.baseline_bookmark_24)
                                                usersReference.update("cocktailIDs.${cocktail.idDrink}", true)
                                                    .addOnSuccessListener {
                                                        Log.i("Portet", "Cocktail '${cocktail.idDrink}' agregado exitosamente")
                                                        cocktailSaved = true
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.e("Firebase", "Error al agregar el cocktail '${cocktail.idDrink}': $e")
                                                        Toast.makeText(requireContext(), "Error al agregar el cocktail '${cocktail.idDrink}': $e", Toast.LENGTH_SHORT).show()
                                                        binding.btnSaveCocktail.setImageResource(R.drawable.baseline_bookmark_border_24)
                                                    }
                                            } else {
                                                binding.btnSaveCocktail.setImageResource(R.drawable.baseline_bookmark_border_24)
                                                usersReference.update("cocktailIDs.${cocktail.idDrink}", FieldValue.delete())
                                                    .addOnSuccessListener {
                                                        Log.i("Portet", "Cocktail '${cocktail.idDrink}' eliminado exitosamente")
                                                        cocktailSaved = false
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.e("Firebase", "Error al eliminar el cocktail '$idDrink': $e")
                                                        binding.btnSaveCocktail.setImageResource(R.drawable.baseline_bookmark_24)
                                                    }
                                            }
                                        } else {
                                            Toast.makeText(requireContext(), "No hay conexión a Internet", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }

                                if (isThereAlreadyOnFavorites(sharedPrefs, cocktail.idDrink.toString())) {
                                    binding.btnAddToFavorites.setImageResource(R.drawable.baseline_star_24)
                                }
                                if (!isDarkModeEnabled(requireContext())) {
                                    binding.tvTitleDrink.setTextColor(resources.getColor(R.color.white, null))
                                    binding.tvInstructions.setTextColor(resources.getColor(R.color.white, null))
                                    binding.tvTranslatedByGoogle.setTextColor(resources.getColor(R.color.white, null))
                                }
                                binding.btnAddToFavorites.setOnClickListener {
                                    if (isNetworkAvailable(requireActivity())) {
                                        if (!isThereAlreadyOnFavorites(sharedPrefs, cocktail.idDrink.toString())) {
                                            showAddToFavoritesDialog(cocktail.idDrink.toString(), binding.btnAddToFavorites)
                                        } else {
                                            resetFavoriteDrink(sharedPrefs, cocktail.idDrink.toString())
                                        }
                                    } else {
                                        Toast.makeText(requireContext(), "No hay conexión a Internet", Toast.LENGTH_SHORT).show()
                                    }
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
                                    binding.tvTranslatedByGoogle.visibility = View.GONE
                                } else {
                                    cocktail.strInstructionsEN?.let {
                                        translator.englishSpanishTranslator.translate(it)
                                            .addOnSuccessListener { itES ->
                                                binding.pbDrinkInstructions.visibility = View.GONE
                                                binding.tvInstructions.text = itES
                                                binding.tvTranslatedByGoogle.visibility = View.VISIBLE


                                                binding.btnSeeOriginal.visibility = View.VISIBLE
                                                binding.btnSeeOriginal.setOnClickListener {
                                                    if (!textOriginalShowed) {
                                                        binding.tvTranslatedByGoogle.visibility = View.GONE
                                                        binding.tvInstructions.text = cocktail.strInstructionsEN
                                                        binding.btnSeeOriginal.text = getString(R.string.ver_traducci_n)
                                                    } else {
                                                        binding.tvInstructions.text = itES
                                                        binding.tvTranslatedByGoogle.visibility = View.VISIBLE
                                                        binding.btnSeeOriginal.text = getString(R.string.ver_original)
                                                    }
                                                    textOriginalShowed = !textOriginalShowed
                                                }
                                            }
                                    }
                                }
                                Glide.with(requireContext())
                                    .load(cocktail.strDrinkThumb)
                                    .into(binding.ivCocktail)
                                binding.tvTitleDrink.text = cocktail.strDrink
                                binding.tvGlass.text = cocktail.strGlass
                                binding.cvGlass.setOnClickListener { if (isNetworkAvailable(requireActivity())) { showFragment(idContainer, requireActivity(), CocktailsListedFragment(), getString(R.string.cocktailslistedfragment_tag), typeGlass = binding.tvGlass.text.toString().replace(" ", "_")) } else { Toast.makeText(requireContext(), "No hay conexión a Internet", Toast.LENGTH_SHORT).show() } }
                                binding.tvKind.text = cocktail.strAlcoholic
                                binding.cvKind.setOnClickListener { if (isNetworkAvailable(requireActivity())) { showFragment(idContainer, requireActivity(), CocktailsListedFragment(), getString(R.string.cocktailslistedfragment_tag), typeKind = binding.tvKind.text.toString().replace(" ", "_")) } else { Toast.makeText(requireContext(), "No hay conexión a Internet", Toast.LENGTH_SHORT).show() } }
                                binding.tvCategory.text = cocktail.strCategory
                                binding.cvCategory.setOnClickListener { if (isNetworkAvailable(requireActivity())) { showFragment(idContainer, requireActivity(), CocktailsListedFragment(), getString(R.string.cocktailslistedfragment_tag), typeCategory = binding.tvCategory.text.toString().replace(" ", "_")) } else { Toast.makeText(requireContext(), "No hay conexión a Internet", Toast.LENGTH_SHORT).show() } }
                                binding.btnCalculator.setOnClickListener { showCalculatorDialog() }
                                binding.rvIngredients.layoutManager = LinearLayoutManager(requireContext())
                                val listIngredients = ArrayList<IngredientSimpleDTO>()
                                if (cocktail.strIngredient1 != null) { listIngredients.add(IngredientSimpleDTO(cocktail.strIngredient1, cocktail.strMeasure1, "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient1}-Small.png")) }
                                if (cocktail.strIngredient2 != null) { listIngredients.add(IngredientSimpleDTO(cocktail.strIngredient2, cocktail.strMeasure2, "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient2}-Small.png")) }
                                if (cocktail.strIngredient3 != null) { listIngredients.add(IngredientSimpleDTO(cocktail.strIngredient3, cocktail.strMeasure3, "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient3}-Small.png")) }
                                if (cocktail.strIngredient4 != null) { listIngredients.add(IngredientSimpleDTO(cocktail.strIngredient4, cocktail.strMeasure4, "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient4}-Small.png")) }
                                if (cocktail.strIngredient5 != null) { listIngredients.add(IngredientSimpleDTO(cocktail.strIngredient5, cocktail.strMeasure5, "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient5}-Small.png")) }
                                if (cocktail.strIngredient6 != null) { listIngredients.add(IngredientSimpleDTO(cocktail.strIngredient6, cocktail.strMeasure6, "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient6}-Small.png")) }
                                if (cocktail.strIngredient7 != null) { listIngredients.add(IngredientSimpleDTO(cocktail.strIngredient7, cocktail.strMeasure7, "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient7}-Small.png")) }
                                if (cocktail.strIngredient8 != null) { listIngredients.add(IngredientSimpleDTO(cocktail.strIngredient8, cocktail.strMeasure8, "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient8}-Small.png")) }
                                if (cocktail.strIngredient9 != null) { listIngredients.add(IngredientSimpleDTO(cocktail.strIngredient9, cocktail.strMeasure9, "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient9}-Small.png")) }
                                if (cocktail.strIngredient10 != null) { listIngredients.add(IngredientSimpleDTO(cocktail.strIngredient10, cocktail.strMeasure10, "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient10}-Small.png")) }
                                if (cocktail.strIngredient11 != null) { listIngredients.add(IngredientSimpleDTO(cocktail.strIngredient11, cocktail.strMeasure11, "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient11}-Small.png")) }
                                if (cocktail.strIngredient12 != null) { listIngredients.add(IngredientSimpleDTO(cocktail.strIngredient12, cocktail.strMeasure12, "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient12}-Small.png")) }
                                if (cocktail.strIngredient13 != null) { listIngredients.add(IngredientSimpleDTO(cocktail.strIngredient13, cocktail.strMeasure13, "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient13}-Small.png")) }
                                if (cocktail.strIngredient14 != null) { listIngredients.add(IngredientSimpleDTO(cocktail.strIngredient14, cocktail.strMeasure14, "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient14}-Small.png")) }
                                if (cocktail.strIngredient15 != null) { listIngredients.add(IngredientSimpleDTO(cocktail.strIngredient15, cocktail.strMeasure15, "https://www.thecocktaildb.com/images/ingredients/${cocktail.strIngredient15}-Small.png")) }
                                val adapter = IngredientsMinimalViewAdapter(listIngredients, requireContext()) { if (isNetworkAvailable(requireActivity())) { showFragment(idContainer, requireActivity(), IngredientFullViewFragment(), getString(R.string.ingredientfullviewfragment_tag), ingredientName = it.strIngredient) } else { Toast.makeText(requireContext(), "No hay conexión a Internet", Toast.LENGTH_SHORT).show() } }
                                binding.rvIngredients.adapter = adapter
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            // Manejo de errores en caso de respuesta no exitosa
                            Log.e("API Error", "Error en la respuesta de la API: ${responseService.message()}")
                            Toast.makeText(requireContext(), "Error en la respuesta de la API: ${responseService.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        // Manejo de errores generales
                        Log.e("Error", "Error en la consulta a la API: $e")
                        Toast.makeText(requireContext(), "Error en la consulta a la API: $e", Toast.LENGTH_SHORT).show()
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

    private fun getCocktailImage(position: Int, ivDrink: ImageView, btn: ImageButton): String {
        var strDrinkThumb = ""
        val id = getFavoriteCocktailID(sharedPrefs, position)
        lifecycleScope.launch(Dispatchers.IO) {
            val responseService = RetrofitCocktail.APICOCKTAILS.getCocktailsList("lookup.php?i=$id")
            val cocktail = responseService.body()?.cocktails?.get(0)
            withContext(Dispatchers.Main) {
                if (cocktail != null) {
                    if (cocktail.strDrinkThumb != null) {
                        strDrinkThumb = cocktail.strDrinkThumb
                        Glide.with(requireContext()).load(cocktail.strDrinkThumb).into(ivDrink)
                        btn.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.white, null))
                    }
                }
            }
        }
        return strDrinkThumb
    }

    private fun showAddToFavoritesDialog(idDrink: String, btnAddToFavorites: ImageButton) {
        val binding = CustomAlertDialogAddToFavoritesBinding.inflate(LayoutInflater.from(requireContext()))
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setCancelable(true)
            .create()
        getCocktailImage(0, binding.ivDrink0, binding.btn0)
        getCocktailImage(1, binding.ivDrink1, binding.btn1)
        getCocktailImage(2, binding.ivDrink2, binding.btn2)
        getCocktailImage(3, binding.ivDrink3, binding.btn3)
        getCocktailImage(4, binding.ivDrink4, binding.btn4)
        getCocktailImage(5, binding.ivDrink5, binding.btn5)
        getCocktailImage(6, binding.ivDrink6, binding.btn6)
        getCocktailImage(7, binding.ivDrink7, binding.btn7)

        binding.btn0.setOnClickListener {
            setFavoriteCocktailID(requireActivity().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE), 0, idDrink)
            btnAddToFavorites.setImageResource(R.drawable.baseline_star_24)
            alertDialog.dismiss()
        }
        binding.btn1.setOnClickListener {
            setFavoriteCocktailID(requireActivity().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE), 1, idDrink)
            btnAddToFavorites.setImageResource(R.drawable.baseline_star_24)
            alertDialog.dismiss()
        }
        binding.btn2.setOnClickListener {
            setFavoriteCocktailID(requireActivity().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE), 2, idDrink)
            btnAddToFavorites.setImageResource(R.drawable.baseline_star_24)
            alertDialog.dismiss()
        }
        binding.btn3.setOnClickListener {
            setFavoriteCocktailID(requireActivity().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE), 3, idDrink)
            btnAddToFavorites.setImageResource(R.drawable.baseline_star_24)
            alertDialog.dismiss()
        }
        binding.btn4.setOnClickListener {
            setFavoriteCocktailID(requireActivity().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE), 4, idDrink)
            btnAddToFavorites.setImageResource(R.drawable.baseline_star_24)
            alertDialog.dismiss()
        }
        binding.btn5.setOnClickListener {
            setFavoriteCocktailID(requireActivity().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE), 5, idDrink)
            btnAddToFavorites.setImageResource(R.drawable.baseline_star_24)
            alertDialog.dismiss()
        }
        binding.btn6.setOnClickListener {
            setFavoriteCocktailID(requireActivity().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE), 6, idDrink)
            btnAddToFavorites.setImageResource(R.drawable.baseline_star_24)
            alertDialog.dismiss()
        }
        binding.btn7.setOnClickListener {
            setFavoriteCocktailID(requireActivity().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE), 7, idDrink)
            btnAddToFavorites.setImageResource(R.drawable.baseline_star_24)
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun trimRecentCocktailsList(usersReference: DocumentReference) {
        usersReference.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val recentCocktails: MutableList<String> =
                        documentSnapshot.get("recentCocktails") as? MutableList<String> ?: mutableListOf()

                    // Trim the list if it exceeds the limit of 10
                    while (recentCocktails.size > maxRecentCocktails) {
                        recentCocktails.removeAt(recentCocktails.size - 1)
                    }

                    // Update the recentCocktails list in Firestore
                    usersReference.update("recentCocktails", recentCocktails)
                        .addOnSuccessListener {
                            Log.i("Portet", "Lista 'recentCocktails' actualizada exitosamente")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Error al actualizar 'recentCocktails': $e")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al obtener el documento 'users': $e")
            }
    }

    private fun checkIfCocktailAlreadyVisited(usersReference: DocumentReference, cocktailId: String) {
        usersReference.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val recentCocktails: MutableList<String> =
                        documentSnapshot.get("recentCocktails") as? MutableList<String> ?: mutableListOf()

                    if (!recentCocktails.contains(cocktailId)) {
                        // Si el cocktail no está en la lista, agrégalo
                        addRecentCocktailToFirestore(usersReference, cocktailId)
                    } else {
                        // Si el cocktail ya está en la lista, muévelo al principio
                        moveCocktailToTop(usersReference, cocktailId)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al obtener el documento 'users': $e")
            }
    }

    private fun addRecentCocktailToFirestore(usersReference: DocumentReference, cocktailId: String) {
        usersReference.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val recentCocktails: MutableList<String> =
                        (documentSnapshot.get("recentCocktails") as? MutableList<String>)?.toMutableList()
                            ?: mutableListOf()

                    // Agrega el nuevo cocktail en la posición 0
                    recentCocktails.add(0, cocktailId)

                    // Si la lista excede el límite de 10 elementos, elimina el último elemento
                    while (recentCocktails.size > maxRecentCocktails) {
                        recentCocktails.removeAt(recentCocktails.size - 1)
                    }

                    // Actualiza la lista en Firestore
                    usersReference.update("recentCocktails", recentCocktails)
                        .addOnSuccessListener {
                            Log.i("Portet", "Cocktail '$cocktailId' agregado a 'recentCocktails' exitosamente")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Error al actualizar 'recentCocktails': $e")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al obtener el documento 'users': $e")
            }
    }


    private fun moveCocktailToTop(usersReference: DocumentReference, cocktailId: String) {
        usersReference.update(
            "recentCocktails",
            FieldValue.arrayRemove(cocktailId)
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                addRecentCocktailToFirestore(usersReference, cocktailId)
            } else {
                Log.e("Firebase", "Error al eliminar y volver a agregar el cocktail en 'recentCocktails': ${task.exception}")
            }
        }
    }

}