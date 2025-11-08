package com.example.expensetracker

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class NotificationActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        recyclerView = findViewById(R.id.recyclerViewNotifications)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val backBtn: ImageView = findViewById(R.id.back_btn)
        backBtn.setOnClickListener { finish() }

        // Sample notifications
        val allNotifications = generateSampleNotifications()

        // Filter only last 30 days
        val last30Days = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }.time
        val recentNotifications = allNotifications.filter { it.date.after(last30Days) }

        adapter = NotificationAdapter(recentNotifications)
        recyclerView.adapter = adapter
    }

    // Sample data generator
    private fun generateSampleNotifications(): List<Notification> {
        val list = mutableListOf<Notification>()
        val calendar = Calendar.getInstance()

        for (i in 1..50) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i) // Notifications from past 50 days
            list.add(Notification("Notification #$i", calendar.time))
        }
        return list
    }
}
