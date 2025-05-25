package com.example.badbudget

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nav = findViewById<BottomNavigationView>(R.id.bottomNav)
        if (savedInstanceState == null) {
            openFragment(DashboardFragment())
            nav.selectedItemId = R.id.nav_dashboard
        }

        nav.setOnItemSelectedListener { item ->
            val frag: Fragment = when (item.itemId) {
                R.id.nav_dashboard -> DashboardFragment()
                R.id.nav_budgets -> BudgetsFragment()
                R.id.nav_awards -> AwardsFragment()
                R.id.nav_summary -> SpendSummaryFragment()
                R.id.nav_history -> ExpenseHistoryFragment()
                else -> return@setOnItemSelectedListener false
            }
            openFragment(frag)
            true
        }
    }

    private fun openFragment(f: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, f)
            .commit()
    }
}
