package com.example.pocketwise

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {
    private lateinit var currentPocketMoney: TextView
    private lateinit var oweAmount: TextView
    private lateinit var expenseBar: ProgressBar
    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart
    private lateinit var toolbar: Toolbar
    private lateinit var navbar: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private var topCategories = mutableListOf<String>()
    private var topCategoryTotals = mutableListOf<Float>()

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
        setContentView(R.layout.activity_home)
        enableEdgeToEdge()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        navbar = findViewById(R.id.nav_view)
        drawerLayout = findViewById(R.id.myDrawerLayout)
        actionBarDrawerToggle=ActionBarDrawerToggle(this,drawerLayout,R.string.nav_open,R.string.nav_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        currentPocketMoney = findViewById(R.id.currentPocketMoney)
        oweAmount = findViewById(R.id.oweAmount)
        expenseBar = findViewById(R.id.pocketMoneyProgress)
        lineChart = findViewById(R.id.lineChart)
        pieChart = findViewById(R.id.pieChart)

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
                    logout()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        calculateExpense()
        fetchExpensesForLat7Days()
        fetchExpensesByCategories()
    }

    private fun calculateExpense() {
        if(checkSession()) {
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if (userId != null) {
                val db = Firebase.firestore
                val studentRef = db.collection("students").document(userId)

                db.collection("balance")
                    .whereEqualTo("student_ref", studentRef)
                    .get()
                    .addOnSuccessListener() { document ->
                        if(document.isEmpty){
                            Toast.makeText(this, "No Record Found", Toast.LENGTH_SHORT).show()
                        } else {
                            for(d in document.documents) {
                                val pocketMoney = d.getLong("monthlyPocket")
                                val currentBalance = d.getLong("currentBalance")

                                if (pocketMoney != null && currentBalance != null) {
                                    val totalExpense = pocketMoney - currentBalance
                                    val expensePercentage = ((totalExpense.toDouble() / pocketMoney.toDouble()) * 100).toInt()

                                    expenseBar.progress = expensePercentage
                                } else {
                                    Toast.makeText(this, "Error calculating balance", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    .addOnFailureListener() {
                        Toast.makeText(this, "No Record Found", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Error in User Session", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchExpensesForLat7Days() {
        if (checkSession()) {
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if (userId != null) {
                val db = Firebase.firestore
                val studentRef = db.collection("students").document(userId)

                val calendar = Calendar.getInstance()
                val currentDate = calendar.time
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val sevenDaysAgo = calendar.time
                val sevenDaysAgoTimestamp = Timestamp(sevenDaysAgo)

                val last7Days = mutableListOf<Date>()
                val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
                val dates = mutableListOf<String>()

                calendar.time = currentDate
                for (i in 0..6) {
                    last7Days.add(calendar.time)
                    dates.add(dateFormat.format(calendar.time))
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                }
                last7Days.reverse()
                dates.reverse()

                db.collection("transactions")
                    .whereEqualTo("user_ref", studentRef)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty) {
                            Toast.makeText(this, "No Transactions Found", Toast.LENGTH_SHORT).show()
                        } else {
                            val entries = mutableListOf<Entry>()
                            val transactionMap = mutableMapOf<String, Float>()

                            for (document in documents) {
                                val transactionAmount = document.getDouble("amount")?.toFloat() ?: 0f
                                val transactionDate = document.getTimestamp("date")?.toDate()

                                if (transactionDate != null) {
                                    val formattedDate = dateFormat.format(transactionDate)
                                    transactionMap[formattedDate] = transactionMap.getOrDefault(formattedDate, 0f) + transactionAmount
                                }
                            }

                            for (i in last7Days.indices) {
                                val formattedDate = dates[i]
                                val amount = transactionMap[formattedDate] ?: 0f
                                entries.add(Entry(i.toFloat(), amount))
                            }

                            createLineChart(entries, dates)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Error", e.message.toString())
                        Toast.makeText(this, "Error fetching transactions: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

            } else {
                Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Error in User Session", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createLineChart(entries: List<Entry>, dates: List<String>) {
        val dataSet = LineDataSet(entries, "")
        dataSet.color = resources.getColor(R.color.primary)
        dataSet.valueTextColor = resources.getColor(R.color.secondary)
        dataSet.valueTextSize = 10f
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 5f
        dataSet.setCircleColor(resources.getColor(R.color.primary))
        dataSet.setDrawFilled(true)

        dataSet.fillDrawable = ContextCompat.getDrawable(this, R.drawable.chart_background)

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(dates)
        xAxis.granularity = 1f
        xAxis.textColor = resources.getColor(R.color.secondary)
        xAxis.textSize = 12f
        xAxis.setDrawGridLines(false)

        val leftAxis = lineChart.axisLeft
        leftAxis.textColor = resources.getColor(R.color.secondary)
        leftAxis.textSize = 12f
        leftAxis.setDrawGridLines(true)

        lineChart.axisRight.isEnabled = false

        lineChart.description.isEnabled = false
        lineChart.setDrawGridBackground(false)
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(false)
        lineChart.setPinchZoom(false)

        lineChart.animateX(1000)
        lineChart.invalidate()
    }

    private fun fetchExpensesByCategories() {
        if (checkSession()) {
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if (userId != null) {
                val db = Firebase.firestore
                val studentRef = db.collection("students").document(userId)

                db.collection("transactions")
                    .whereEqualTo("user_ref", studentRef)
                    .get()
                    .addOnSuccessListener() {documents ->
                        if (documents.isEmpty) {
                            Toast.makeText(this, "No Transactions Found", Toast.LENGTH_SHORT).show()
                        } else {
                            val categoryTotals = mutableMapOf<String, Float>()

                            for (document in documents) {
                                val category = document.getString("category") ?: "Uncategorized"
                                val amount = document.getDouble("amount")?.toFloat() ?: 0f

                                categoryTotals[category] = categoryTotals.getOrDefault(category, 0f) + amount
                            }

                            val sortedCategories = categoryTotals.entries.sortedByDescending { it.value }

                            val top5 = sortedCategories.take(5)

                            topCategories.clear()
                            topCategoryTotals.clear()

                            for (entry in top5) {
                                topCategories.add(entry.key)
                                topCategoryTotals.add(entry.value)
                            }

                            displayPieChart(topCategories, topCategoryTotals)
                        }
                    }
                    .addOnFailureListener() {e ->
                        Log.e("Error", e.message.toString())
                        Toast.makeText(this, "Error fetching transactions: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Error in User Session", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayPieChart(categories: List<String>, totals: List<Float>) {
        val pieEntries = ArrayList<PieEntry>()

        for (i in categories.indices) {
            pieEntries.add(PieEntry(totals[i], categories[i]))
        }

        val pieDataSet = PieDataSet(pieEntries, "")
        pieDataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        pieDataSet.valueTextColor = resources.getColor(R.color.white)

        val pieData = PieData(pieDataSet)
        pieData.setValueTextSize(12f)

        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 40f
        pieChart.setEntryLabelTextSize(12f)

        pieChart.invalidate()
    }

    private fun checkSession(): Boolean {
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
        return sharedPreference.getBoolean("loggedIn", false)
    }

    private fun logout() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Do you want to logout?")

        builder.setTitle("ALERT!")
        builder.setCancelable(false)

        builder.setPositiveButton("Yes") { _, _ ->
            val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        builder.setNegativeButton("No") {
                dialog, which -> dialog.cancel()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }
}