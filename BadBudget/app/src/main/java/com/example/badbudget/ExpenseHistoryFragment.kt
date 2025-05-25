package com.example.badbudget

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.LocalDate

class ExpenseHistoryFragment : Fragment(), ExpenseHistoryAdapter.Callback {
    private val historyAdapter = ExpenseHistoryAdapter(this)
    private val uid get() = UserSession.id(requireContext())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_expense_history, container, false)
        v.findViewById<RecyclerView>(R.id.recyclerViewHistory).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
        val fab = v.findViewById<FloatingActionButton>(R.id.fabAddExpense)
        fab.setOnClickListener {
            startActivity(Intent(requireContext(), AddExpenseActivity::class.java))
        }
        val today = LocalDate.now()
        val initRange = PeriodPickerHelper.Range(
            start = today.minusDays(7).toString(),
            end = today.toString()
        )
        PeriodPickerHelper.hook(
            v.findViewById(R.id.periodPicker),
            requireContext(),
            initRange
        ) { range ->
            loadExpenses(range)
        }
        loadExpenses(initRange)
        return v
    }

    private fun loadExpenses(r: PeriodPickerHelper.Range) {
        FirestoreService.getExpenses(uid) { all ->
            val filtered = all.filter { it.date in r.start..r.end }
            historyAdapter.submitList(filtered)
        }
    }

    override fun onOpenReceipt(path: String) {
        startActivity(
            Intent(requireContext(), ReceiptViewerActivity::class.java)
                .putExtra("path", path)
        )
    }
}
