package com.example.maliflow

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maliflow.data.database.AppDatabase
import com.example.maliflow.data.model.Category
import com.example.maliflow.databinding.ActivityCategoryBinding
import kotlinx.coroutines.launch

class CategoryActivity : AppCompatActivity() {
    // This handles the categories used for organising expenses
    private lateinit var binding: ActivityCategoryBinding
    private var userId: Int = -1
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("USER_ID", -1)
        val db = AppDatabase.getDatabase(this)

        adapter = CategoryAdapter(
            onDelete = { category ->
                lifecycleScope.launch {
                    db.categoryDao().delete(category)
                }
            },
            onEditLimit = { category ->
                showEditLimitDialog(category, db)
            },
            onRolloverToggle = { category, isChecked ->
                lifecycleScope.launch {
                    db.categoryDao().update(category.copy(rolloverEnabled = isChecked))
                }
            }
        )

        binding.rvCategories.layoutManager = LinearLayoutManager(this)
        binding.rvCategories.adapter = adapter

        // Observe categories and update the adapter using submitList
        db.categoryDao().getCategoriesByUser(userId).observe(this) { list ->
            adapter.submitList(list)
        }

        binding.btnAddCategory.setOnClickListener {
            val name = binding.etCategoryName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Enter a category name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                db.categoryDao().insert(Category(name = name, userId = userId))
                runOnUiThread {
                    binding.etCategoryName.text?.clear()
                }
            }
        }
    }

    // Shows a small dialog letting the user set or update a category's monthly budget limit
    private fun showEditLimitDialog(category: Category, db: AppDatabase) {
        val input = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "e.g. 500"
            if (category.budgetLimit > 0.0) {
                setText(category.budgetLimit.toString())
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Set monthly limit for ${category.name}")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val limitText = input.text.toString().trim()
                val limit = limitText.toDoubleOrNull() ?: 0.0

                lifecycleScope.launch {
                    db.categoryDao().update(category.copy(budgetLimit = limit))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
