package com.example.username.cocktailsdb.adapters

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.username.cocktailsdb.R
import com.example.username.cocktailsdb.databinding.DrawerListGroupBinding
import com.example.username.cocktailsdb.databinding.DrawerListItemBinding
import com.example.username.cocktailsdb.objects.Preferences.isSessionSaved
import com.google.android.gms.auth.api.signin.GoogleSignIn

class DrawerExpandableListAdapter(private val activity: Activity, private val context: Context) : BaseExpandableListAdapter() {

    private val groups = arrayOf("Buscar por vaso", "Buscar por categoria", "Buscar por tipo", "Buscar por letra", "Mi Cuenta")
    private val children: Map<String, List<Pair<Int, String>>> = mapOf(
        "Buscar por vaso" to listOf(
            R.drawable.glass_highball to "Highball glass",
            R.drawable.glass_cocktail to "Cocktail glass",
            R.drawable.glass_old_fashioned to "Old-fashioned glass",
            R.drawable.glass_whiskey to "Whiskey Glass",
            R.drawable.glass_collins to "Collins glass",
            R.drawable.glass_pousse_cafe to "Pousse cafe glass",
            R.drawable.glass_champagne_flute to "Champagne flute",
            R.drawable.glass_whiskey_sour to "Whiskey sour glass",
            R.drawable.glass_cordial to "Cordial glass",
            R.drawable.glass_snifter to "Brandy snifter",
            R.drawable.glass_white_wine to "White wine glass",
            R.drawable.glass_hurricane to "Hurricane glass",
            R.drawable.glass_shot to "Shot glass",
            R.drawable.glass_irish_beer_mug to "Irish coffee cup",
            R.drawable.glass_pint to "Pint glass",
            R.drawable.glass_white_wine to "Wine Glass",
            R.drawable.glass_beer_mug to "Beer mug",
            R.drawable.glass_whiskey_sour to "Margarita/Coupette glass",
            R.drawable.glass_pilsner_beer to "Beer pilsner",
            R.drawable.glass_whiskey_sour to "Margarita glass",
            R.drawable.glass_cocktail to "Martini Glass",
            R.drawable.glass_balloon to "Balloon Glass",
            R.drawable.glass_champagne_coupe to "Coupe Glass"),
        "Buscar por categoria" to listOf(
            0 to "Ordinary Drink",
            0 to "Cocktail",
            0 to "Shake",
            0 to "Other / Unknown",
            0 to "Cocoa",
            0 to "Shot",
            0 to "Coffee / Tea",
            0 to "Homemade Liqueur",
            0 to "Punch / Party Drink",
            0 to "Beer",
            0 to "Soft Drink"),
        "Buscar por tipo" to listOf(
            0 to "Alcoholic",
            0 to "Optional alcohol",
            0 to "Non alcoholic"),
        "Buscar por letra" to listOf(
            0 to "A",
            0 to "B",
            0 to "C",
            0 to "D",
            0 to "E",
            0 to "F",
            0 to "G",
            0 to "H",
            0 to "I",
            0 to "J",
            0 to "K",
            0 to "L",
            0 to "M",
            0 to "N",
            0 to "O",
            0 to "P",
            0 to "Q",
            0 to "R",
            0 to "S",
            0 to "T",
            0 to "U",
            0 to "V",
            0 to "W",
            0 to "X",
            0 to "Y",
            0 to "Z"
        ),
        // Agrega más opciones adicionales según sea necesario
        "Mi Cuenta" to listOf(
            0 to "Mis Cocteles"
        )
    )

    override fun getGroupCount(): Int = if (isSessionSaved(activity.getSharedPreferences(context.getString(R.string.preferences), Context.MODE_PRIVATE))) { groups.size } else { groups.size - 1 }

    override fun getChildrenCount(groupPosition: Int): Int = children[groups[groupPosition]]?.size ?: 0

    override fun getGroup(groupPosition: Int): Any = groups[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): Any = children[groups[groupPosition]]?.get(childPosition) ?: ""

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = (groupPosition * 100 + childPosition).toLong()

    override fun hasStableIds(): Boolean = true

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        //val view = LayoutInflater.from(context).inflate(R.layout.drawer_list_group, parent, false) as TextView
        //view.text = getGroup(groupPosition).toString()
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.drawer_list_group, parent, false)
        val binding = DrawerListGroupBinding.bind(view)

        binding.listHeader.text = getGroup(groupPosition).toString()

        binding.ivProfile.visibility = if (groupPosition == 4) View.VISIBLE else View.GONE

        if (groupPosition == 4) {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account != null) {
                // Obtener la URL de la foto de perfil
                val photoUrl: Uri? = account.photoUrl

                // Verificar si hay una URL de foto de perfil
                if (photoUrl != null) {
                    // Cargar la foto de perfil utilizando Glide
                    Glide.with(context)
                        .load(photoUrl)
                        .into(binding.ivProfile)
                } else {
                    // Si no hay URL de foto de perfil, puedes establecer una imagen predeterminada
                    binding.ivProfile.setImageResource(R.drawable.placeholder_60_x_60)
                }
            }
        }

        return view
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.drawer_list_item, parent, false)
        val binding = DrawerListItemBinding.bind(view)

        val childData = getChild(groupPosition, childPosition) as Pair<Int, String>

        binding.ivItem.visibility = if (childData.first != 0) { View.VISIBLE } else { View.GONE }
        binding.ivItem.setImageResource(childData.first)
        binding.listItem.text = childData.second

        return view
    }


    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
}
