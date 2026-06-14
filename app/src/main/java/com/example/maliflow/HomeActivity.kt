package com.example.maliflow

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.maliflow.data.database.AppDatabase
import com.example.maliflow.data.database.MaliFlowApplication
import com.example.maliflow.databinding.ActivityHomeBinding
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private var userId: Int = -1
    private var username: String = ""

    private val db: AppDatabase by lazy {
        (application as MaliFlowApplication).database
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize user data
        userId = intent.getIntExtra("USER_ID", -1)
        username = intent.getStringExtra("USERNAME") ?: "User"

        // Set UI values
        binding.tvUsername.text = username

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good morning,"
            hour < 17 -> "Good afternoon,"
            else -> "Good evening,"
        }
        binding.tvWelcome.text = greeting

        // Navigation Listeners
        binding.btnAddEntry.setOnClickListener {
            val intent = Intent(this, AddEntryActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        binding.btnCategories.setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        binding.btnViewEntries.setOnClickListener {
            val intent = Intent(this, ViewEntriesActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        binding.btnGraphs.setOnClickListener {
            val intent = Intent(this, GraphsActivity::class.java)
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

        binding.btnRewards.setOnClickListener {
            val intent = Intent(this, RewardsActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            finish()
        }
    }


    // Refresh dashboard every time the user comes back to this screen
    override fun onResume() {
        super.onResume()
        loadDashboard()
    }
    // Calculates last month's date range and returns yyyy-MM-dd start/end strings
    private fun getLastMonthRange(): Pair<String, String> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val start = String.format("%04d-%02d-01", year, month)
        val end = String.format("%04d-%02d-%02d", year, month, lastDay)
        return Pair(start, end)
    }

    // Loads the daily allowance and category progress bars
    private fun loadDashboard() {
        lifecycleScope.launch {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val monthKey = String.format(Locale.getDefault(), "%04d-%02d", year, month)

            // First and last day of the current month
            val startDate = String.format(Locale.getDefault(), "%04d-%02d-01", year, month)
            val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val endDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, lastDay)
            val today = calendar.get(Calendar.DAY_OF_MONTH)
            val daysLeft = (lastDay - today + 1).coerceAtLeast(1)

            // Total spent this month
            val categoryTotals = db.expenseEntryDao().getCategoryTotalsSync(userId, startDate, endDate)
            val totalSpent = categoryTotals.sumOf { it.total }

            // Overall monthly goal
            val goal = db.goalDao().getGoalByMonthSync(userId, monthKey)
            val maxGoal = goal?.maxGoal ?: 0.0

            // Daily allowance = (max goal - total spent) / days left
            val remaining = (maxGoal - totalSpent).coerceAtLeast(0.0)
            val dailyAllowance = if (maxGoal > 0) remaining / daysLeft else 0.0

            binding.tvDailyAllowance.text = "R${dailyAllowance.toInt()}"
            binding.tvAllowanceSubtitle.text = if (maxGoal > 0) {
                "R${remaining.toInt()} left over $daysLeft days"
            } else {
                "Set a monthly goal to see your daily allowance"
            }

            // Build category progress bars
            val categories = db.categoryDao().getCategoriesByUserSync(userId)
            val totalsMap = categoryTotals.associateBy({ it.categoryId }, { it.total })

            binding.categoryProgressContainer.removeAllViews()

            // Only show categories that have a budget limit set
            val categoriesWithLimits = categories.filter { it.budgetLimit > 0.0 }

            if (categoriesWithLimits.isEmpty()) {
                val emptyText = TextView(this@HomeActivity).apply {
                    text = "Set monthly limits on your categories to see progress here"
                    textSize = 13f
                    setTextColor(Color.parseColor("#1c1a2e"))
                    alpha = 0.6f
                }
                binding.categoryProgressContainer.addView(emptyText)
                return@launch
            }

            for (category in categoriesWithLimits) {
                val spent = totalsMap[category.id] ?: 0.0
                val limit = category.budgetLimit
                val percentage = ((spent / limit) * 100).toInt().coerceAtMost(100)

                // Container for this category's row
                val rowLayout = LinearLayout(this@HomeActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(0, 8, 0, 8)
                }

                // Label row: category name + amount
                val labelRow = LinearLayout(this@HomeActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                val nameText = TextView(this@HomeActivity).apply {
                    text = category.name
                    textSize = 13f
                    setTextColor(Color.parseColor("#1c1a2e"))
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                // Color-code the amount text based on spending status
                val statusColor = when {
                    spent > limit -> Color.parseColor("#b85c5c") // red - over budget
                    spent >= limit * 0.8 -> Color.parseColor("#c9a84c") // amber - close to limit
                    else -> Color.parseColor("#4CAF50") // green - on track
                }

                val amountText = TextView(this@HomeActivity).apply {
                    text = "R${spent.toInt()} / R${limit.toInt()}"
                    textSize = 12f
                    setTextColor(statusColor)
                    gravity = Gravity.END
                }

                labelRow.addView(nameText)
                labelRow.addView(amountText)

                // Progress bar
                val progressBar = ProgressBar(this@HomeActivity, null, android.R.attr.progressBarStyleHorizontal).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        12
                    ).also { it.topMargin = 6 }
                    max = 100
                    progress = percentage
                    progressTintList = ColorStateList.valueOf(statusColor)
                }

                rowLayout.addView(labelRow)
                rowLayout.addView(progressBar)

                // Show "Over by RXX" text if over budget
                if (spent > limit) {
                    val overText = TextView(this@HomeActivity).apply {
                        text = "Over by R${(spent - limit).toInt()}"
                        textSize = 11f
                        setTextColor(Color.parseColor("#b85c5c"))
                        setTypeface(typeface, Typeface.BOLD)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).also { it.topMargin = 2 }
                    }
                    rowLayout.addView(overText)
                }

                binding.categoryProgressContainer.addView(rowLayout)
            }
        }
    }
}
