package com.example.maliflow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.maliflow.data.dao.CategoryTotal
import com.example.maliflow.data.model.Category

class CategoryTotalsAdapter(
    private val categories: MutableList<Category>,
    private val totals: MutableList<CategoryTotal>
) : RecyclerView.Adapter<CategoryTotalsAdapter.TotalViewHolder>() {

    inner class TotalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategoryName: TextView = itemView.findViewById(R.id.tvTotalCategoryName)
        val tvTotal: TextView = itemView.findViewById(R.id.tvTotalAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TotalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_total, parent, false)
        return TotalViewHolder(view)
    }

    override fun onBindViewHolder(holder: TotalViewHolder, position: Int) {
        val total = totals[position]
        val category = categories.find { it.id == total.categoryId }
        holder.tvCategoryName.text = category?.name ?: "Unknown"
        holder.tvTotal.text = "R ${"%.2f".format(total.total)}"
    }

    override fun getItemCount() = totals.size

    fun update(newCategories: MutableList<Category>, newTotals: MutableList<CategoryTotal>) {
        categories.clear()
        categories.addAll(newCategories)
        totals.clear()
        totals.addAll(newTotals)
        notifyDataSetChanged()
    }
}