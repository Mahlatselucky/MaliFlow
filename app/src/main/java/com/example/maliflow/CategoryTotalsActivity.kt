package com.example.maliflow

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maliflow.data.database.AppDatabase
import com.example.maliflow.databinding.ActivityCategoryTotalsBinding

class CategoryTotalsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryTotalsBinding
    private var userId: Int = -1
    private lateinit var adapter: CategoryTotalsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryTotalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("USER_ID", -1)
        val db = AppDatabase.getDatabase(this)

        adapter = CategoryTotalsAdapter(mutableListOf(), mutableListOf())
        binding.rvCategoryTotals.layoutManager = LinearLayoutManager(this)
        binding.rvCategoryTotals.adapter = adapter

        binding.btnFilter.setOnClickListener {
            val startDate = binding.etStartDate.text.toString().trim()
            val endDate = binding.etEndDate.text.toString().trim()

            if (startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(this, "Please enter both dates", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.categoryDao().getCategoriesByUser(userId).observe(this) { categories ->
                db.expenseEntryDao().getCategoryTotals(userId, startDate, endDate)
                    .observe(this) { totals ->
                        adapter.update(categories.toMutableList(), totals.toMutableList())
                    }
            }
        }
    }
}