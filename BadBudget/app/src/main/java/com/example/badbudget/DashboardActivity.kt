package com.example.badbudget

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.badbudget.databinding.ActivityDashboardBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navbar
        binding.navBudgets.setOnClickListener {
            startActivity(Intent(this, BudgetsActivity::class.java))
        }
        binding.navAwards.setOnClickListener {
            startActivity(Intent(this, AwardsActivity::class.java))
        }
        binding.navSummary.setOnClickListener {
            startActivity(Intent(this, SpendSummaryActivity::class.java))
        }
        binding.navHistory.setOnClickListener {
            startActivity(Intent(this, ExpenseHistoryActivity::class.java))
        }
        binding.backButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.buttonInsights.setOnClickListener {
            startActivity(Intent(this, InsightsActivity::class.java))
        }

        setSupportActionBar(binding.topAppBar)
        supportActionBar?.hide()
        binding.topAppBar.inflateMenu(R.menu.menu_dashboard)

        // Fetches budgets and expenses
        FirestoreService.getExpenses(UserSession.id(this)) { allExpenses ->
            FirestoreService.getBudgets(UserSession.id(this)) { budgets ->
                // Spending distribution
                val catMap = allExpenses
                    .groupBy { it.category }
                    .mapValues { it.value.sumOf { it.amount } }
                updateSpendingPie(catMap)

                // Compliance over last 30 days
                updateCompliancePie(allExpenses, budgets)

                // Summary cards
                val totalSpent = catMap.values.sum()
                val budgetMax  = budgets.sumOf { it.maxAmount }
                updateSummaryCards(totalSpent, budgetMax)
            }
        }
    }

    // Spending per category pie chart
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
            legend.isEnabled = false
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
            .mapValues { it.value.sumOf { it.amount } }

        var under = 0
        var onTrack = 0
        var over = 0
        budgets.forEach { b ->
            val spent = spentMap[b.category] ?: 0.0
            when {
                spent < b.minAmount -> under++
                spent > b.maxAmount -> over++
                else                 -> onTrack++
            }
        }

        val entries = listOf(
            PieEntry(under.toFloat(), "Under Min"),
            PieEntry(onTrack.toFloat(), "On Track"),
            PieEntry(over.toFloat(), "Over")
        )
        val colorsArr = intArrayOf(
            ContextCompat.getColor(this, R.color.chartGrey),
            ContextCompat.getColor(this, android.R.color.holo_green_light),
            ContextCompat.getColor(this, android.R.color.holo_red_light)
        )
        val ds = PieDataSet(entries, "").apply {
            setDrawValues(false)
            sliceSpace = 2f
            setColors(colorsArr, this@DashboardActivity)
        }
        binding.pieCompliance.apply {
            data = PieData(ds)
            description.isEnabled = false
            legend.isEnabled = true
            setDrawEntryLabels(false)
            invalidate()
        }
    }

    // Update text cards
    private fun updateSummaryCards(spent: Double, max: Double) {
        binding.textTotalExpenses.text = "R ${spent.toInt()}"
        binding.textLastMonth.text =
            "Upcoming bills: Rent (5 May)\nElectricity (7 May)"
    }
}
