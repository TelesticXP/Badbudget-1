package com.example.badbudget

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.badbudget.models.CategorySpend
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SpendSummaryFragment : Fragment() {
    private lateinit var btnStart: MaterialButton
    private lateinit var btnEnd: MaterialButton
    private lateinit var tvRange: TextView
    private lateinit var adapter: CategorySpendAdapter

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var startDate = ""
    private var endDate = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_spend_summary, container, false)
        btnStart = v.findViewById(R.id.btnStartDate)
        btnEnd = v.findViewById(R.id.btnEndDate)
        tvRange = v.findViewById(R.id.tvRange)
        adapter = CategorySpendAdapter()
        v.findViewById<RecyclerView>(R.id.recyclerViewSpend).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SpendSummaryFragment.adapter
        }
        val cal = Calendar.getInstance()
        val pickListener = { isStart: Boolean ->
            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    cal.set(y, m, d)
                    val chosen = sdf.format(cal.time)
                    if (isStart) startDate = chosen else endDate = chosen
                    updateUi()
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        btnStart.setOnClickListener { pickListener(true) }
        btnEnd.setOnClickListener { pickListener(false) }
        return v
    }

    private fun updateUi() {
        tvRange.text = if (startDate.isNotEmpty() && endDate.isNotEmpty())
            "$startDate  →  $endDate" else "—"
        if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
            FirestoreService.getExpenses(UserSession.id(requireContext())) { all ->
                val filtered = all.filter { it.date in startDate..endDate }
                val catTotals = filtered
                    .groupBy { it.category }
                    .map { (cat, list) -> CategorySpend(cat, list.sumOf { it.amount }) }
                if (catTotals.isEmpty()) {
                    Toast.makeText(context, "No expenses in that period", Toast.LENGTH_SHORT).show()
                }
                adapter.submitList(catTotals)
            }
        }
    }
}
