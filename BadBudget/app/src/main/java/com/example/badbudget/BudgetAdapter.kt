import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.badbudget.Budget
import com.example.badbudget.R

class BudgetAdapter(
    private var budgets: List<Budget>,
    private val onItemClick: (Budget) -> Unit
) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    inner class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewCategory: TextView = itemView.findViewById(R.id.textViewCategory)
        val textViewMin:      TextView = itemView.findViewById(R.id.textViewMin)
        val textViewMax:      TextView = itemView.findViewById(R.id.textViewMax)
        val textViewSpent:    TextView = itemView.findViewById(R.id.textViewSpent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_budget, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val budget = budgets[position]

        holder.textViewCategory.text = budget.category
        holder.textViewMin.text      = "Min: R${budget.minAmount}"
        holder.textViewMax.text      = "Max: R${budget.maxAmount}"
        holder.textViewSpent.text    = "Spent: R${budget.spentAmount}"

        holder.itemView.setOnClickListener {
            onItemClick(budget)
        }
    }

    override fun getItemCount() = budgets.size

    fun setBudgets(newBudgets: List<Budget>) {
        budgets = newBudgets
        notifyDataSetChanged()
    }
}