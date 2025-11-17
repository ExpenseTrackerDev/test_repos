package com.example.expensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Income(
    var category: String,
    var amount: Double,
    var date: String,
    var description: String
)

class IncomeAdapter(
    private val incomeList: MutableList<Income>,
    private val onEdit: (Int) -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder>() {

    inner class IncomeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val btnEdit: Button = view.findViewById(R.id.btnEditIncome)
        val btnDelete: Button = view.findViewById(R.id.btnDeleteIncome)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.income_item, parent, false)
        return IncomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        val income = incomeList[position]
        holder.tvCategory.text = "Category: ${income.category}"
        holder.tvAmount.text = "Amount: ${income.amount}"
        holder.tvDate.text = "Date: ${income.date}"
        holder.tvDescription.text = "Description: ${income.description}"

        holder.btnEdit.setOnClickListener { onEdit(position) }
        holder.btnDelete.setOnClickListener { onDelete(position) }
    }
    fun updateList(newList: List<Income>) {
        incomeList.clear()
        incomeList.addAll(newList)
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int = incomeList.size
}
