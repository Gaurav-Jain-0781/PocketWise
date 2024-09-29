package com.example.pocketwise

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.util.*

class ExpenseActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var toolbar_title: TextView
    private lateinit var navbar: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var categorySpinner: Spinner
    private lateinit var dateText: TextView
    private lateinit var amountInput: EditText
    private lateinit var notesInput: EditText

    private val categories = listOf(
        "Food & Drink", "Transport", "Rent", "Shopping", "Entertainment",
        "Bills", "Health", "Education", "Savings", "Others"
    )

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            return super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense)
        enableEdgeToEdge()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toolbar_title=findViewById(R.id.toolbar_title)
        toolbar_title.text="Expense"

        navbar = findViewById(R.id.nav_view)
        drawerLayout = findViewById(R.id.myDrawerLayout)
        actionBarDrawerToggle=ActionBarDrawerToggle(this,drawerLayout,R.string.nav_open,R.string.nav_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navbar.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home_nav_menu -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                }

                R.id.summary_nav_menu -> {
                    val intent = Intent(this, SummaryActivity::class.java)
                    startActivity(intent)
                }

                R.id.history_nav_menu -> {
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                }

                R.id.expense_nav_menu-> {
                    val intent =Intent(this,ExpenseActivity::class.java)
                    startActivity(intent)
                }

                R.id.profile_nav_menu -> {
                    Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                }

                R.id.logout_nav_menu -> {
                    Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        categorySpinner = findViewById(R.id.category_spinner)
        dateText = findViewById(R.id.expense_date)
        amountInput = findViewById(R.id.expense_amount)
        notesInput = findViewById(R.id.expense_note)

        setupCategoryDropdown()

        findViewById<ImageView>(R.id.change_date_arrow).setOnClickListener {
            showDatePickerDialog()
        }
    }
    private fun setupCategoryDropdown() {
        val adapter = ArrayAdapter(this, R.layout.expense_category_item, categories)
        adapter.setDropDownViewResource(R.layout.expense_category_dropdown)
        categorySpinner.adapter = adapter
        val defaultCategoryPosition = categories.indexOf("Food & Drink")
        categorySpinner.setSelection(defaultCategoryPosition)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            dateText.text = "$selectedDay/${selectedMonth + 1}/$selectedYear"
        }, year, month, day)

        datePickerDialog.show()
    }
}