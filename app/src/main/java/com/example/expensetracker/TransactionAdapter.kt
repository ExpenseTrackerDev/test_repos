package com.example.expensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.model.Transaction

class TransactionAdapter(private var transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    // Use a Set internally to avoid duplicates in report activity
    fun updateData(newData: List<Transaction>) {
        // Remove duplicates based on type + category + amount + date + description
        transactions = newData.distinctBy { listOf(it.type, it.category, it.amount, it.date, it.description) }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]

        holder.tvCategory.text = transaction.category ?: "—"
        holder.tvAmount.text = "Tk.${transaction.amount ?: 0.0}"

        val dateStr = transaction.date ?: ""
        holder.tvDate.text = if (dateStr.length >= 10) {
            dateStr.substring(0, 10)
        } else if (dateStr.isNotBlank()) {
            dateStr
        } else {
            "N/A"
        }

        holder.tvType.text = transaction.type ?: ""
    }

    override fun getItemCount(): Int = transactions.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvType: TextView = itemView.findViewById(R.id.tvType)
    }
}
