package com.example.badbudget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ExpenseHistoryAdapter(private val cb: Callback)
    : ListAdapter<Expense, ExpenseHistoryAdapter.VH>(DIFF) {

    interface Callback { fun onOpenReceipt(path: String) }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Expense>() {
            override fun areItemsTheSame(o: Expense, n: Expense) = o.id == n.id
            override fun areContentsTheSame(o: Expense, n: Expense) = o == n
        }
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvCat: TextView = v.findViewById(R.id.tvCat)
        private val tvAmt: TextView = v.findViewById(R.id.tvAmt)
        private val tvDate: TextView = v.findViewById(R.id.tvDate)
        private val img: ImageView  = v.findViewById(R.id.imgThumb)

        fun bind(e: Expense) {
            tvCat.text  = e.category
            tvAmt.text  = "R${e.amount}"
            tvDate.text = e.date

            if (e.receiptPath != null) {
                img.visibility = View.VISIBLE
                Glide.with(img).load(java.io.File(e.receiptPath)).into(img)
                itemView.setOnClickListener { cb.onOpenReceipt(e.receiptPath) }
            } else {
                img.setImageDrawable(null)
                img.visibility = View.GONE
                itemView.setOnClickListener(null)
            }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int): VH {
        val item = LayoutInflater.from(p.context)
            .inflate(R.layout.item_expense_history, p, false)
        return VH(item)
    }

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))
}
