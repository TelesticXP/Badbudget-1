package com.example.badbudget.models
// GameificationStats data class
data class GamificationStats(
    val userId: String = "",
    val monthKey: String = "",
    val lastLoginDate: String = "",
    val loginStreak: Int = 0,
    val weekStreaks: Int = 0,
    val points: Int = 0,
    val badges: List<String> = emptyList()
)