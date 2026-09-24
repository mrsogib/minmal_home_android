package com.sogib.minimallauncher

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppDrawerAdapter(
    private var items: List<AppInfo>,
    private val typeface: Typeface,
    private val textSizeSp: Float,
    private val paddingPx: Int,
    private val textColor: Int,
    private val showIcons: Boolean,
    private val onClick: (AppInfo) -> Unit,
    private val onLongClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppDrawerAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val name: TextView = view.findViewById(R.id.appName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_drawer, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = items[position]
        val context = holder.itemView.context

        holder.name.text = app.displayLabel
        holder.name.typeface = typeface
        holder.name.textSize = textSizeSp
        holder.name.setTextColor(textColor)
        holder.itemView.setPadding(paddingPx, paddingPx / 2, paddingPx, paddingPx / 2)

        if (showIcons) {
            holder.icon.visibility = View.VISIBLE
            holder.icon.setImageDrawable(IconCache.get(context, app.packageName))
        } else {
            holder.icon.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onClick(app) }
        holder.itemView.setOnLongClickListener { onLongClick(app); true }
    }

    override fun getItemCount(): Int = items.size

    fun submit(newItems: List<AppInfo>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun positionForLetter(letter: Char): Int =
        items.indexOfFirst { it.sortLetter == letter }.coerceAtLeast(0)

    /** Used by GroupDividerDecoration to know where letter groups change. */
    fun letterAt(position: Int): Char? = items.getOrNull(position)?.sortLetter
}
