package com.example.badbudget

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object FirestoreService {
    private val db = Firebase.firestore

    // ─── Expenses ───────────────────────────────────────────────────────

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
            // new
            collection.add(exp.copy(id = ""))
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        } else {
            // update existing
            collection.document(exp.id)
                .set(exp)
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        }
    }

    // ─── Budgets ────────────────────────────────────────────────────────

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
            // new
            collection.add(b.copy(id = ""))
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        } else {
            // update
            collection.document(b.id)
                .set(b)
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        }
    }

    // ─── Categories ─────────────────────────────────────────────────────

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
            // new
            collection.add(cat.copy(id = ""))
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        } else {
            // update
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
}
