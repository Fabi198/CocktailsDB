package com.example.username.cocktailsdb.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.username.cocktailsdb.R
import com.example.username.cocktailsdb.adapters.CocktailsMinimalViewAdapter
import com.example.username.cocktailsdb.databinding.FragmentCocktailsListedBinding
import com.example.username.cocktailsdb.entities.CocktailSimpleDTO
import com.example.username.cocktailsdb.objects.DarkMode
import com.example.username.cocktailsdb.objects.ShowFragmentFromFragment.showFragment
import com.example.username.cocktailsdb.retrofit.RetrofitCocktail
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
        val cocktailsSaved = arguments?.getBoolean(getString(R.string.cocktailssaved_tag))
        val letter = arguments?.getString(getString(R.string.letter_tag))

        if (idContainer != null) {
            if (cocktailName != null) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val responseService = RetrofitCocktail.APICOCKTAILS.getCocktailsSimpleList("search.php?s=$cocktailName")
                    val cocktails = responseService.body()?.cocktails
                    withContext(Dispatchers.Main) {
                        onCocktailsQuery(idContainer, cocktails, false)
                    }
                }
            } else if (typeGlass != null) {
                binding.tvKindOrGlassOrCategory.text = typeGlass.replace("_", " ")
                binding.tvKindOrGlassOrCategory.visibility = View.VISIBLE
                lifecycleScope.launch(Dispatchers.IO) {
                    val responseService = RetrofitCocktail.APICOCKTAILS.getCocktailsSimpleList("filter.php?g=$typeGlass")
                    val cocktails = responseService.body()?.cocktails
                    withContext(Dispatchers.Main) {
                        onCocktailsQuery(idContainer, cocktails, false)
                    }
                }
            } else if (typeKind != null) {
                binding.tvKindOrGlassOrCategory.text = typeKind.replace("_", " ")
                binding.tvKindOrGlassOrCategory.visibility = View.VISIBLE
                lifecycleScope.launch(Dispatchers.IO) {
                    val responseService = RetrofitCocktail.APICOCKTAILS.getCocktailsSimpleList("filter.php?a=$typeKind")
                    val cocktails = responseService.body()?.cocktails
                    withContext(Dispatchers.Main) {
                        onCocktailsQuery(idContainer, cocktails, false)
                    }
                }
            } else if (typeCategory != null) {
                binding.tvKindOrGlassOrCategory.text = typeCategory.replace("_", " ")
                binding.tvKindOrGlassOrCategory.visibility = View.VISIBLE
                lifecycleScope.launch(Dispatchers.IO) {
                    val responseService = RetrofitCocktail.APICOCKTAILS.getCocktailsSimpleList("filter.php?c=$typeCategory")
                    val cocktails = responseService.body()?.cocktails
                    withContext(Dispatchers.Main) {
                        onCocktailsQuery(idContainer, cocktails, false)
                    }
                }
            } else if (letter != null) {
                binding.tvKindOrGlassOrCategory.text = letter.uppercase()
                binding.tvKindOrGlassOrCategory.visibility = View.VISIBLE
                lifecycleScope.launch(Dispatchers.IO) {
                    val responseService = RetrofitCocktail.APICOCKTAILS.getCocktailsSimpleList("search.php?f=$letter")
                    val cocktails = responseService.body()?.cocktails
                    withContext(Dispatchers.Main) {
                        onCocktailsQuery(idContainer, cocktails, false)
                    }
                }
            } else if (cocktailsSaved != null) {
                onCocktailsSaved(idContainer)
            }
        }




    }

    private fun onCocktailsSaved(idContainer: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val account = GoogleSignIn.getLastSignedInAccount(requireContext())
            val userId: String? = account!!.id

            try {
                // Obtener los cocktailsIDs del documento del usuario en Firebase
                val userDocumentReference = FirebaseFirestore.getInstance().collection("users").document(userId!!)
                val documentSnapshot = userDocumentReference.get().await()

                val cocktailsIDsMap = documentSnapshot["cocktailIDs"] as? Map<String, Boolean>
                val cocktailsIDsList = cocktailsIDsMap?.keys?.toList() ?: emptyList()

                // Lanzar todas las llamadas a Retrofit de manera asíncrona
                val deferredCocktails = cocktailsIDsList.map { cocktailID ->
                    async(Dispatchers.IO) {
                        // Lanzar cada llamada a Retrofit
                        val responseService =
                            RetrofitCocktail.APICOCKTAILS.getCocktailsList("lookup.php?i=$cocktailID")
                        val cocktail = responseService.body()?.cocktails?.get(0)
                        cocktail?.let {
                            CocktailSimpleDTO(it.idDrink, it.strDrink, it.strDrinkThumb)
                        }
                    }
                }

                // Esperar a que todas las llamadas asincrónicas se completen
                val cocktailsList = deferredCocktails.awaitAll().filterNotNull()

                // Llamar a la función que maneja los cocktailsIDs después de completar todas las operaciones
                withContext(Dispatchers.Main) {
                    onCocktailsQuery(idContainer, cocktailsList, true)
                    // Aquí puedes realizar cualquier otra acción necesaria después de completar la búsqueda de todos los ids
                }
            } catch (e: Exception) {
                Log.e("Firebase", "Error en la obtención de cocktailsIDs: $e")
            }
        }
    }

    private fun onCocktailsQuery(idContainer: Int, cocktails: List<CocktailSimpleDTO>?, cocktailsSaved: Boolean) {
        if (cocktailsSaved) { binding.rvCocktailsByIngredient.layoutManager = GridLayoutManager(requireContext(), 2) }
        binding.pbCocktailList.visibility = View.GONE
        if (cocktails != null) {
            val adapter = CocktailsMinimalViewAdapter(cocktails, requireContext(), cocktailsSaved, {
                showFragment(
                    idContainer,
                    requireActivity(),
                    CocktailFullViewFragment(),
                    getString(R.string.cocktailfullviewfragment_tag),
                    idDrink = it.idDrink
                )
            }, { c ->
                val account = GoogleSignIn.getLastSignedInAccount(requireContext())
                val userId: String? = account!!.id

                try {
                    val usersReference = FirebaseFirestore.getInstance().collection("users").document(userId!!)
                    usersReference.update("cocktailIDs.${c.idDrink}", FieldValue.delete())
                        .addOnSuccessListener {
                            Log.i("Portet", "Cocktail '${c.idDrink}' eliminado exitosamente")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Error al eliminar el cocktail '${c.idDrink}': $e")
                        }
                } catch (e: Exception) {
                    Log.e("Firebase", "Error en la obtención de cocktailsIDs: $e")
                }
                binding.rvCocktailsByIngredient.visibility = View.GONE
                binding.pbCocktailList.visibility = View.VISIBLE
                onCocktailsSaved(idContainer)
            })
            binding.rvCocktailsByIngredient.adapter = adapter
            binding.rvCocktailsByIngredient.visibility = View.VISIBLE
        } else {
            if (!DarkMode.isDarkModeEnabled(requireContext())) { binding.tvNoResults.setTextColor(resources.getColor(R.color.white, null)) }
            binding.tvNoResults.visibility = View.VISIBLE
        }
    }

}