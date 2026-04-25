package com.example.maliflow

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maliflow.data.database.AppDatabase
import com.example.maliflow.databinding.ActivityViewEntriesBinding

class ViewEntriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewEntriesBinding
    private var userId: Int = -1
    private lateinit var adapter: EntryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewEntriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("USER_ID", -1)
        val db = AppDatabase.getDatabase(this)

        adapter = EntryAdapter(mutableListOf())
        binding.rvEntries.layoutManager = LinearLayoutManager(this)
        binding.rvEntries.adapter = adapter

        binding.btnFilter.setOnClickListener {
            val startDate = binding.etStartDate.text.toString().trim()
            val endDate = binding.etEndDate.text.toString().trim()

            if (startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(this, "Please enter both dates", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.expenseEntryDao().getEntriesByPeriod(userId, startDate, endDate)
                .observe(this) { entries ->
                    adapter.updateEntries(entries.toMutableList())
                }
        }
    }
}