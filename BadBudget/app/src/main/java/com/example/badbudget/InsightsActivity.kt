package com.example.badbudget

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.badbudget.models.Budget
import com.example.badbudget.databinding.ActivityInsightsBinding
import com.example.badbudget.models.Expense
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class InsightsActivity : AppCompatActivity() {
    private lateinit var ui: ActivityInsightsBinding
    private lateinit var backButton: ImageView
    private val uid get() = UserSession.id(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityInsightsBinding.inflate(layoutInflater)
        setContentView(ui.root)

        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        ui.groupPeriod.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val p = when (checkedId) {
                ui.btnWeekly.id  -> Period.WEEK
                ui.btnYearly.id  -> Period.YEAR
                else             -> Period.MONTH
            }
            loadCharts(p)
        }
        ui.groupPeriod.check(ui.btnMonthly.id)
    }

    private fun loadCharts(period: Period) {
        val (start, end) = period.bounds()

        FirestoreService.getExpenses(uid) { allExpenses ->
            val filtered = allExpenses.filter { it.date in start..end }
            buildLineChart(filtered)

            // build chart
            FirestoreService.getBudgets(uid) { budgets ->
                buildGoalsChart(filtered, budgets)
            }
        }
    }

    private fun buildLineChart(expenses: List<Expense>) {
        val byDate = expenses
            .groupBy { it.date }
            .mapValues { it.value.sumOf { e -> e.amount } }
            .toSortedMap()

        val dates = byDate.keys.toList()
        val entries = dates.mapIndexed { i, d ->
            Entry(i.toFloat(), byDate[d]!!.toFloat())
        }

        val ds = LineDataSet(entries, "Spent").apply {
            setDrawCircles(false); lineWidth = 2f
        }

        ui.lineChart.apply {
            data = LineData(ds)
            axisRight.isEnabled = false
            xAxis.apply {
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(dates)
                setDrawLabels(true)
            }
            description = Description().apply { text = "" }
            invalidate()
        }
    }

    private fun buildGoalsChart(
        expenses: List<Expense>,
        budgets: List<Budget>
    ) {
        // combine budgets and expenses
        val spentMap = expenses.groupBy { it.category }
            .mapValues { it.value.sumOf { e -> e.amount } }

        // pick all categories that user has a budget for
        val cats = budgets.map { it.category }
        val minEntries   = mutableListOf<BarEntry>()
        val spentEntries = mutableListOf<BarEntry>()
        val maxEntries   = mutableListOf<BarEntry>()

        cats.forEachIndexed { i, cat ->
            val b = budgets.first { it.category == cat }
            val spent = spentMap[cat] ?: 0.0
            minEntries  .add(BarEntry(i.toFloat(), b.minAmount.toFloat()))
            spentEntries.add(BarEntry(i.toFloat(), spent.toFloat()))
            maxEntries  .add(BarEntry(i.toFloat(), b.maxAmount.toFloat()))
        }

        val dsMin   = BarDataSet(minEntries, "Min Goal")
        val dsSpent = BarDataSet(spentEntries, "Spent")
        val dsMax   = BarDataSet(maxEntries, "Max Goal")

        // chart colors
        dsMin.color   = getColor(R.color.chartGrey)
        dsSpent.color = getColor(R.color.chartPurple)
        dsMax.color   = getColor(R.color.teal_700)

        val data = BarData(dsMin, dsSpent, dsMax)
        val groupCount = cats.size
        val barSpace   = 0.05f
        val groupSpace = 0.3f
        val barWidth   = (1f - groupSpace) / 3f - barSpace

        data.barWidth = barWidth

        ui.goalsChart.apply {
            this.data = data
            xAxis.apply {
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(cats)
                setCenterAxisLabels(true)
                setDrawGridLines(false)
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            }
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled  = false
            description = Description().apply { text = "" }

            xAxis.axisMinimum = 0f
            xAxis.axisMaximum = 0f + groupCount * data.getGroupWidth(groupSpace, barSpace)
            groupBars(0f, groupSpace, barSpace)

            legend.isWordWrapEnabled = true
            invalidate()
        }
    }

    private enum class Period {
        WEEK, MONTH, YEAR;
        fun bounds(): Pair<String,String> {
            val end   = LocalDate.now()
            val start = when(this){
                WEEK  -> end.minusWeeks(1)
                MONTH -> end.minusMonths(1)
                YEAR  -> end.minusYears(1)
            }
            val fmt = DateTimeFormatter.ISO_DATE
            return fmt.format(start) to fmt.format(end)
        }
    }
}
