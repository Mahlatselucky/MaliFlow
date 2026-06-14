package com.example.maliflow

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.maliflow.data.database.AppDatabase
import com.example.maliflow.data.database.MaliFlowApplication
import com.example.maliflow.data.model.GamificationHelper
import com.example.maliflow.databinding.ActivityRewardsBinding
import kotlinx.coroutines.launch

// Represents a single badge: its name, description, and the condition that unlocks it
data class Badge(
    val emoji: String,
    val title: String,
    val description: String,
    val target: Int,
    val currentValue: Int
) {
    val isUnlocked: Boolean get() = currentValue >= target
    val progressPercent: Int get() = ((currentValue.toFloat() / target) * 100).toInt().coerceAtMost(100)
}

class RewardsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRewardsBinding
    private var userId: Int = -1

    private val db: AppDatabase by lazy {
        (application as MaliFlowApplication).database
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRewardsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("USER_ID", -1)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        loadRewards()
    }

    private fun loadRewards() {
        lifecycleScope.launch {
            // Updated to correct method name in FlowPointsDao
            val flowPoints = db.flowPointsDao().getFlowPointsByUserId(userId)

            val points = flowPoints?.points ?: 0
            val streak = flowPoints?.streak ?: 0
            val totalLogged = flowPoints?.totalExpensesLogged ?: 0

            val level = GamificationHelper.getLevel(points)
            val toNext = GamificationHelper.pointsToNextLevel(points)

            binding.tvPoints.text = points.toString()
            binding.tvLevel.text = level.toString()
            binding.tvStreak.text = streak.toString()
            binding.tvTotalLogged.text = totalLogged.toString()
            binding.tvNextLevel.text = "$toNext points to Level ${level + 1}"

            buildBadges(points, streak, totalLogged)
        }
    }

    // Builds the badge list and adds a card for each one
    private fun buildBadges(points: Int, streak: Int, totalLogged: Int) {
        val badges = listOf(
            Badge("🌱", "First Step", "Log your first expense", target = 1, currentValue = totalLogged),
            Badge("📝", "Getting Started", "Log 10 expenses", target = 10, currentValue = totalLogged),
            Badge("📊", "Dedicated Tracker", "Log 50 expenses", target = 50, currentValue = totalLogged),
            Badge("🔥", "3 Day Streak", "Log expenses for 3 days in a row", target = 3, currentValue = streak),
            Badge("🔥", "Week Warrior", "Log expenses for 7 days in a row", target = 7, currentValue = streak),
            Badge("🏆", "Monthly Master", "Log expenses for 30 days in a row", target = 30, currentValue = streak),
            Badge("⭐", "Rising Star", "Reach Level 3", target = 200, currentValue = points),
            Badge("👑", "Flow Royalty", "Reach Level 5", target = 400, currentValue = points)
        )

        binding.badgeContainer.removeAllViews()

        for (badge in badges) {
            binding.badgeContainer.addView(createBadgeCard(badge))
        }
    }

    // Builds a single badge card view
    private fun createBadgeCard(badge: Badge): CardView {
        val card = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 12 }
            radius = 32f
            cardElevation = 4f
            setCardBackgroundColor(Color.WHITE)
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(40, 32, 40, 32)
            gravity = Gravity.CENTER_VERTICAL
        }

        // Emoji icon - greyed out if locked
        val emojiText = TextView(this).apply {
            text = badge.emoji
            textSize = 32f
            alpha = if (badge.isUnlocked) 1.0f else 0.25f
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).also {
                it.marginEnd = 32
            }
        }

        // Text column: title, description, progress bar if locked
        val textColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val titleText = TextView(this).apply {
            text = badge.title
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(if (badge.isUnlocked) Color.parseColor("#1c1a2e") else Color.parseColor("#9e9e9e"))
        }

        val descText = TextView(this).apply {
            text = badge.description
            textSize = 12f
            setTextColor(Color.parseColor("#9e9e9e"))
            alpha = 0.8f
        }

        textColumn.addView(titleText)
        textColumn.addView(descText)

        if (!badge.isUnlocked) {
            val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 10
                ).also { it.topMargin = 12 }
                max = 100
                progress = badge.progressPercent
                progressTintList = ColorStateList.valueOf(Color.parseColor("#c9a84c"))
            }

            val progressLabel = TextView(this).apply {
                text = "${badge.currentValue} / ${badge.target}"
                textSize = 10f
                setTextColor(Color.parseColor("#9e9e9e"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.topMargin = 4 }
            }

            textColumn.addView(progressBar)
            textColumn.addView(progressLabel)
        } else {
            val unlockedText = TextView(this).apply {
                text = "✓ Unlocked"
                textSize = 11f
                setTextColor(Color.parseColor("#4CAF50"))
                setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.topMargin = 4 }
            }
            textColumn.addView(unlockedText)
        }

        row.addView(emojiText)
        row.addView(textColumn)
        card.addView(row)

        return card
    }
}
