package com.example.username.cocktailsdb.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.username.cocktailsdb.R
import com.example.username.cocktailsdb.adapters.CocktailsMinimalViewAdapter
import com.example.username.cocktailsdb.databinding.FragmentCocktailsListedBinding
import com.example.username.cocktailsdb.entities.CocktailSimpleDTO
import com.example.username.cocktailsdb.objects.DarkMode
import com.example.username.cocktailsdb.objects.NetworkManager.isNetworkAvailable
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
    private lateinit var tvKindOrGlassOrCategory: TextView

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
        tvKindOrGlassOrCategory = requireActivity().findViewById<TextView>(R.id.tvKindOrGlassOrCategory)


        if (idContainer != null) {
            if (cocktailName != null) {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val responseService = RetrofitCocktail.APICOCKTAILS.getCocktailsSimpleList("search.php?s=$cocktailName")
                        val cocktails = responseService.body()?.cocktails
                        withContext(Dispatchers.Main) {
                            onCocktailsQuery(idContainer, cocktails, cocktailsFromAccount = false, cocktailsSaved = false)
                        }
                    } catch (e: Exception) {
                        handleApiError(e)
                    }
                }
            } else if (typeGlass != null) {
                tvKindOrGlassOrCategory.text = typeGlass.replace("_", " ")
                tvKindOrGlassOrCategory.visibility = View.VISIBLE
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val responseService = RetrofitCocktail.APICOCKTAILS.getCocktailsSimpleList("filter.php?g=$typeGlass")
                        val cocktails = responseService.body()?.cocktails
                        withContext(Dispatchers.Main) {
                            onCocktailsQuery(idContainer, cocktails, cocktailsFromAccount = false, cocktailsSaved = false)
                        }
                    } catch (e: Exception) {
                        handleApiError(e)
                    }
                }
            } else if (typeKind != null) {
                tvKindOrGlassOrCategory.text = typeKind.replace("_", " ")
                tvKindOrGlassOrCategory.visibility = View.VISIBLE
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val responseService = RetrofitCocktail.APICOCKTAILS.getCocktailsSimpleList("filter.php?a=$typeKind")
                        val cocktails = responseService.body()?.cocktails
                        withContext(Dispatchers.Main) {
                            onCocktailsQuery(idContainer, cocktails, cocktailsFromAccount = false, cocktailsSaved = false)
                        }
                    } catch (e: Exception) {
                        handleApiError(e)
                    }
                }
            } else if (typeCategory != null) {
                tvKindOrGlassOrCategory.text = typeCategory.replace("_", " ")
                tvKindOrGlassOrCategory.visibility = View.VISIBLE
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val responseService = RetrofitCocktail.APICOCKTAILS.getCocktailsSimpleList("filter.php?c=$typeCategory")
                        val cocktails = responseService.body()?.cocktails
                        withContext(Dispatchers.Main) {
                            onCocktailsQuery(idContainer, cocktails, cocktailsFromAccount = false, cocktailsSaved = false)
                        }
                    } catch (e: Exception) {
                        handleApiError(e)
                    }
                }
            } else if (letter != null) {
                tvKindOrGlassOrCategory.text = letter.uppercase()
                tvKindOrGlassOrCategory.visibility = View.VISIBLE
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val responseService = RetrofitCocktail.APICOCKTAILS.getCocktailsSimpleList("search.php?f=$letter")
                        val cocktails = responseService.body()?.cocktails
                        withContext(Dispatchers.Main) {
                            onCocktailsQuery(idContainer, cocktails, cocktailsFromAccount = false, cocktailsSaved = false)
                        }
                    } catch (e: Exception) {
                        handleApiError(e)
                    }
                }
            } else if (cocktailsSaved != null) {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        if (cocktailsSaved) {
                            tvKindOrGlassOrCategory.text = getString(R.string.guardados)
                            tvKindOrGlassOrCategory.visibility = View.VISIBLE
                            onCocktailsSaved(idContainer)
                        } else {
                            tvKindOrGlassOrCategory.text = getString(R.string.historial)
                            tvKindOrGlassOrCategory.visibility = View.VISIBLE
                            onCocktailsHistory(idContainer)
                        }
                        
                    } catch (e: Exception) {
                        handleFirebaseError(e)
                    }
                }

            }
        }




    }

    private fun onCocktailsHistory(idContainer: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val account = GoogleSignIn.getLastSignedInAccount(requireContext())
            val userId: String? = account!!.id

            try {
                // Obtener los recentCocktails del documento del usuario en Firebase
                val userDocumentReference = FirebaseFirestore.getInstance().collection("users").document(userId!!)
                val documentSnapshot = userDocumentReference.get().await()

                val recentCocktails = documentSnapshot.get("recentCocktails") as? List<String> ?: emptyList()

                // Lanzar todas las llamadas a Retrofit de manera asíncrona
                val deferredCocktails = recentCocktails.map { cocktailID ->
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

                // Llamar a la función que maneja los recentCocktails después de completar todas las operaciones
                withContext(Dispatchers.Main) {
                    onCocktailsQuery(idContainer, cocktailsList, cocktailsFromAccount = true, cocktailsSaved = false)
                }
            } catch (e: Exception) {
                Log.e("Firebase", "Error en la obtención de recentCocktails: $e")
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
                    onCocktailsQuery(idContainer, cocktailsList, cocktailsFromAccount = true, cocktailsSaved = true)
                }
            } catch (e: Exception) {
                Log.e("Firebase", "Error en la obtención de cocktailsIDs: $e")
            }
        }
    }

    private fun onCocktailsQuery(idContainer: Int, cocktails: List<CocktailSimpleDTO>?, cocktailsFromAccount: Boolean, cocktailsSaved: Boolean) {
        if (cocktailsFromAccount) { binding.rvCocktailsByIngredient.layoutManager = GridLayoutManager(requireContext(), 2) }
        binding.pbCocktailList.visibility = View.GONE
        if (!cocktails.isNullOrEmpty()) {
            val adapter = CocktailsMinimalViewAdapter(cocktails, requireContext(), cocktailsSaved, {
                if (isNetworkAvailable(requireActivity())) {
                    tvKindOrGlassOrCategory.visibility = View.GONE
                    showFragment(
                        idContainer,
                        requireActivity(),
                        CocktailFullViewFragment(),
                        getString(R.string.cocktailfullviewfragment_tag),
                        idDrink = it.idDrink
                    )
                } else {
                    Toast.makeText(requireContext(), "No hay conexión a Internet", Toast.LENGTH_SHORT).show()
                }
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

    private suspend fun handleApiError(e: Exception) {
        withContext(Dispatchers.Main) {
            Log.e("API Error", "Error en la consulta a la API: $e")
            Toast.makeText(requireContext(), "Error en la consulta a la API", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun handleFirebaseError(e: Exception) {
        withContext(Dispatchers.Main) {
            Log.e("Firebase", "Error en la obtención de cocktailsIDs: $e")
        }
    }


}