package com.example.pocketwise

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import java.util.*
import kotlin.collections.HashMap

class ExpenseActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var navbar: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var amount: EditText
    private lateinit var category: Spinner
    private lateinit var dateText: TextView
    private lateinit var datearrow: ImageView
    private lateinit var notes: EditText
    private lateinit var expenseButton: Button
    private var selectedCategory = ""

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
                R.id.ioweu_nav_menu->{
                    Toast.makeText(this, "IOweU", Toast.LENGTH_SHORT).show()
                    val intent=Intent(this,IOweUActivity::class.java)
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

        amount = findViewById(R.id.expense_amount)
        category = findViewById(R.id.category_spinner)
        dateText = findViewById(R.id.expense_date)
        datearrow = findViewById(R.id.change_date_arrow)
        notes = findViewById(R.id.expense_note)
        expenseButton = findViewById(R.id.add_expense)

        setupCategoryDropdown()

        datearrow.setOnClickListener {
            showDatePickerDialog()
        }

        expenseButton.setOnClickListener(){
            addExpense()
        }
    }
    private fun setupCategoryDropdown() {
        val adapter = ArrayAdapter(this, R.layout.expense_category_item, categories)
        adapter.setDropDownViewResource(R.layout.expense_category_dropdown)
        category.adapter = adapter
        val defaultCategoryPosition = categories.indexOf("Food & Drink")
        category.setSelection(defaultCategoryPosition)

        category.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategory = parent.getItemAtPosition(position) as String
                Toast.makeText(this@ExpenseActivity, "Selected: $selectedCategory", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
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

    private fun checkSession(): Boolean {
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
        return sharedPreference.getBoolean("loggedIn", false)
    }

    private fun addExpense() {
        if(checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if(userId != null) {
                val db = Firebase.firestore
                val studentRef = db.collection("students").document(userId)
                val note = notes.text.toString()
                val amount = amount.text.toString()
                val currentDate = Timestamp.now()

                val transactionHashMap = hashMapOf(
                    "category" to selectedCategory,
                    "note" to note,
                    "amount" to amount.toLong(),
                    "date" to currentDate,
                    "user_ref" to studentRef
                )

                val transactionDocRef = db.collection("transactions").document()

                transactionDocRef.set(transactionHashMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Expense Added Successful", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {e ->
                        Toast.makeText(this, "Registration Failed: ${e.message}", Toast.LENGTH_LONG).show()
                        return@addOnFailureListener
                    }

            } else {
                Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Error in User Session", Toast.LENGTH_SHORT).show()
        }
    }
}