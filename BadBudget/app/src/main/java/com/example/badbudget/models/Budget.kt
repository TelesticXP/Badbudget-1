package com.example.badbudget.models

data class Budget(
    val id: String = "",
    val category: String = "",
    val minAmount: Double = 0.0,
    val maxAmount: Double = 0.0,
    val spentAmount: Double = 0.0,
    val userId: String = ""
)