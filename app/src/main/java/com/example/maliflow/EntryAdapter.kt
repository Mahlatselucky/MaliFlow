package com.example.maliflow

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.maliflow.data.model.ExpenseEntry
import java.io.File

class EntryAdapter(
    private val entries: MutableList<ExpenseEntry>
) : RecyclerView.Adapter<EntryAdapter.EntryViewHolder>() {

    inner class EntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvEntryDate)
        val tvDescription: TextView = itemView.findViewById(R.id.tvEntryDescription)
        val tvAmount: TextView = itemView.findViewById(R.id.tvEntryAmount)
        val tvTime: TextView = itemView.findViewById(R.id.tvEntryTime)
        val ivPhoto: ImageView = itemView.findViewById(R.id.ivEntryPhoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_entry, parent, false)
        return EntryViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val entry = entries[position]
        holder.tvDate.text = entry.date
        holder.tvDescription.text = entry.description
        holder.tvAmount.text = "R ${entry.amount}"
        holder.tvTime.text = "${entry.startTime} - ${entry.endTime}"

        if (entry.photoPath != null) {
            holder.ivPhoto.visibility = View.VISIBLE
            if (entry.photoPath.startsWith("content://")) {
                Glide.with(holder.itemView.context)
                    .load(Uri.parse(entry.photoPath))
                    .into(holder.ivPhoto)
            } else {
                Glide.with(holder.itemView.context)
                    .load(File(entry.photoPath))
                    .into(holder.ivPhoto)
            }
        } else {
            holder.ivPhoto.visibility = View.GONE
        }
    }

    override fun getItemCount() = entries.size

    fun updateEntries(newEntries: MutableList<ExpenseEntry>) {
        entries.clear()
        entries.addAll(newEntries)
        notifyDataSetChanged()
    }
}