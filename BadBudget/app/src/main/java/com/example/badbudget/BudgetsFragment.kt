package com.example.badbudget

import BudgetAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.badbudget.models.Budget

class BudgetsFragment : Fragment() {

    private lateinit var editTextBudgetCategory: EditText
    private lateinit var editTextBudgetMin: EditText
    private lateinit var editTextBudgetMax: EditText
    private lateinit var buttonAddBudget: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BudgetAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_budgets, container, false)
        editTextBudgetCategory = v.findViewById(R.id.editTextBudgetCategory)
        editTextBudgetMin = v.findViewById(R.id.editTextBudgetMin)
        editTextBudgetMax = v.findViewById(R.id.editTextBudgetMax)
        buttonAddBudget = v.findViewById(R.id.buttonAddBudget)
        recyclerView = v.findViewById(R.id.recyclerViewBudgets)
        adapter = BudgetAdapter(emptyList()) { budget ->
            showEditDialog(budget)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@BudgetsFragment.adapter
        }
        loadBudgets()
        buttonAddBudget.setOnClickListener {
            val cat = editTextBudgetCategory.text.toString().trim()
            val minTx = editTextBudgetMin.text.toString().trim()
            val maxTx = editTextBudgetMax.text.toString().trim()
            if (cat.isEmpty() || maxTx.isEmpty()) {
                Toast.makeText(context, "Please fill category and max goal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val min = minTx.toDoubleOrNull() ?: 0.0
            val max = maxTx.toDouble()
            val budget = Budget(
                category = cat,
                minAmount = min,
                maxAmount = max,
                userId = UserSession.id(requireContext())
            )
            FirestoreService.addOrUpdateBudget(budget) { success ->
                if (success) {
                    Toast.makeText(context, "Budget saved", Toast.LENGTH_SHORT).show()
                    GamificationManager.onBudgetAdded(requireContext())
                    editTextBudgetCategory.text.clear()
                    editTextBudgetMin.text.clear()
                    editTextBudgetMax.text.clear()
                    loadBudgets()
                } else {
                    Toast.makeText(context, "Failed to save budget", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return v
    }

    private fun loadBudgets() {
        val uid = UserSession.id(requireContext())
        FirestoreService.getBudgets(uid) { budgets ->
            FirestoreService.getExpenses(uid) { expenses ->
                val spentMap = expenses.groupBy { it.category }
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
        category.setText(budget.category)
        min.setText(budget.minAmount.toString())
        max.setText(budget.maxAmount.toString())
        AlertDialog.Builder(requireContext())
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
                    if (success) loadBudgets() else Toast.makeText(
                        context,
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
