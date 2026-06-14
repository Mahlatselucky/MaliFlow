package com.example.maliflow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.maliflow.data.model.Category

class CategoryAdapter(
    private val onDelete: (Category) -> Unit,
    private val onEditLimit: (Category) -> Unit,
    private val onRolloverToggle: (Category, Boolean) -> Unit
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEmoji: TextView = itemView.findViewById(R.id.tvCategoryEmoji)
        val tvName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val tvLimit: TextView = itemView.findViewById(R.id.tvCategoryLimit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteCategory)
        val btnEditLimit: ImageButton = itemView.findViewById(R.id.btnEditLimit)
        val switchRollover: SwitchCompat = itemView.findViewById(R.id.switchRollover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.tvEmoji.text = category.emoji
        holder.tvName.text = category.name

        if (category.budgetLimit > 0.0) {
            holder.tvLimit.text = "Monthly limit: R${category.budgetLimit.toInt()}"
        } else {
            holder.tvLimit.text = "No limit set - tap edit to add one"
        }

        // Set the switch state without triggering the listener
        holder.switchRollover.setOnCheckedChangeListener(null)
        holder.switchRollover.isChecked = category.rolloverEnabled
        holder.switchRollover.setOnCheckedChangeListener { _, isChecked ->
            onRolloverToggle(category, isChecked)
        }

        holder.btnDelete.setOnClickListener { onDelete(category) }
        holder.btnEditLimit.setOnClickListener { onEditLimit(category) }
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }
}
