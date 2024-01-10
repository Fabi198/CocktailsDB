package com.example.username.cocktailsdb

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.username.cocktailsdb.adapters.DrawerExpandableListAdapter
import com.example.username.cocktailsdb.databinding.ActivityMainBinding
import com.example.username.cocktailsdb.databinding.CustomAlertDialogAddToFavoritesBinding
import com.example.username.cocktailsdb.databinding.CustomAlertDialogYonBinding
import com.example.username.cocktailsdb.entities.ArraysNames.arrayCategories
import com.example.username.cocktailsdb.entities.ArraysNames.arrayGlasses
import com.example.username.cocktailsdb.entities.ArraysNames.arrayKinds
import com.example.username.cocktailsdb.entities.ArraysNames.arrayLetters
import com.example.username.cocktailsdb.entities.ArraysNames.arrayMyAccount
import com.example.username.cocktailsdb.fragments.CocktailFullViewFragment
import com.example.username.cocktailsdb.fragments.CocktailsListedFragment
import com.example.username.cocktailsdb.fragments.IngredientFullViewFragment
import com.example.username.cocktailsdb.objects.GoogleSignInManager
import com.example.username.cocktailsdb.objects.Preferences.getFavoriteCocktailID
import com.example.username.cocktailsdb.objects.Preferences.getFavoritesCocktailsIDs
import com.example.username.cocktailsdb.objects.Preferences.getLanguagePreference
import com.example.username.cocktailsdb.objects.Preferences.getToastTwoBackShowed
import com.example.username.cocktailsdb.objects.Preferences.isSessionSaved
import com.example.username.cocktailsdb.objects.Preferences.restoreFavoriteCocktailsDefault
import com.example.username.cocktailsdb.objects.Preferences.saveLanguagePreference
import com.example.username.cocktailsdb.objects.Preferences.setToastTwoBackShowed
import com.example.username.cocktailsdb.objects.ProviderType
import com.example.username.cocktailsdb.objects.ShowFragmentFromFragment
import com.example.username.cocktailsdb.retrofit.RetrofitCocktail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private val doubleClickTimeDelta = 300
    private var lastClickTime: Long = 0
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPrefs: SharedPreferences
    private var googleSignInManager: GoogleSignInManager? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPrefs = getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE)
        googleSignInManager = GoogleSignInManager.getInstance(this)
        googleSignInManager?.setupGoogleSignInOptions()

        setupDrawerNavigationBar()
        setupDrawerContent()
        setupFavoriteCocktails()
        setupSearchers()

        binding.cvCloseSession.btnGoogle.setOnClickListener { googleSignInManager!!.signIn() }
        binding.cvCloseSession.btnCloseSession.setOnClickListener { googleSignInManager!!.signOut() }
        binding.cvCloseSession.btnPreferences.setOnClickListener { showPreferences() }
    }

    private fun setupSearchers() {
        binding.etSearch.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                if (binding.etSearch.text.toString().isNotEmpty()) {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.dlMain.windowToken, 0)
                    showFragment(CocktailsListedFragment(), "CocktailListedFragment", cocktailName = binding.etSearch.text.toString().replace(" ", ""))
                    binding.etSearch.text = null
                }
                return@setOnKeyListener true }
            false
        }
        binding.btnSearch.setOnClickListener {
            if (binding.etSearch.text.toString().isNotEmpty()) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.dlMain.windowToken, 0)
                showFragment(CocktailsListedFragment(), "CocktailListedFragment", cocktailName = binding.etSearch.text.toString().replace(" ", ""))
                binding.etSearch.text = null
            }
        }
        binding.etIngredientSearch.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                if (binding.etIngredientSearch.text.toString().isNotEmpty()) {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.dlMain.windowToken, 0)
                    lifecycleScope.launch(Dispatchers.IO) {
                        val responseService = RetrofitCocktail.APICOCKTAILS.getIngredient("search.php?i=${binding.etIngredientSearch.text}")
                        val ingredientDTO = responseService.body()
                        withContext(Dispatchers.Main) {
                            if (ingredientDTO?.ingredient != null) {
                                showFragment(IngredientFullViewFragment(), getString(R.string.ingredientfullviewfragment_tag), ingredientName = ingredientDTO.ingredient[0].strIngredient)
                            } else {
                                Toast.makeText(this@MainActivity, "No hay resultados", Toast.LENGTH_SHORT).show()
                            }
                            binding.etIngredientSearch.text = null
                        }
                    }

                }
                return@setOnKeyListener true }
            false
        }
        binding.btnIngredientSearch.setOnClickListener {
            if (binding.etIngredientSearch.text.toString().isNotEmpty()) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.dlMain.windowToken, 0)
                lifecycleScope.launch(Dispatchers.IO) {
                    val responseService = RetrofitCocktail.APICOCKTAILS.getIngredient("search.php?i=${binding.etIngredientSearch.text}")
                    val ingredientDTO = responseService.body()
                    withContext(Dispatchers.Main) {
                        if (ingredientDTO != null) {
                            showFragment(IngredientFullViewFragment(), getString(R.string.ingredientfullviewfragment_tag), ingredientName = ingredientDTO.ingredient[0].strIngredient)
                        } else {
                            Toast.makeText(this@MainActivity, "No hay resultados", Toast.LENGTH_SHORT).show()
                        }
                        binding.etIngredientSearch.text = null
                    }
                }

            }
        }
    }

    private fun setupFavoriteCocktails() {
        getFavoritesCocktailsIDs(sharedPrefs).forEachIndexed { index, c ->
            lifecycleScope.launch(Dispatchers.IO) {
                val responseService =
                    RetrofitCocktail.APICOCKTAILS.getCocktailsList("lookup.php?i=$c")
                val cocktail = responseService.body()?.cocktails?.get(0)
                withContext(Dispatchers.Main) {
                    if (cocktail != null) {
                        Glide.with(this@MainActivity)
                            .load(cocktail.strDrinkThumb)
                            .placeholder(R.drawable.placeholder_60_x_60)
                            .into(when (index) {
                                0 -> { binding.ivPopDrink1 }
                                1 -> { binding.ivPopDrink2 }
                                2 -> { binding.ivPopDrink3 }
                                3 -> { binding.ivPopDrink4 }
                                4 -> { binding.ivPopDrink5 }
                                5 -> { binding.ivPopDrink6 }
                                6 -> { binding.ivPopDrink7 }
                                7 -> { binding.ivPopDrink8 }
                                else -> { binding.ivPopDrink1 }
                            })
                        when (index) {
                            0 -> { binding.tvPopDrink1.text = cocktail.strDrink }
                            1 -> { binding.tvPopDrink2.text = cocktail.strDrink }
                            2 -> { binding.tvPopDrink3.text = cocktail.strDrink }
                            3 -> { binding.tvPopDrink4.text = cocktail.strDrink }
                            4 -> { binding.tvPopDrink5.text = cocktail.strDrink }
                            5 -> { binding.tvPopDrink6.text = cocktail.strDrink }
                            6 -> { binding.tvPopDrink7.text = cocktail.strDrink }
                            7 -> { binding.tvPopDrink8.text = cocktail.strDrink }
                            else -> { binding.tvPopDrink1.text = cocktail.strDrink }
                        }
                    }
                }
            }
        }

        binding.btnPopDrink1.setOnClickListener { showFragment(CocktailFullViewFragment(), getString(R.string.cocktailfullviewfragment_tag), idDrink = getFavoriteCocktailID(sharedPrefs, 0)) }
        binding.btnPopDrink2.setOnClickListener { showFragment(CocktailFullViewFragment(), getString(R.string.cocktailfullviewfragment_tag), idDrink = getFavoriteCocktailID(sharedPrefs, 1)) }
        binding.btnPopDrink3.setOnClickListener { showFragment(CocktailFullViewFragment(), getString(R.string.cocktailfullviewfragment_tag), idDrink = getFavoriteCocktailID(sharedPrefs, 2)) }
        binding.btnPopDrink4.setOnClickListener { showFragment(CocktailFullViewFragment(), getString(R.string.cocktailfullviewfragment_tag), idDrink = getFavoriteCocktailID(sharedPrefs, 3)) }
        binding.btnPopDrink5.setOnClickListener { showFragment(CocktailFullViewFragment(), getString(R.string.cocktailfullviewfragment_tag), idDrink = getFavoriteCocktailID(sharedPrefs, 4)) }
        binding.btnPopDrink6.setOnClickListener { showFragment(CocktailFullViewFragment(), getString(R.string.cocktailfullviewfragment_tag), idDrink = getFavoriteCocktailID(sharedPrefs, 5)) }
        binding.btnPopDrink7.setOnClickListener { showFragment(CocktailFullViewFragment(), getString(R.string.cocktailfullviewfragment_tag), idDrink = getFavoriteCocktailID(sharedPrefs, 6)) }
        binding.btnPopDrink8.setOnClickListener { showFragment(CocktailFullViewFragment(), getString(R.string.cocktailfullviewfragment_tag), idDrink = getFavoriteCocktailID(sharedPrefs, 7)) }
    }

    private fun showPreferences() {
        binding.expandableListView.visibility = View.GONE
        binding.llPreferences.visibility = View.VISIBLE


        binding.btnResetFavorites.setOnClickListener {
            showSureDialog("restoreFavorites")
            binding.dlMain.closeDrawers()
        }

        val animDown: Animation = AnimationUtils.loadAnimation(this, R.anim.scroll_to_down)
        val animUp: Animation = AnimationUtils.loadAnimation(this, R.anim.scroll_to_up)

        animUp.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) { binding.btnLanguageGroup.visibility = View.GONE }
            override fun onAnimationRepeat(animation: Animation?) {}
        })

        binding.btnShowLanguageOptions.setOnClickListener {
            if (binding.btnLanguageGroup.visibility == View.VISIBLE) {
                binding.btnLanguageGroup.startAnimation(animUp)
            } else {
                binding.btnLanguageGroup.startAnimation(animDown)
                binding.btnLanguageGroup.visibility = View.VISIBLE
            }
        }

        when (getLanguagePreference(sharedPrefs)) {
            getString(R.string.english) -> { binding.btnEnglish.isChecked = true }
            getString(R.string.spanish) -> { binding.btnSpanish.isChecked = true }
            getString(R.string.german) -> { binding.btnGerman.isChecked = true }
            getString(R.string.french) -> { binding.btnFrench.isChecked = true }
            getString(R.string.italian) -> { binding.btnItalian.isChecked = true }
        }


        binding.btnLanguageGroup.setOnCheckedChangeListener { _, checkedId ->
            val newLanguageSelected = when (checkedId) {
                R.id.btnEnglish -> getString(R.string.english)
                R.id.btnSpanish -> getString(R.string.spanish)
                R.id.btnGerman -> getString(R.string.german)
                R.id.btnFrench -> getString(R.string.french)
                R.id.btnItalian -> getString(R.string.italian)
                else -> getString(R.string.english)
            }

            saveLanguagePreference(sharedPrefs, newLanguageSelected)
            binding.dlMain.closeDrawers()
            startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }




    }

    private fun showSureDialog(task: String) {
        val binding = CustomAlertDialogYonBinding.inflate(LayoutInflater.from(this))
        val alertDialog = AlertDialog.Builder(this)
            .setView(binding.root)
            .setCancelable(true)
            .create()
        when (task) {
            "restoreFavorites" -> {
                binding.btnYes.setOnClickListener {
                    restoreFavoriteCocktailsDefault(getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE))
                    alertDialog.dismiss()
                    startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                }
            }
            else -> {}
        }
        binding.btnNo.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    private fun setupDrawerNavigationBar() {
        binding.ivLogoMain.setOnClickListener { startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)) }
        if (isSessionSaved(sharedPrefs)) {
            binding.cvCloseSession.btnCloseSession.visibility = View.VISIBLE
        } else {
            binding.cvCloseSession.btnGoogle.visibility = View.VISIBLE
        }
        setSupportActionBar(binding.toolbarMain)
        supportActionBar?.title = null
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val drawerToggle = ActionBarDrawerToggle(this, binding.dlMain, binding.toolbarMain, R.string.abrir, R.string.cerrar)
        binding.dlMain.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        drawerToggle.drawerArrowDrawable.color = resources.getColor(R.color.white, null)
        binding.nvHome.itemIconTintList = null
        binding.nvHome.itemBackground = null
        binding.cvCloseSession.btnCloseSession.setOnClickListener { signOut() }
        binding.dlMain.addDrawerListener(object : DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerClosed(drawerView: View) {
                binding.btnLanguageGroup.visibility = View.GONE
                binding.llPreferences.visibility = View.GONE
                binding.expandableListView.visibility = View.VISIBLE
            }
            override fun onDrawerStateChanged(newState: Int) {}
        })
    }

    private fun setupDrawerContent() {
        val expandableListAdapter = DrawerExpandableListAdapter(this, this)
        binding.expandableListView.setAdapter(expandableListAdapter)

        binding.expandableListView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            val childData = expandableListAdapter.getChild(groupPosition, childPosition) as Pair<Int, String>

            when (childData.second) {
                in arrayGlasses -> {
                    showFragment(CocktailsListedFragment(), getString(R.string.cocktailslistedfragment_tag), typeGlass = childData.second.replace(" ", "_"))
                }
                in arrayCategories -> {
                    showFragment(CocktailsListedFragment(), getString(R.string.cocktailslistedfragment_tag), typeCategory = childData.second.replace(" ", "_"))
                }
                in arrayKinds -> {
                    showFragment(CocktailsListedFragment(), getString(R.string.cocktailslistedfragment_tag), typeKind = childData.second.replace(" ", "_"))
                }
                in arrayLetters -> {
                    showFragment(CocktailsListedFragment(), getString(R.string.cocktailslistedfragment_tag), letter = childData.second.lowercase())
                }
                in arrayMyAccount -> {
                    showFragment(CocktailsListedFragment(), getString(R.string.cocktailslistedfragment_tag), cocktailsSaved = true)
                }
            }
            for (i in 0 until expandableListAdapter.groupCount) {
                binding.expandableListView.collapseGroup(i)
            }
            binding.dlMain.closeDrawers()
            true
        }

        binding.expandableListView.setOnGroupClickListener { _, _, groupPosition, _ ->
            if (binding.expandableListView.isGroupExpanded(groupPosition)) {
                binding.expandableListView.collapseGroup(groupPosition)
            } else {
                binding.expandableListView.expandGroup(groupPosition)
            }
            true
        }
    }

    private fun signOut() {
        val prefsEd = sharedPrefs.edit()
        prefsEd.clear()
        prefsEd.apply()
        startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == googleSignInManager!!.GOOGLE_SIGN_IN) {
            googleSignInManager!!.handleSignInResult(data)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.llPreferences.visibility == View.VISIBLE) {
            binding.llPreferences.visibility = View.GONE
            binding.expandableListView.visibility = View.VISIBLE
        } else if (binding.dlMain.isDrawerOpen(GravityCompat.START)) {
            binding.dlMain.closeDrawer(GravityCompat.START)
        } else {
            if (supportFragmentManager.backStackEntryCount > 1) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime < doubleClickTimeDelta) {
                    supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    supportFragmentManager.popBackStack()
                    startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                } else {
                    lastClickTime = currentTime
                    if (!getToastTwoBackShowed(sharedPrefs)) {
                        Toast.makeText(this, getString(R.string.presiona_2_veces_para_volver_al_inicio), Toast.LENGTH_SHORT).show()
                        setToastTwoBackShowed(sharedPrefs, true)
                    }
                    supportFragmentManager.popBackStack()
                }
            } else if (supportFragmentManager.backStackEntryCount == 1) {
                supportFragmentManager.popBackStack()
                allVisible()
            } else {
                super.onBackPressed()
                finish()
            }
        }




    }

    private fun showFragment(fragment: Fragment, tag: String, typeKind: String? = null, typeGlass: String? = null, typeCategory: String? = null, idDrink: String? = null, idIngredient: String? = null, ingredientName: String? = null, cocktailName: String? = null, cocktailsSaved: Boolean? = null, letter: String ?= null) {
        val bundle = Bundle()
        bundle.putInt(getString(R.string.idcontainer_tag), binding.containerFragment.id)
        if (typeKind != null) { bundle.putString(getString(R.string.kind_tag), typeKind) }
        if (typeGlass != null) { bundle.putString(getString(R.string.glass_tag), typeGlass) }
        if (typeCategory != null) { bundle.putString(getString(R.string.category_tag), typeCategory) }
        if (idDrink != null) { bundle.putString(getString(R.string.iddrink_tag), idDrink) }
        if (idIngredient != null) { bundle.putInt(getString(R.string.idingredient_tag), Integer.parseInt(idIngredient)) }
        if (ingredientName != null) { bundle.putString(getString(R.string.ingredient_tag), ingredientName) }
        if (cocktailName != null) { bundle.putString(getString(R.string.cocktailname_tag), cocktailName) }
        if (cocktailsSaved != null) { bundle.putBoolean(getString(R.string.cocktailssaved_tag), cocktailsSaved) }
        if (letter != null) { bundle.putString(getString(R.string.letter_tag), letter) }
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(binding.containerFragment.id, fragment, tag).addToBackStack(tag).commit()
        allGone()
        binding.containerFragment.visibility = View.VISIBLE
    }

    private fun allVisible() {
        binding.containerFragment.visibility = View.GONE
        binding.ivBackground.visibility = View.VISIBLE
        binding.cvSearch.visibility = View.VISIBLE
        binding.cvIngredientSearch.visibility = View.VISIBLE
        binding.tvPopDrinksTitle.visibility = View.VISIBLE
        binding.tlPopCocktails.visibility = View.VISIBLE
    }

    private fun allGone() {
        binding.ivBackground.visibility = View.GONE
        binding.cvSearch.visibility = View.GONE
        binding.cvIngredientSearch.visibility = View.GONE
        binding.tvPopDrinksTitle.visibility = View.GONE
        binding.tlPopCocktails.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        setToastTwoBackShowed(sharedPrefs, false)
    }
}