//package com.example.badbudget
//
//import androidx.room.Dao
//import androidx.room.Insert
//import androidx.room.Query
//import androidx.room.Delete
//
//@Dao
//interface CategoryDao {
//    @Insert
//    fun insertCategory(category: Category)
//
//    @Delete
//    fun deleteCategory(category: Category)
//
//    @Query("SELECT * FROM categories ORDER BY name ASC")
//    fun getAllCategories(): List<Category>
//}