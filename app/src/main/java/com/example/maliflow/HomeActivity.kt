package com.example.maliflow

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.maliflow.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private var userId: Int = -1
    private var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("USER_ID", -1)
        username = intent.getStringExtra("USERNAME") ?: "User"

        binding.tvUsername.text = username

        binding.btnCategories.setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }
        userId = intent.getIntExtra("USER_ID", -1)
        username = intent.getStringExtra("USERNAME") ?: "User"

        binding.tvUsername.text = username

        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good morning,"
            hour < 17 -> "Good afternoon,"
            else -> "Good evening,"
        }

        binding.tvWelcome.text = greeting
        binding.btnAddEntry.setOnClickListener {
            val intent = Intent(this, AddEntryActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        binding.btnViewEntries.setOnClickListener {
            val intent = Intent(this, ViewEntriesActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        binding.btnGoals.setOnClickListener {
            val intent = Intent(this, GoalsActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        binding.btnCategoryTotals.setOnClickListener {
            val intent = Intent(this, CategoryTotalsActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            finish()
        }
    }
}