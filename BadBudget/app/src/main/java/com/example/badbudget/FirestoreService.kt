/*
References:

1. Title: Get data with Cloud Firestore
   Author: Firebase
   Date: May 23, 2025
   URL: https://firebase.google.com/docs/firestore/query-data/get-data

2. Title: Add data to Cloud Firestore
   Author: Firebase
   Date: May 23, 2025
   URL: https://firebase.google.com/docs/firestore/manage-data/add-data

3. Title: Delete data from Cloud Firestore
   Author: Firebase
   Date: May 23, 2025
   URL: https://firebase.google.com/docs/firestore/manage-data/delete-data

4. Title: com.google.firebase.firestore.ktx
   Author: Firebase
   Date: October 18, 2023
   URL: https://firebase.google.com/docs/reference/kotlin/com/google/firebase/firestore/ktx/package-summary
*/

package com.example.badbudget

import com.example.badbudget.models.Budget
import com.example.badbudget.models.Category
import com.example.badbudget.models.Expense
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object FirestoreService {
    private val db = Firebase.firestore

    // expenses methods
    fun getExpenses(userId: String, callback: (List<Expense>) -> Unit) {
        db.collection("expenses")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { doc ->
                    doc.toObject(Expense::class.java)?.copy(id = doc.id)
                }
                callback(list)
            }
    }

    fun addExpense(exp: Expense, onComplete: (Boolean) -> Unit) {
        val collection = db.collection("expenses")
        if (exp.id.isBlank()) {
            // new record
            collection.add(exp.copy(id = ""))
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        } else {
            // update existing record
            collection.document(exp.id)
                .set(exp)
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        }
    }

    // budget methods
    fun getBudgets(userId: String, callback: (List<Budget>) -> Unit) {
        db.collection("budgets")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { doc ->
                    doc.toObject(Budget::class.java)?.copy(id = doc.id)
                }
                callback(list)
            }
    }

    fun addOrUpdateBudget(b: Budget, onComplete: (Boolean) -> Unit) {
        val collection = db.collection("budgets")
        if (b.id.isBlank()) {
            collection.add(b.copy(id = ""))
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        } else {
            collection.document(b.id)
                .set(b)
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        }
    }

    // category methods

    fun getCategories(userId: String, callback: (List<Category>) -> Unit) {
        db.collection("categories")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { doc ->
                    doc.toObject(Category::class.java)?.copy(id = doc.id)
                }
                callback(list)
            }
    }

    fun addCategory(cat: Category, onComplete: (Boolean) -> Unit) {
        val collection = db.collection("categories")
        if (cat.id.isBlank()) {
            collection.add(cat.copy(id = ""))
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        } else {
            collection.document(cat.id)
                .set(cat)
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        }
    }

    fun deleteCategory(categoryId: String, onComplete: (Boolean) -> Unit) {
        db.collection("categories")
            .document(categoryId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // gamification stats methods
    fun getStats(userId: String, callback: (com.example.badbudget.models.GamificationStats?) -> Unit) {
        db.collection("stats")
            .document(userId)
            .get()
            .addOnSuccessListener { snap ->
                callback(snap.toObject(com.example.badbudget.models.GamificationStats::class.java))
            }
            .addOnFailureListener { callback(null) }
    }

    fun saveStats(stats: com.example.badbudget.models.GamificationStats, onComplete: (Boolean) -> Unit) {
        db.collection("stats")
            .document(stats.userId)
            .set(stats)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
