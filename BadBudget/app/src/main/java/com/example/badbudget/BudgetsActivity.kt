/*
References:

1. Create dynamic lists with RecyclerView
   Author: Android Developers
   Date: May 20, 2025
   URL: https://developer.android.com/develop/ui/views/layout/recyclerview

2. Title: Intents and intent filters
   Author: Android Developers
   Date: May 20, 2025
   URL: https://developer.android.com/guide/components/intents-filters
*/

package com.example.badbudget

import BudgetAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.badbudget.models.Budget

class BudgetsActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var editTextBudgetCategory: EditText
    private lateinit var editTextBudgetMin: EditText
    private lateinit var editTextBudgetMax: EditText
    private lateinit var buttonAddBudget: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BudgetAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        // back arrow
        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        // form fields
        editTextBudgetCategory = findViewById(R.id.editTextBudgetCategory)
        editTextBudgetMin = findViewById(R.id.editTextBudgetMin)
        editTextBudgetMax = findViewById(R.id.editTextBudgetMax)
        buttonAddBudget = findViewById(R.id.buttonAddBudget)

        // RecyclerView and adapter
        recyclerView = findViewById(R.id.recyclerViewBudgets)
        adapter = BudgetAdapter(emptyList()) { budget ->
            showEditDialog(budget)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@BudgetsActivity)
            adapter = this@BudgetsActivity.adapter
        }

        // initial load
        loadBudgets()

        // save button
        buttonAddBudget.setOnClickListener {
            val cat = editTextBudgetCategory.text.toString().trim()
            val minTx = editTextBudgetMin.text.toString().trim()
            val maxTx = editTextBudgetMax.text.toString().trim()

            if (cat.isEmpty() || maxTx.isEmpty()) {
                Toast.makeText(this, "Please fill category and max goal", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val min = minTx.toDoubleOrNull() ?: 0.0
            val max = maxTx.toDouble()
            val budget = Budget(
                category = cat,
                minAmount = min,
                maxAmount = max,
                userId = UserSession.id(this)
            )

            FirestoreService.addOrUpdateBudget(budget) { success ->
                if (success) {
                    Toast.makeText(this, "Budget saved", Toast.LENGTH_SHORT).show()
                    GamificationManager.onBudgetAdded(this)
                    // clear input
                    editTextBudgetCategory.text.clear()
                    editTextBudgetMin.text.clear()
                    editTextBudgetMax.text.clear()
                    loadBudgets()
                } else {
                    Toast.makeText(this, "Failed to save budget", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadBudgets() {
        val uid = UserSession.id(this)
        FirestoreService.getBudgets(uid) { budgets ->
            FirestoreService.getExpenses(uid) { expenses ->
                val spentMap = expenses
                    .groupBy { it.category }
                    .mapValues { it.value.sumOf { e -> e.amount } }
                val withSpent = budgets.map { b ->
                    b.copy(spentAmount = spentMap[b.category] ?: 0.0)
                }
                adapter.setBudgets(withSpent)
            }
        }
    }

    private fun showEditDialog(budget: Budget) {
        val view = layoutInflater.inflate(R.layout.dialog_budget, null)
        val category = view.findViewById<EditText>(R.id.editTextBudgetCategory)
        val min = view.findViewById<EditText>(R.id.editTextBudgetMin)
        val max = view.findViewById<EditText>(R.id.editTextBudgetMax)

        // pre-fill
        category.setText(budget.category)
        min.setText(budget.minAmount.toString())
        max.setText(budget.maxAmount.toString())

        AlertDialog.Builder(this)
            .setTitle("Edit Budget")
            .setView(view)
            .setPositiveButton("Save") { dialog, _ ->
                val newCat = category.text.toString().trim()
                val newMin = min.text.toString().toDoubleOrNull() ?: 0.0
                val newMax = max.text.toString().toDoubleOrNull() ?: 0.0

                val updated = budget.copy(
                    category = newCat,
                    minAmount = newMin,
                    maxAmount = newMax
                )

                FirestoreService.addOrUpdateBudget(updated) { success ->
                    if (success) loadBudgets()
                    else Toast.makeText(
                        this,
                        "Failed to update budget",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
