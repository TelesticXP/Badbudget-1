package com.example.badbudget

import android.content.Context
import com.example.badbudget.models.GamificationStats
import com.google.firebase.firestore.ktx.toObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter


object GamificationManager {

    private const val WEEK_POINTS = 5
    private val BADGE_POINTS = mapOf(
        "first_budget"    to 10,
        "first_expense"   to 10,
        "five_budgets"    to 10,
        "ten_expenses"    to 10,
        "receipt_keeper"  to 10,
        "first_category"  to 10,
        "five_categories" to 10,
        "insights_viewer" to 10,
        "streak_7"        to 15
    )

    private fun currentMonth() = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))

    private fun resetIfNeeded(stats: GamificationStats?): GamificationStats {
        val month = currentMonth()
        return if (stats == null || stats.monthKey != month) {
            GamificationStats(userId = stats?.userId ?: "", monthKey = month)
        } else stats
    }

    fun recordLogin(context: Context) {
        val userId = UserSession.id(context)
        if (userId.isBlank()) return

        FirestoreService.getStats(userId) { current ->
            var stats = resetIfNeeded(current?.copy(userId = userId))
            val today = LocalDate.now()
            val lastDate = stats.lastLoginDate.takeIf { it.isNotEmpty() }?.let {
                runCatching { LocalDate.parse(it) }.getOrNull()
            }

            val newStreak = when {
                lastDate == null -> 1
                lastDate.plusDays(1) == today -> stats.loginStreak + 1
                lastDate == today -> stats.loginStreak
                else -> 1
            }

            var weekStreaks = stats.weekStreaks
            var points = stats.points
            val earnedWeeks = newStreak / 7
            if (earnedWeeks > weekStreaks) {
                val add = earnedWeeks - weekStreaks
                weekStreaks = earnedWeeks
                points += WEEK_POINTS * add
            }

            var badges = stats.badges
            if (newStreak >= 7 && !badges.contains("streak_7")) {
                badges = badges + "streak_7"
                points += BADGE_POINTS["streak_7"] ?: 0
            }

            stats = stats.copy(
                monthKey = currentMonth(),
                lastLoginDate = today.format(DateTimeFormatter.ISO_DATE),
                loginStreak = newStreak,
                weekStreaks = weekStreaks,
                points = points,
                badges = badges
            )

            FirestoreService.saveStats(stats) {}
        }
    }

    fun onBudgetAdded(context: Context) {
        val uid = UserSession.id(context)
        FirestoreService.getBudgets(uid) { list ->
            if (list.isNotEmpty()) updateBadge(context, "first_budget")
            if (list.size >= 5) updateBadge(context, "five_budgets")
        }
    }

    fun onExpenseLogged(context: Context, hasReceipt: Boolean) {
        val uid = UserSession.id(context)
        FirestoreService.getExpenses(uid) { list ->
            if (list.isNotEmpty()) updateBadge(context, "first_expense")
            if (list.size >= 10) updateBadge(context, "ten_expenses")
            if (hasReceipt) updateBadge(context, "receipt_keeper")
        }
    }

    fun onCategoryAdded(context: Context) {
        val uid = UserSession.id(context)
        FirestoreService.getCategories(uid) { list ->
            if (list.isNotEmpty()) updateBadge(context, "first_category")
            if (list.size >= 5) updateBadge(context, "five_categories")
        }
    }

    fun onInsightsViewed(context: Context) {
        updateBadge(context, "insights_viewer")
    }

    private fun updateBadge(context: Context, badge: String) {
        val userId = UserSession.id(context)
        if (userId.isBlank()) return

        FirestoreService.getStats(userId) { current ->
            val stats = resetIfNeeded(current?.copy(userId = userId))
            if (!stats.badges.contains(badge)) {
                val pts = BADGE_POINTS[badge] ?: 0
                val updated = stats.copy(
                    monthKey = currentMonth(),
                    badges = stats.badges + badge,
                    points = stats.points + pts
                )
                FirestoreService.saveStats(updated) {}
            }
        }
    }
}
