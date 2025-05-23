package com.example.badbudget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.badbudget.models.Expense
import android.net.Uri

class ExpenseAdapter : ListAdapter<Expense, ExpenseAdapter.VH>(DIFF) {
    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Expense>() {
            override fun areItemsTheSame(old: Expense, new: Expense) = old.id == new.id
            override fun areContentsTheSame(old: Expense, new: Expense) = old == new
        }
    }

    inner class VH(item: View) : RecyclerView.ViewHolder(item) {
        private val tvDate: TextView = item.findViewById(R.id.textViewExpenseDate)
        private val tvTime: TextView = item.findViewById(R.id.textViewExpenseTime)
        private val tvAmt: TextView = item.findViewById(R.id.textViewExpenseAmount)
        private val tvDesc: TextView = item.findViewById(R.id.textViewExpenseDescription)
        private val ivRec: ImageView = item.findViewById(R.id.imageViewExpenseReceipt)

        fun bind(e: Expense) {
            tvDate.text = e.date
            tvTime.text = "${e.startTime} - ${e.endTime}"
            tvAmt.text = "R${e.amount}"
            tvDesc.text = e.description ?: ""
            if (!e.receiptPath.isNullOrEmpty()) {
                ivRec.setImageURI(Uri.parse(e.receiptPath))
                ivRec.visibility = View.VISIBLE
            } else ivRec.visibility = View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}
