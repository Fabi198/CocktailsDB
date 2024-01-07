package com.example.username.cocktailsdb.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.example.username.cocktailsdb.R
import com.example.username.cocktailsdb.databinding.DrawerListItemBinding

class DrawerExpandableListAdapter(private val context: Context) : BaseExpandableListAdapter() {

    private val groups = arrayOf("Buscar por vaso", "Buscar por categoria", "Buscar por tipo")
    private val children: Map<String, List<Pair<Int, String>>> = mapOf(
        "Buscar por vaso" to listOf(
            R.drawable.glass_highball to "Highball glass",
            R.drawable.glass_cocktail to "Cocktail glass",
            R.drawable.glass_old_fashioned to "Old-fashioned glass",
            R.drawable.glass_whiskey to "Whiskey glass",
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
            0 to "Homemade Liquieur",
            0 to "Punch / Party Drink",
            0 to "Beer",
            0 to "Soft Drink"),
        "Buscar por tipo" to listOf(
            0 to "Alcoholic",
            0 to "Optional alcohol",
            0 to "Non alcoholic")
        // Agrega más opciones adicionales según sea necesario
    )

    override fun getGroupCount(): Int = groups.size

    override fun getChildrenCount(groupPosition: Int): Int = children[groups[groupPosition]]?.size ?: 0

    override fun getGroup(groupPosition: Int): Any = groups[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): Any = children[groups[groupPosition]]?.get(childPosition) ?: ""

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = (groupPosition * 100 + childPosition).toLong()

    override fun hasStableIds(): Boolean = true

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.drawer_list_group, parent, false) as TextView
        view.text = getGroup(groupPosition).toString()
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
