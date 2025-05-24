package com.example.badbudget

import com.example.badbudget.models.UserStats
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object StatisticsService {
    private val db = Firebase.firestore
    private val fmt = DateTimeFormatter.ISO_DATE

    fun getUserStats(userId: String, callback: (UserStats) -> Unit) {
        db.collection("stats")
            .document(userId)
            .get()
            .addOnSuccessListener { snap ->
                val stats = snap.toObject(UserStats::class.java)?.copy(userId = userId)
                    ?: UserStats(userId = userId)
                callback(stats)
            }
            .addOnFailureListener {
                // return empty if user not found
                callback(UserStats(userId = userId))
            }
    }

    fun updateUserStats(stats: UserStats, onComplete: (Boolean) -> Unit) {
        db.collection("userStats")
            .document(stats.userId)
            .set(stats)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // handles streaks and gives 10 points
    fun recordLogin(userId: String, onComplete: (UserStats) -> Unit) {
        getUserStats(userId) { stats ->
            val today = LocalDate.now().format(fmt)
            val yesterday = LocalDate.now().minusDays(1).format(fmt)
            val newStreak = if (stats.lastLogin == yesterday) stats.streak + 1 else 1
            val newPoints = stats.points + 10
            val updated = stats.copy(lastLogin = today, streak = newStreak, points = newPoints)

            db.collection("stats")
                .document(userId)
                .set(updated)
                .addOnSuccessListener { onComplete(updated) }
                .addOnFailureListener { onComplete(stats) }
        }
    }

    // award a badge and 50 points
    fun awardBadge(userId: String, badge: String, onComplete: (Boolean) -> Unit) {
        getUserStats(userId) { stats ->
            if (stats.badges.contains(badge)) {
                onComplete(true)
                return@getUserStats
            }
            val updatedBadges = stats.badges + badge
            val updatedPoints = stats.points + 50
            val updated = stats.copy(badges = updatedBadges, points = updatedPoints)

            db.collection("stats")
                .document(userId)
                .set(updated)
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        }
    }
}
