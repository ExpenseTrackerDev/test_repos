package com.example.expensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

data class BudgetItem(
    val month: Int,
    val year: Int,
    val budgetId: String?,
    val amount: Int,
    val usedAmount: Int,
    val percentUsed: Int,
    val editable: Boolean,
    val overBudget: Int
)

class BudgetAdapter(
    private val budgetList: List<BudgetItem>,
    private val onEditClick: (BudgetItem) -> Unit
) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    inner class BudgetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMonth: TextView = view.findViewById(R.id.tvMonth)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBudget)
        val tvUsage: TextView = view.findViewById(R.id.tvUsage)
        val btnEdit: Button = view.findViewById(R.id.btnEditBudgetItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.budget_item, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val budget = budgetList[position]
        val monthName = java.text.SimpleDateFormat("MMMM", java.util.Locale.getDefault()).format(
            java.util.Calendar.getInstance().apply { set(java.util.Calendar.MONTH, budget.month - 1) }.time
        )

        holder.tvMonth.text = "$monthName ${budget.year}"
        holder.progressBar.progress = budget.percentUsed
        holder.tvUsage.text = "Used: ${budget.percentUsed}% of $${budget.amount}" +
                if (budget.overBudget > 0) " (Over by $${budget.overBudget})" else ""

        if (budget.editable) {
            holder.btnEdit.visibility = View.VISIBLE
            holder.btnEdit.setOnClickListener { onEditClick(budget) }
        } else {
            holder.btnEdit.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = budgetList.size
}
