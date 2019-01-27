package com.example.czarek.czarkomondo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.czarek.czarkomondo.R
import com.example.czarek.czarkomondo.models.SettingsListItem
import kotlinx.android.synthetic.main.settings_row.view.*

class ActionsAdapter(private val context: Context, private val dataSource: List<SettingsListItem>): BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = convertView ?: inflater.inflate(R.layout.settings_row, parent, false)
        itemView.settings_row_drawable.setImageDrawable(context.getDrawable(getItem(position).drawableId))
        itemView.settings_row_text.text = context.getString(getItem(position).nameId)
        return itemView
    }

    override fun getItem(position: Int) = dataSource[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = dataSource.size
}