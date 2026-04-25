package com.example.maliflow

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.maliflow.data.database.AppDatabase
import com.example.maliflow.data.model.Goal
import com.example.maliflow.databinding.ActivityGoalsBinding
import kotlinx.coroutines.launch

class GoalsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoalsBinding
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("USER_ID", -1)
        val db = AppDatabase.getDatabase(this)

        binding.btnSaveGoal.setOnClickListener {
            val month = binding.etMonth.text.toString().trim()
            val minStr = binding.etMinGoal.text.toString().trim()
            val maxStr = binding.etMaxGoal.text.toString().trim()

            if (month.isEmpty() || minStr.isEmpty() || maxStr.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val min = minStr.toDoubleOrNull() ?: run {
                Toast.makeText(this, "Invalid minimum goal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val max = maxStr.toDoubleOrNull() ?: run {
                Toast.makeText(this, "Invalid maximum goal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (min >= max) {
                Toast.makeText(this, "Minimum must be less than maximum", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                db.goalDao().insert(Goal(
                    userId = userId,
                    month = month,
                    minGoal = min,
                    maxGoal = max
                ))
                runOnUiThread {
                    Toast.makeText(this@GoalsActivity, "Goal saved!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.etMonth.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val month = binding.etMonth.text.toString().trim()
                if (month.isNotEmpty()) {
                    db.goalDao().getGoalByMonth(userId, month).observe(this) { goal ->
                        if (goal != null) {
                            binding.tvCurrentGoal.text =
                                "Current goal for $month:\nMin: R${goal.minGoal} | Max: R${goal.maxGoal}"
                        } else {
                            binding.tvCurrentGoal.text = "No goal set for this month"
                        }
                    }
                }
            }
        }
    }
}