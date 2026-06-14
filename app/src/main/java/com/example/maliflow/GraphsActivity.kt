package com.example.maliflow

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.maliflow.data.database.AppDatabase
import com.example.maliflow.data.database.MaliFlowApplication
import com.example.maliflow.databinding.ActivityGraphsBinding
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class GraphsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGraphsBinding
    private var startDate: String = ""
    private var endDate: String = ""
    private var userId: Int = -1

    private val db: AppDatabase by lazy {
        (application as MaliFlowApplication).database
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGraphsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("USER_ID", -1)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnStartDate.setOnClickListener {
            Log.d("GraphsActivity", "Start date button clicked")
            showDatePicker { date ->
                startDate = date
                binding.btnStartDate.text = date
            }
        }

        binding.btnEndDate.setOnClickListener {
            Log.d("GraphsActivity", "End date button clicked")
            showDatePicker { date ->
                endDate = date
                binding.btnEndDate.text = date
            }
        }

        binding.btnLoad.setOnClickListener {
            Log.d("GraphsActivity", "Load button clicked - startDate=$startDate, endDate=$endDate")
            if (binding.pieChart.visibility == View.VISIBLE) {
                loadPieChart()
            } else {
                loadBarChart()
            }
        }
        
        binding.btnTabBar.setOnClickListener {
            binding.barChart.visibility = View.VISIBLE
            binding.pieChart.visibility = View.GONE
        }

        binding.btnTabPie.setOnClickListener {
            binding.barChart.visibility = View.GONE
            binding.pieChart.visibility = View.VISIBLE
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            onDateSelected(formattedDate)
        }, year, month, day).show()
    }

    private fun loadBarChart() {
        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please select start and end dates", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val dailyTotals = db.expenseEntryDao().getDailyTotals(userId, startDate, endDate)
                val monthKey = if (startDate.length >= 7) startDate.substring(0, 7) else ""
                val goal = if (monthKey.isNotEmpty()) db.goalDao().getGoalByMonthSync(userId, monthKey) else null

                if (dailyTotals.isEmpty()) {
                    Toast.makeText(this@GraphsActivity, "No data for selected period", Toast.LENGTH_SHORT).show()
                    binding.barChart.clear()
                    return@launch
                }

                val entries = dailyTotals.mapIndexed { index, daily ->
                    BarEntry(index.toFloat(), daily.total.toFloat())
                }

                val labels = dailyTotals.map { if (it.date.length >= 10) it.date.substring(5) else it.date }

                val dataSet = BarDataSet(entries, "Daily Spending").apply {
                    colors = ColorTemplate.MATERIAL_COLORS.toList()
                    valueTextColor = Color.BLACK
                    valueTextSize = 11f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String = "R${value.toInt()}"
                    }
                }

                binding.barChart.apply {
                    data = BarData(dataSet)
                    description.isEnabled = false
                    setBackgroundColor(Color.WHITE)
                    setFitBars(true)

                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        valueFormatter = IndexAxisValueFormatter(labels)
                        granularity = 1f
                        setDrawGridLines(false)
                        textColor = Color.BLACK
                    }

                    axisLeft.apply {
                        axisMinimum = 0f
                        textColor = Color.BLACK
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String = "R${value.toInt()}"
                        }
                        removeAllLimitLines()
                        goal?.let {
                            val minLine = LimitLine(it.minGoal.toFloat(), "Min Goal").apply {
                                lineColor = Color.GREEN
                                lineWidth = 2f
                                textColor = Color.BLACK
                            }
                            val maxLine = LimitLine(it.maxGoal.toFloat(), "Max Goal").apply {
                                lineColor = Color.RED
                                lineWidth = 2f
                                textColor = Color.BLACK
                            }
                            addLimitLine(minLine)
                            addLimitLine(maxLine)
                        }
                    }
                    axisRight.isEnabled = false
                    legend.isEnabled = true
                    
                    animateY(1000)
                    invalidate()
                }
            } catch (e: Exception) {
                Log.e("GraphsActivity", "Error loading bar chart", e)
            }
        }
    }

    private fun loadPieChart() {
        if (startDate.isEmpty() || endDate.isEmpty()) return

        lifecycleScope.launch {
            val categoryTotals = db.expenseEntryDao().getCategoryTotalsSync(userId, startDate, endDate)
            if (categoryTotals.isEmpty()) {
                Toast.makeText(this@GraphsActivity, "No data for selected period", Toast.LENGTH_SHORT).show()
                binding.pieChart.clear()
                return@launch
            }

            // Get all categories so we can map IDs to real names
            val categories = db.categoryDao().getCategoriesByUserSync(userId)
            val categoryMap = categories.associateBy { it.id }

            val pieEntries = categoryTotals.map { total ->
                val categoryName = categoryMap[total.categoryId]?.name ?: "Unknown"
                PieEntry(total.total.toFloat(), categoryName)
            }

            val pieDataSet = PieDataSet(pieEntries, "Categories").apply {
                colors = ColorTemplate.JOYFUL_COLORS.toList()
                valueTextColor = Color.parseColor("#1c1a2e")
                valueTextSize = 13f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "R${value.toInt()}"
                    }
                }
            }

            binding.pieChart.apply {
                data = PieData(pieDataSet)
                setBackgroundColor(Color.parseColor("#f4f2fb"))
                description.isEnabled = false
                isDrawHoleEnabled = true
                holeRadius = 40f
                setHoleColor(Color.parseColor("#f4f2fb"))
                setEntryLabelColor(Color.parseColor("#1c1a2e"))
                setEntryLabelTextSize(12f)
                legend.apply {
                    isEnabled = true
                    textColor = Color.parseColor("#1c1a2e")
                    textSize = 12f
                }
                setExtraOffsets(16f, 8f, 16f, 8f)
                animateY(500)
                invalidate()
            }
        }
    }
}
