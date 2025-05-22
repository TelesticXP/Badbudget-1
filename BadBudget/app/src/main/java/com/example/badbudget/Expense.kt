package com.example.badbudget

data class Expense(
    val id: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val date: String = "",
    val startTime: String? = null,
    val endTime: String? = null,
    val description: String? = null,
    val receiptPath: String? = null,
    val userId: String = ""
)
