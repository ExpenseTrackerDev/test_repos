package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class dashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        val menuIcon: ImageView = findViewById(R.id.menu_icon)
        val notificationIcon: ImageView = findViewById(R.id.notification_icon)
        val usernameText: TextView = findViewById(R.id.username_text)

        // Set username dynamically
        usernameText.text = "Hello, Sadia"

        // Setup Drawer Toggle
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Drawer Menu Icon Click
        menuIcon.setOnClickListener {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView)
            } else {
                drawerLayout.openDrawer(navigationView)
            }
        }

        // Notification Icon Click
//        notificationIcon.setOnClickListener {
//            startActivity(Intent(this, NotificationActivity::class.java))
//        }

        // Bottom Buttons
        findViewById<Button>(R.id.btnBottomIncome).setOnClickListener {
            startActivity(Intent(this, IncomeActivity::class.java))
        }
        findViewById<Button>(R.id.btnBottomExpense).setOnClickListener {
            startActivity(Intent(this, ExpenseActivity::class.java))
        }
        findViewById<Button>(R.id.btnBottomBudget).setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
        }

        // Handle Drawer Item Clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_income -> startActivity(Intent(this, IncomeActivity::class.java))
                R.id.nav_expense -> startActivity(Intent(this, ExpenseActivity::class.java))
                R.id.nav_budget -> startActivity(Intent(this, BudgetActivity::class.java))
              //  R.id.nav_report -> startActivity(Intent(this, ReportActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, settingsActivity::class.java))
                R.id.nav_logout -> finish()
            }
            drawerLayout.closeDrawers()
            true
        }
    }
}
