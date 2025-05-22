package com.example.badbudget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.badbudget.model.CategorySpend

class CategorySpendAdapter : ListAdapter<CategorySpend, CategorySpendAdapter.VH>(DIFF) {
    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CategorySpend>() {
            override fun areItemsTheSame(old: CategorySpend, new: CategorySpend) = old.category == new.category
            override fun areContentsTheSame(old: CategorySpend, new: CategorySpend) = old == new
        }
    }

    inner class VH(item: View) : RecyclerView.ViewHolder(item) {
        private val tvCat: TextView = item.findViewById(R.id.textViewSpendCategory)
        private val tvTot: TextView = item.findViewById(R.id.textViewSpendTotal)

        fun bind(item: CategorySpend) {
            tvCat.text = item.category
            tvTot.text = "R${item.total}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_category_spend, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}