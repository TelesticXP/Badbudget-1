package com.example.badbudget

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.example.badbudget.R.id

class AwardsActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var textStreak: TextView
    private lateinit var medalBudget: ImageView
    private lateinit var medalExpense: ImageView
    private lateinit var medalStreak: ImageView
    private lateinit var textBadges: TextView
    private lateinit var progressPoints: CircularProgressIndicator
    private lateinit var ratingBar: RatingBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_awards)
        backButton = findViewById(id.backButton)
        textStreak = findViewById(R.id.textStreak)
        medalBudget = findViewById(R.id.medalBudget)
        medalExpense = findViewById(R.id.medalExpense)
        medalStreak = findViewById(R.id.medalStreak)
        textBadges = findViewById(R.id.textBadges)
        progressPoints = findViewById(R.id.progressPoints)
        ratingBar = findViewById(R.id.ratingBar)

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

            medalBudget.setImageResource(if (s.badges.contains("first_budget")) R.drawable.gold else R.drawable.silver)
            medalExpense.setImageResource(if (s.badges.contains("first_expense")) R.drawable.gold else R.drawable.silver)
            medalStreak.setImageResource(if (s.badges.contains("streak_7")) R.drawable.gold else R.drawable.silver)
        }
    }
}
