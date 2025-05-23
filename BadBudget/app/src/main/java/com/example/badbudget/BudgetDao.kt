//package com.example.badbudget
//
//import androidx.room.*
//import com.example.badbudget.Models.Budget
//
//@Dao
//interface BudgetDao {
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insertBudget(budget: Budget)
//
//    @Update
//    fun updateBudget(budget: Budget)
//
//    @Query("SELECT * FROM budgets WHERE userId = :uid ORDER BY category ASC")
//    fun getAllBudgets(uid: String): List<Budget>
//
//    @Query("SELECT DISTINCT category FROM budgets WHERE userId = :uid")
//    fun getCategories(uid: String): List<String>
//}
