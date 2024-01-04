package com.example.username.cocktailsdb

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.username.cocktailsdb.adapters.DrawerExpandableListAdapter
import com.example.username.cocktailsdb.databinding.ActivityMainBinding
import com.example.username.cocktailsdb.entities.ArraysNames.arrayCategories
import com.example.username.cocktailsdb.entities.ArraysNames.arrayGlasses
import com.example.username.cocktailsdb.entities.ArraysNames.arrayKinds
import com.example.username.cocktailsdb.fragments.CocktailFullViewFragment
import com.example.username.cocktailsdb.objects.Preferences.getLanguagePreference
import com.example.username.cocktailsdb.objects.Preferences.getToastTwoBackShowed
import com.example.username.cocktailsdb.objects.Preferences.isSessionSaved
import com.example.username.cocktailsdb.objects.Preferences.saveLanguagePreference
import com.example.username.cocktailsdb.objects.Preferences.setToastTwoBackShowed
import com.google.firebase.auth.FirebaseAuth

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private val doubleClickTimeDelta = 300
    private var lastClickTime: Long = 0
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPrefs: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPrefs = getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE)

        setupDrawerNavigationBar()
        setupDrawerContent()
        setupPopularCocktails()

        binding.cvCloseSession.btnPreferences.setOnClickListener { showPreferences() }
    }

    private fun setupPopularCocktails() {
        binding.btnPopDrink1.setOnClickListener { showFragment(CocktailFullViewFragment(), getString(R.string.cocktailfullviewfragment_tag)) }
    }

    private fun showPreferences() {
        binding.expandableListView.visibility = View.GONE
        binding.llPreferences.visibility = View.VISIBLE

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
        }




    }

    private fun setupDrawerNavigationBar() {
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
        val expandableListAdapter = DrawerExpandableListAdapter(this)
        binding.expandableListView.setAdapter(expandableListAdapter)

        binding.expandableListView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            val childData = expandableListAdapter.getChild(groupPosition, childPosition) as Pair<Int, String>

            when (childData.second) {
                in arrayGlasses -> {

                }
                in arrayCategories -> {

                }
                in arrayKinds -> {

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
        FirebaseAuth.getInstance().signOut()
        val prefsEd = sharedPrefs.edit()
        prefsEd.clear()
        prefsEd.apply()
        startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
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
                allVisible()
            } else {
                super.onBackPressed()
                finish()
            }
        }




    }

    private fun showFragment(fragment: Fragment, tag: String, typeAlcoholic: String? = null, typeGlass: String? = null, typeCategory: String? = null, idDrink: String? = null, idIngredient: String? = null, ingredientName: String? = null, letter: String? = null) {
        val bundle = Bundle()
        bundle.putInt(getString(R.string.idcontainer_tag), binding.containerFragment.id)
        if (typeAlcoholic != null) { bundle.putString("Alcoholic", typeAlcoholic) }
        if (typeGlass != null) { bundle.putString("Glass", typeGlass) }
        if (typeCategory != null) { bundle.putString("Category", typeCategory) }
        if (idDrink != null) { bundle.putString("id", idDrink) }
        if (idIngredient != null) { bundle.putInt("id", Integer.parseInt(idIngredient)) }
        if (ingredientName != null) { bundle.putString("Ingredient", ingredientName) }
        if (letter != null) { bundle.putString("letter", letter) }
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