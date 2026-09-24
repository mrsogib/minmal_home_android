package com.sogib.minimallauncher

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavoritesAdapter(
    private val items: MutableList<AppInfo>,
    private val typeface: Typeface,
    private val textSizeSp: Float,
    private val paddingPx: Int,
    private val textColor: Int,
    private val showIcons: Boolean,
    private val showNotificationPreview: Boolean,
    private val onClick: (AppInfo) -> Unit,
    private val onLongClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.VH>() {

    class VH(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val name: TextView = view.findViewById(R.id.appName)
        val preview: TextView = view.findViewById(R.id.notificationPreview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite, parent, false)
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
            holder.icon.visibility = android.view.View.VISIBLE
            holder.icon.setImageDrawable(IconCache.get(context, app.packageName))
        } else {
            holder.icon.visibility = android.view.View.GONE
        }

        if (showNotificationPreview) {
            val preview = NotificationAccessService.getPreview(app.packageName)
            if (preview != null) {
                holder.preview.visibility = android.view.View.VISIBLE
                holder.preview.text = preview
                holder.preview.typeface = typeface
            } else {
                holder.preview.visibility = android.view.View.GONE
            }
        } else {
            holder.preview.visibility = android.view.View.GONE
        }

        holder.itemView.setOnClickListener { onClick(app) }
        holder.itemView.setOnLongClickListener { onLongClick(app); true }
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

    /** Call periodically (e.g. onResume) to reflect updated notification previews. */
    fun refreshPreviewsOnly() {
        if (showNotificationPreview) notifyDataSetChanged()
    }
}
