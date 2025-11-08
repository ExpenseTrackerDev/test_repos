package com.example.expensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Transaction(
    val date: String,
    val type: String,
    val amount: Double,
    val category: String,
    val description: String
)

class TransactionAdapter(private var transactionList: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTransTitle: TextView = view.findViewById(R.id.tvTransTitle)
        val tvTransDate: TextView = view.findViewById(R.id.tvTransDate)
        val tvTransAmount: TextView = view.findViewById(R.id.tvTransAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactionList[position]
        // Display category as title
        holder.tvTransTitle.text = "${transaction.type} - ${transaction.category}"
        holder.tvTransDate.text = transaction.date
        holder.tvTransAmount.text = "$transaction.amount"
    }

    override fun getItemCount(): Int = transactionList.size

    fun updateData(newList: List<Transaction>) {
        transactionList = newList
        notifyDataSetChanged()
    }
}
