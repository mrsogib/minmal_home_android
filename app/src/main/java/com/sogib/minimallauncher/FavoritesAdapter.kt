package com.sogib.minimallauncher

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavoritesAdapter(
    private val items: MutableList<AppInfo>,
    private val typeface: Typeface,
    private val textSizeSp: Float,
    private val paddingPx: Int,
    private val textColor: Int,
    private val onClick: (AppInfo) -> Unit,
    private val onLongClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.VH>() {

    class VH(val text: TextView) : RecyclerView.ViewHolder(text)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val tv = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite, parent, false) as TextView
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

    fun moveItem(from: Int, to: Int) {
        val item = items.removeAt(from)
        items.add(to, item)
        notifyItemMoved(from, to)
    }

    fun currentOrder(): List<String> = items.map { it.packageName }

    fun replaceAll(newItems: List<AppInfo>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }
}
