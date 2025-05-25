package com.example.badbudget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.progressindicator.CircularProgressIndicator

class AwardsFragment : Fragment() {
    private lateinit var textStreak: TextView
    private lateinit var progressPoints: CircularProgressIndicator
    private lateinit var ratingBar: RatingBar
    private lateinit var avatarView: ImageView
    private lateinit var medals: List<ImageView>

    private val badgeOrder = listOf(
        "first_budget", "first_expense", "five_budgets",
        "ten_expenses", "receipt_keeper", "first_category",
        "five_categories", "insights_viewer"
    )
    private val badgeNames = mapOf(
        "first_budget" to "First Budget",
        "first_expense" to "First Expense",
        "five_budgets" to "Five Budgets",
        "ten_expenses" to "Ten Expenses",
        "receipt_keeper" to "Receipt Keeper",
        "first_category" to "First Category",
        "five_categories" to "Five Categories",
        "insights_viewer" to "Insights Viewer"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_awards, container, false)
        textStreak = v.findViewById(R.id.textStreak)
        progressPoints = v.findViewById(R.id.progressPoints)
        ratingBar = v.findViewById(R.id.ratingBar)
        avatarView = v.findViewById(R.id.imageAvatar)
        medals = listOf(
            v.findViewById(R.id.medal1),
            v.findViewById(R.id.medal2),
            v.findViewById(R.id.medal3),
            v.findViewById(R.id.medal4),
            v.findViewById(R.id.medal5),
            v.findViewById(R.id.medal6),
            v.findViewById(R.id.medal7),
            v.findViewById(R.id.medal8)
        )
        loadStats()
        return v
    }

    private fun loadStats() {
        val uid = UserSession.id(requireContext())
        FirestoreService.getStats(uid) { stats ->
            val s = stats ?: return@getStats
            textStreak.text = "\uD83D\uDD25 ${s.loginStreak}-day streak!"
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
                img.setOnClickListener {
                    val name = badgeNames[key] ?: key.replaceFirstChar { it.uppercase() }.replace("_", " ")
                    Toast.makeText(context, name, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
