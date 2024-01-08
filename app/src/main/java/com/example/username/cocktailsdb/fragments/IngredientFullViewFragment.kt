package com.example.username.cocktailsdb.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.username.cocktailsdb.R
import com.example.username.cocktailsdb.adapters.CocktailsMinimalViewAdapter
import com.example.username.cocktailsdb.databinding.CustomAlertDialogIngredientDescriptionBinding
import com.example.username.cocktailsdb.databinding.FragmentIngredientFullViewBinding
import com.example.username.cocktailsdb.objects.DarkMode.isDarkModeEnabled
import com.example.username.cocktailsdb.objects.ShowFragmentFromFragment.showFragment
import com.example.username.cocktailsdb.retrofit.RetrofitCocktail
import com.example.username.cocktailsdb.translator.TranslateService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class IngredientFullViewFragment : Fragment(R.layout.fragment_ingredient_full_view) {

    private lateinit var binding: FragmentIngredientFullViewBinding
    private lateinit var descTranslated: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentIngredientFullViewBinding.bind(view)
        val idContainer = arguments?.getInt(getString(R.string.idcontainer_tag))
        val ingredientName = arguments?.getString(getString(R.string.ingredient_tag))?.replace(" ", "_")

        setupUI(idContainer, ingredientName)
    }

    private fun setupUI(idContainer: Int?, ingredientName: String?) {
        if (idContainer != null && ingredientName != null) {
            Log.i("Portet", ingredientName)
            lifecycleScope.launch(Dispatchers.IO) {
                val translator = TranslateService
                val responseService = RetrofitCocktail.APICOCKTAILS.getIngredient("search.php?i=$ingredientName")
                val ingredientDTO = responseService.body()
                val responseService2 = RetrofitCocktail.APICOCKTAILS.getCocktailsSimpleList("filter.php?i=$ingredientName")
                val cocktails = responseService2.body()
                withContext(Dispatchers.Main) {
                    if (responseService.isSuccessful) {
                        if (!isDarkModeEnabled(requireContext())) {
                            binding.tvTitleIngredient.setTextColor(resources.getColor(R.color.white, null))
                            binding.tvDescription.setTextColor(resources.getColor(R.color.white, null))
                        }
                        binding.pbIngredient.visibility = View.GONE
                        binding.rlMain.visibility = View.VISIBLE
                        binding.tvTitleIngredient.text = ingredientDTO?.ingredient?.get(0)?.strIngredient
                        Glide.with(requireContext())
                            .load("https://www.thecocktaildb.com/images/ingredients/$ingredientName.png")
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    binding.pbIvIngredient.visibility = View.GONE
                                    binding.ivIngredient.visibility = View.VISIBLE
                                    Toast.makeText(requireContext(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    // La imagen se ha cargado correctamente
                                    binding.pbIvIngredient.visibility = View.GONE
                                    binding.ivIngredient.visibility = View.VISIBLE
                                    return false
                                }
                            })
                            .into(binding.ivIngredient)
                        binding.tvDescription.text = ingredientDTO?.ingredient?.get(0)?.strDescription
                        ingredientDTO?.ingredient?.get(0)?.strDescription?.let {
                            translator.englishSpanishTranslator.translate(it)
                                .addOnSuccessListener { itES ->
                                    binding.tvDescription.text = itES
                                    descTranslated = itES
                                }
                        }
                        binding.tvDescription.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                            override fun onGlobalLayout() {
                                if (binding.tvDescription.lineCount > 4) {
                                    binding.tvSeeMore.visibility = View.VISIBLE
                                }
                                binding.tvDescription.viewTreeObserver.removeOnGlobalLayoutListener(this)
                            }
                        })
                        if (binding.tvDescription.lineCount > 4) { binding.tvSeeMore.visibility = View.VISIBLE }
                        binding.tvSeeMore.setOnClickListener { showDialogFullDescription(descTranslated) }
                        if (responseService2.isSuccessful) {
                            val listCocktails = cocktails?.cocktails
                            val adapter = CocktailsMinimalViewAdapter(listCocktails!!, requireContext()) {
                                showFragment(
                                    idContainer,
                                    requireActivity(),
                                    CocktailFullViewFragment(),
                                    getString(R.string.cocktailfullviewfragment_tag),
                                    idDrink = it.idDrink
                                )
                            }
                            binding.rvCocktailsByIngredient.adapter = adapter
                        } else {
                            Toast.makeText(requireContext(), "Error en la base de datos", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun showDialogFullDescription(description: String?) {
        val binding = CustomAlertDialogIngredientDescriptionBinding.inflate(LayoutInflater.from(requireContext()))
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setCancelable(true)
            .create()
        binding.tvDescFull.text = description
        alertDialog.show()
    }

}