package com.example.badbudget

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.badbudget.databinding.ActivityDashboardBinding
import com.example.badbudget.models.Budget
import com.example.badbudget.models.Expense
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DashboardFragment : Fragment() {

    private var _binding: ActivityDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }
        binding.buttonInsights.setOnClickListener {
            startActivity(Intent(requireContext(), InsightsActivity::class.java))
        }

        val act = requireActivity() as AppCompatActivity
        act.setSupportActionBar(binding.topAppBar)
        act.supportActionBar?.hide()
        binding.topAppBar.inflateMenu(R.menu.menu_dashboard)

        FirestoreService.getExpenses(UserSession.id(requireContext())) { allExpenses ->
            FirestoreService.getBudgets(UserSession.id(requireContext())) { budgets ->
                val catMap = allExpenses.groupBy { it.category }
                    .mapValues { it.value.sumOf { e -> e.amount } }
                updateSpendingPie(catMap)
                updateCompliancePie(allExpenses, budgets)
                val totalSpent = catMap.values.sum()
                val budgetMax = budgets.sumOf { it.maxAmount }
                updateSummaryCards(totalSpent, budgetMax)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateSpendingPie(catMap: Map<String, Double>) {
        if (catMap.isEmpty()) {
            binding.pieSpending.data = null
            return
        }
        val entries = catMap.map { (cat, amt) -> PieEntry(amt.toFloat(), cat) }
        val ds = PieDataSet(entries, "").apply {
            setDrawValues(false)
            sliceSpace = 2f
            colors = ColorTemplate.MATERIAL_COLORS.toList()
        }
        binding.pieSpending.apply {
            data = PieData(ds)
            description.isEnabled = false
            legend.apply {
                isEnabled = true
                isWordWrapEnabled = true
                horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
            }
            setDrawEntryLabels(false)
            invalidate()
        }
    }

    private fun updateCompliancePie(allExpenses: List<Expense>, budgets: List<Budget>) {
        val cutoff = LocalDate.now()
            .minusDays(30)
            .format(DateTimeFormatter.ISO_DATE)
        val recent = allExpenses.filter { it.date >= cutoff }
        val spentMap = recent.groupBy { it.category }
            .mapValues { it.value.sumOf { e -> e.amount } }

        var under = 0
        var onTrack = 0
        var over = 0
        budgets.forEach { b ->
            val spent = spentMap[b.category] ?: 0.0
            when {
                spent < b.minAmount -> under++
                spent > b.maxAmount -> over++
                else -> onTrack++
            }
        }

        val entries = listOf(
            PieEntry(under.toFloat(), "Under Min"),
            PieEntry(onTrack.toFloat(), "On Track"),
            PieEntry(over.toFloat(), "Over Max")
        )
        val grey = ContextCompat.getColor(requireContext(), R.color.chartGrey)
        val green = ContextCompat.getColor(requireContext(), android.R.color.holo_green_light)
        val red = ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)

        val ds = PieDataSet(entries, "").apply {
            setDrawValues(false)
            sliceSpace = 2f
            setColors(listOf(grey, green, red))
        }
        binding.pieCompliance.apply {
            data = PieData(ds)
            description.isEnabled = false
            legend.isEnabled = true
            setDrawEntryLabels(false)
            invalidate()
        }
    }

    private fun updateSummaryCards(spent: Double, max: Double) {
        binding.textTotalExpenses.text = "R ${spent.toInt()}"
        binding.textLastMonth.text =
            "Upcoming bills: Rent (5 May)\nElectricity (7 May)"
    }
}
