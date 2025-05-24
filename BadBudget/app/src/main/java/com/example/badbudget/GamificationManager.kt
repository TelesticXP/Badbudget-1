package com.example.badbudget

import android.content.Context
import com.example.badbudget.models.GamificationStats
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object GamificationManager {

    fun recordLogin(context: Context) {
        val userId = UserSession.id(context)
        if (userId.isBlank()) return

        FirestoreService.getStats(userId) { current ->
            val today = LocalDate.now()
            val lastDate = current?.lastLoginDate?.let {
                runCatching { LocalDate.parse(it) }.getOrNull()
            }
            val newStreak = when {
                lastDate == null -> 1
                lastDate.plusDays(1) == today -> current!!.loginStreak + 1
                lastDate == today -> current!!.loginStreak
                else -> 1
            }

            var badges = current?.badges ?: emptyList()
            var points = (current?.points ?: 0) + 1
            if (newStreak >= 7 && !badges.contains("streak_7")) {
                badges = badges + "streak_7"
                points += 15
            }

            val updated = (current ?: GamificationStats(userId = userId)).copy(
                lastLoginDate = today.format(DateTimeFormatter.ISO_DATE),
                loginStreak = newStreak,
                points = points,
                badges = badges
            )
            FirestoreService.saveStats(updated){}
        }
    }

    fun onBudgetAdded(context: Context) {
        updateBadge(context, "first_budget", 10)
    }

    fun onExpenseLogged(context: Context) {
        updateBadge(context, "first_expense", 5)
    }

    private fun updateBadge(context: Context, badge: String, pts: Int) {
        val userId = UserSession.id(context)
        if (userId.isBlank()) return
        FirestoreService.getStats(userId) { current ->
            val stats = current ?: GamificationStats(userId = userId)
            if (!stats.badges.contains(badge)) {
                val updated = stats.copy(
                    badges = stats.badges + badge,
                    points = stats.points + pts
                )
                FirestoreService.saveStats(updated){}
            }
        }
    }
}
