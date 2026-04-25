package com.example.maliflow

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maliflow.data.database.AppDatabase
import com.example.maliflow.data.model.Category
import com.example.maliflow.databinding.ActivityCategoryBinding
import kotlinx.coroutines.launch

class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private var userId: Int = -1
    private val categories = mutableListOf<Category>()
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("USER_ID", -1)
        val db = AppDatabase.getDatabase(this)

        adapter = CategoryAdapter(categories) { category ->
            lifecycleScope.launch {
                db.categoryDao().delete(category)
            }
        }

        binding.rvCategories.layoutManager = LinearLayoutManager(this)
        binding.rvCategories.adapter = adapter

        db.categoryDao().getCategoriesByUser(userId).observe(this) { list ->
            categories.clear()
            categories.addAll(list)
            adapter.notifyDataSetChanged()
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
}