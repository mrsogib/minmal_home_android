package com.sogib.minimallauncher

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppDrawerAdapter(
    private var items: List<AppInfo>,
    private val typeface: Typeface,
    private val textSizeSp: Float,
    private val paddingPx: Int,
    private val textColor: Int,
    private val onClick: (AppInfo) -> Unit,
    private val onLongClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppDrawerAdapter.VH>() {

    class VH(val text: TextView) : RecyclerView.ViewHolder(text)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val tv = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_drawer, parent, false) as TextView
        return VH(tv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = items[position]
        holder.text.text = app.displayLabel
        holder.text.typeface = typeface
        holder.text.textSize = textSizeSp
        holder.text.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        holder.text.setTextColor(textColor)
        holder.text.setOnClickListener { onClick(app) }
        holder.text.setOnLongClickListener { onLongClick(app); true }
    }

    override fun getItemCount(): Int = items.size

    fun submit(newItems: List<AppInfo>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun positionForLetter(letter: Char): Int =
        items.indexOfFirst { it.sortLetter == letter }.coerceAtLeast(0)
}
