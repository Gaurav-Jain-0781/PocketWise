package com.example.pocketwise

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.core.view.setPadding
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class HistoryEntry(
    val category: String = "",
    val note: String = "",
    val amount: Long? = null,
    val date: Timestamp? = null
)

class HistoryActivity : AppCompatActivity() {
    private lateinit var historyLayout: LinearLayout
    private lateinit var toolbar: Toolbar
    private lateinit var navbar: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

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
        setContentView(R.layout.history)
        enableEdgeToEdge()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        navbar = findViewById(R.id.nav_view)
        drawerLayout = findViewById(R.id.myDrawerLayout)
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navbar.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home_nav_menu -> {
                    val intent = Intent(this, HomeActivity::class.java)
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

        historyLayout = findViewById(R.id.historyParent)

        getExpenses()
    }

    private fun getExpenses () {
        if(checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if(userId != null){
                val db = Firebase.firestore
                val userRef = db.collection("students").document(userId)

                db.collection("transactions")
                    .whereEqualTo("user_ref", userRef)
                    .get()
                    .addOnSuccessListener { documents ->
                        val expensesByDate = mutableMapOf<String, MutableList<HistoryEntry>>()
                        val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

                        for (document in documents) {
                            val expense = document.toObject(HistoryEntry::class.java)
                            val expenseDateFormatted = expense.date?.let { dateFormatter.format(it.toDate()) } ?: "Unknown Date"

                            val formattedExpense = HistoryEntry(
                                category = expense.category,
                                note = expense.note,
                                amount = expense.amount,
                                date = expense.date
                            )

                            expensesByDate.getOrPut(expenseDateFormatted) { mutableListOf() }.add(formattedExpense)
                        }

                        displayExpensesByDate(expensesByDate)
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Error in Logs", Toast.LENGTH_SHORT).show()
                        Log.w("Firestore", "Error getting documents: ", exception)
                    }
            } else {
                Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Error Loading Photo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayExpensesByDate(expensesByDate: Map<String, List<HistoryEntry>>) {
        historyLayout.removeAllViews()

        val datePadding = resources.getDimensionPixelSize(R.dimen.date_padding)
        val expenseMargin = resources.getDimensionPixelSize(R.dimen.expense_margin)
        val expensePadding = resources.getDimensionPixelSize(R.dimen.expense_padding)
        val imageSize = resources.getDimensionPixelSize(R.dimen.image_size)
        val imageMarginEnd = resources.getDimensionPixelSize(R.dimen.image_margin_end)

        for ((date, expenses) in expensesByDate) {
            val dateTextView = TextView(this).apply {
                text = date
                setPadding(datePadding, datePadding, datePadding, datePadding)
                gravity = Gravity.START
                setTextColor(ContextCompat.getColor(context, R.color.background))
                typeface = ResourcesCompat.getFont(context, R.font.dosis_medium)
                setTypeface(typeface, Typeface.BOLD)
            }
            historyLayout.addView(dateTextView)

            for (expense in expenses) {
                val horizontalLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
                    setPadding(expensePadding, expensePadding, expensePadding, expensePadding)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                val imageView = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        imageSize,
                        imageSize
                    ).apply {
                        gravity = Gravity.CENTER_VERTICAL
                        marginEnd = imageMarginEnd
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageResource(R.drawable.account)
                    background = ContextCompat.getDrawable(context, R.drawable.circular_background)
                }

                val verticalLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }

                val titleTextView = TextView(this).apply {
                    text = expense.category
                    textSize = 22f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(context, R.color.black))
                    gravity = Gravity.START
                    typeface = ResourcesCompat.getFont(context, R.font.righteous)
                }

                val messageTextView = TextView(this).apply {
                    text = expense.note
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(context, R.color.notes))
                    gravity = Gravity.START
                    typeface = ResourcesCompat.getFont(context, R.font.dosis_semibold)
                }

                val amountTextView = TextView(this).apply {
                    text = "- ₹${expense.amount}"
                    textSize = 30f
                    typeface = ResourcesCompat.getFont(context, R.font.righteous)
                    setTextColor(ContextCompat.getColor(context, R.color.expense))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.CENTER_VERTICAL
                    }
                }

                verticalLayout.addView(titleTextView)
                verticalLayout.addView(messageTextView)

                horizontalLayout.addView(imageView)
                horizontalLayout.addView(verticalLayout)
                horizontalLayout.addView(amountTextView)

                historyLayout.addView(horizontalLayout)
            }
        }
    }

    private fun checkSession(): Boolean {
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
        return sharedPreference.getBoolean("loggedIn", false)
    }
}