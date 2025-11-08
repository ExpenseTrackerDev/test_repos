package com.example.expensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

data class Notification(
    val description: String,
    val date: Date // Store as Date for easy filtering
)

class NotificationAdapter(private var notificationList: List<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDescription: TextView = view.findViewById(R.id.tvNotificationDescription)
        val tvDate: TextView = view.findViewById(R.id.tvNotificationDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notificationList[position]
        holder.tvDescription.text = notification.description

        // Format date to readable string
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        holder.tvDate.text = sdf.format(notification.date)
    }

    override fun getItemCount(): Int = notificationList.size

    fun updateData(newList: List<Notification>) {
        notificationList = newList
        notifyDataSetChanged()
    }
}
