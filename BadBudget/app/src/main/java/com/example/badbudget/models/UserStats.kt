package com.example.badbudget.models

data class UserStats(
    val userId: String = "",
    val lastLogin: String = "",
    val streak: Int = 0,
    val points: Int = 0,
    val badges: List<String> = emptyList()
)