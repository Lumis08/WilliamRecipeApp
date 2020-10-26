package com.example.williamrecipeapp.model

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.williamrecipeapp.R

class CustomSpinnerAdapter(val context: Context, var dataSource: List<String>) : BaseAdapter() {

    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View
        val itemHolder: ItemHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.recipetypes, parent, false)
            itemHolder = ItemHolder(view)
            view?.tag = itemHolder
        } else {
            view = convertView
            itemHolder = view.tag as ItemHolder
        }

        itemHolder.textItem.text = dataSource[position]

        return view
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return dataSource.size
    }

    private class ItemHolder(row: View?) {
        val textItem = row?.findViewById(R.id.textViewSpinner) as TextView
    }


}