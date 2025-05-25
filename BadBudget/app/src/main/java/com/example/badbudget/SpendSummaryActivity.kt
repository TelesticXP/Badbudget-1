package com.example.badbudget

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.badbudget.models.CategorySpend
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SpendSummaryActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var btnStart: MaterialButton
    private lateinit var btnEnd: MaterialButton
    private lateinit var tvRange: TextView
    private lateinit var adapter: CategorySpendAdapter

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var startDate = ""
    private var endDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spend_summary)

        // Back arrow
        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        btnStart = findViewById(R.id.btnStartDate)
        btnEnd   = findViewById(R.id.btnEndDate)
        tvRange  = findViewById(R.id.tvRange)

        // RecyclerView + adapter
        adapter = CategorySpendAdapter()
        findViewById<RecyclerView>(R.id.recyclerViewSpend).apply {
            layoutManager = LinearLayoutManager(this@SpendSummaryActivity)
            adapter = this@SpendSummaryActivity.adapter
        }

        // Date pickers
        val cal = Calendar.getInstance()
        val pickListener = { isStart: Boolean ->
            DatePickerDialog(
                this,
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
    }

    private fun updateUi() {
        tvRange.text = if (startDate.isNotEmpty() && endDate.isNotEmpty())
            "$startDate  →  $endDate"
        else
            "—"

        if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
            // fetch all expenses and then filter+aggregate locally
            FirestoreService.getExpenses(UserSession.id(this)) { all ->
                val filtered = all.filter { it.date in startDate..endDate }
                val catTotals = filtered
                    .groupBy { it.category }
                    .map { (cat, list) ->
                        CategorySpend(cat, list.sumOf { it.amount })
                    }

                if (catTotals.isEmpty()) {
                    Toast.makeText(this, "No expenses in that period", Toast.LENGTH_SHORT).show()
                }
                adapter.submitList(catTotals)
            }
        }
    }
}
