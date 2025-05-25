package com.example.badbudget

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.LocalDate

class ExpenseHistoryActivity : AppCompatActivity(),
    ExpenseHistoryAdapter.Callback {

    private lateinit var backButton: ImageView
    private lateinit var fabAddExpense: FloatingActionButton
    private val historyAdapter = ExpenseHistoryAdapter(this)
    private val uid get() = UserSession.id(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_history)

        // back button
        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // RecyclerView
        findViewById<RecyclerView>(R.id.recyclerViewHistory).apply {
            layoutManager = LinearLayoutManager(this@ExpenseHistoryActivity)
            adapter = historyAdapter
        }

        // FAB
        fabAddExpense = findViewById(R.id.fabAddExpense)
        fabAddExpense.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        // period picker
        val today = LocalDate.now()
        val initRange = PeriodPickerHelper.Range(
            start = today.minusDays(7).toString(),
            end = today.toString()
        )
        PeriodPickerHelper.hook(
            findViewById(R.id.periodPicker),
            this,
            initRange
        ) { range ->
            loadExpenses(range)
        }

        // load expenses
        loadExpenses(initRange)
    }

    private fun loadExpenses(r: PeriodPickerHelper.Range) {
        FirestoreService.getExpenses(uid) { all ->
            // filter expenses
            val filtered = all.filter { it.date in r.start..r.end }
            historyAdapter.submitList(filtered)
        }
    }

    override fun onOpenReceipt(path: String) {
        startActivity(
            Intent(this, ReceiptViewerActivity::class.java)
                .putExtra("path", path)
        )
    }
}
