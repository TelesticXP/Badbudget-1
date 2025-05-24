package com.example.badbudget.models

data class GamificationStats(
    val userId: String = "",
    val lastLoginDate: String = "",
    val loginStreak: Int = 0,
    val points: Int = 0,
    val badges: List<String> = emptyList()
)
