package com.example.badbudget

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import android.widget.RatingBar
import com.example.badbudget.R
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.example.badbudget.R.id

class AwardsActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var textStreak: TextView
    private lateinit var textBadges: TextView
    private lateinit var progressPoints: CircularProgressIndicator
    private lateinit var ratingBar: RatingBar
    private lateinit var avatarView: ImageView
    private lateinit var medals: List<ImageView>
    private val badgeOrder = listOf(
        "first_budget",
        "first_expense",
        "five_budgets",
        "ten_expenses",
        "receipt_keeper",
        "first_category",
        "five_categories",
        "insights_viewer"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_awards)
        backButton = findViewById(id.backButton)
        textStreak = findViewById(R.id.textStreak)
        textBadges = findViewById(R.id.textBadges)
        progressPoints = findViewById(R.id.progressPoints)
        ratingBar = findViewById(R.id.ratingBar)
        avatarView = findViewById(R.id.imageAvatar)

        textBadges = findViewById(R.id.textBadges)
        progressPoints = findViewById(R.id.progressPoints)
        ratingBar = findViewById(R.id.ratingBar)
        medals = listOf(
            findViewById(R.id.medal1),
            findViewById(R.id.medal2),
            findViewById(R.id.medal3),
            findViewById(R.id.medal4),
            findViewById(R.id.medal5),
            findViewById(R.id.medal6),
            findViewById(R.id.medal7),
            findViewById(R.id.medal8)
        )

        backButton.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
        loadStats()
    }

    private fun loadStats() {
        val uid = UserSession.id(this)
        FirestoreService.getStats(uid) { stats ->
            val s = stats ?: return@getStats
            textStreak.text = "\uD83D\uDD25 ${s.loginStreak}-day streak!"
            textBadges.text = s.badges.joinToString(", ")
            progressPoints.progress = s.points.coerceAtMost(100)
            ratingBar.rating = when {
                s.points >= 80 -> 5f
                s.points >= 60 -> 4f
                s.points >= 40 -> 3f
                s.points >= 20 -> 2f
                else -> 1f
            }
            val avatarRes = when (ratingBar.rating.toInt()) {
                5 -> R.drawable.avatar_five_star
                4 -> R.drawable.avatar_four_star
                3 -> R.drawable.avatar_three_star
                2 -> R.drawable.avatar_two_star
                else -> R.drawable.avatar_one_star
            }
            avatarView.setImageResource(avatarRes)

            badgeOrder.forEachIndexed { index, key ->
                val img = medals.getOrNull(index) ?: return@forEachIndexed
                val res = if (s.badges.contains(key)) R.drawable.gold else R.drawable.silver
                img.setImageResource(res)
            }
        }
    }
}
