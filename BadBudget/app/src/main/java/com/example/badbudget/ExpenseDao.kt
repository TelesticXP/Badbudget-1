//package com.example.badbudget
//
//import androidx.room.Dao
//import androidx.room.Insert
//import androidx.room.Query
//import com.example.badbudget.CategoryTotal
//import com.example.badbudget.Expense
//import com.example.badbudget.model.CategorySpend
//
//@Dao
//interface ExpenseDao {
//    @Insert
//    fun insertExpense(expense: Expense)
//
//    @Query("SELECT * FROM expenses WHERE userId = :uid ORDER BY date DESC, startTime ASC")
//    fun getAllExpenses(uid: String): List<Expense>
//
//    @Query(
//        "SELECT * FROM expenses WHERE date >= :startDate AND date <= :endDate AND userId = :uid ORDER BY date DESC, startTime ASC"
//    )
//    fun getExpensesBetween(startDate: String, endDate: String, uid: String): List<Expense>
//
//    @Query(
//        "SELECT category AS category, SUM(amount) AS total " +
//                "FROM expenses " +
//                "WHERE date >= :startDate AND date <= :endDate AND userId = :uid " +
//                "GROUP BY category " +
//                "ORDER BY total DESC"
//    )
//    fun getCategorySpendBetween(startDate: String, endDate: String, uid: String): List<CategorySpend>
//
//    @Query(
//        "SELECT category, COALESCE(SUM(amount),0) AS total " +
//            "FROM expenses " +
//                "WHERE userId = :uid " +
//            "GROUP BY category"
//    )
//    fun totalsByCategory(uid: String): List<CategoryTotal>
//
//    @Insert
//    fun insert(expense: Expense)
//}