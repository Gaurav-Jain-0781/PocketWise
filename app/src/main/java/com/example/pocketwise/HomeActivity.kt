package com.example.pocketwise

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
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
    private lateinit var lendAmount: TextView
    private lateinit var lastSavings: TextView
    private lateinit var totalSavings: TextView
    private lateinit var avgExpenses: TextView
    private lateinit var totalExpenses: TextView
    private lateinit var addIncomeButton: Button

    private lateinit var expenseBar: ProgressBar
    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart
    private lateinit var toolbar: Toolbar
    private lateinit var navbar: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private val balanceArray : Array<String?> = arrayOf(null, null, null, null, null, null, null)
    private var topCategories = mutableListOf<String>()
    private var topCategoryTotals = mutableListOf<Float>()

    private val categorySuggestions = mapOf(
        "Food & Drink" to listOf(
            "Consider preparing meals at home to save $50 next month.",
            "Limit dining out to twice a week to save an extra $30.",
            "Try meal prepping to reduce impulsive food purchases."
        ),
        "Transport" to listOf(
            "Use public transportation to save on fuel costs.",
            "Consider carpooling to reduce travel expenses.",
            "Walk or bike for short trips to save fuel and improve health."
        ),
        "Rent" to listOf(
            "Negotiate with your landlord for a potential discount on rent.",
            "Consider sharing a flat to reduce monthly rent costs.",
            "Look for cheaper rent alternatives in different areas."
        ),
        "Shopping" to listOf(
            "Set a monthly shopping budget to avoid overspending.",
            "Wait for seasonal sales to buy expensive items.",
            "Use discount coupons and cashback offers for shopping."
        ),
        "Entertainment" to listOf(
            "Reduce entertainment subscriptions to save $50 per month.",
            "Limit movie outings to once a month to save on tickets.",
            "Use free or low-cost activities for entertainment."
        ),
        "Bills" to listOf(
            "Review and renegotiate utility plans for better rates.",
            "Switch to energy-saving appliances to lower your bills.",
            "Automate bill payments to avoid late fees."
        ),
        "Health" to listOf(
            "Exercise at home instead of paying for a gym membership.",
            "Switch to generic medicines to reduce healthcare expenses.",
            "Use free health clinics for basic health services."
        ),
        "Education" to listOf(
            "Take free or discounted online courses instead of paid ones.",
            "Buy used textbooks or digital copies to save money.",
            "Apply for scholarships and financial aid to cover tuition."
        ),
        "Savings" to listOf(
            "Increase your emergency fund by setting aside an extra $50.",
            "Set up automatic transfers to your savings account monthly.",
            "Reduce unnecessary expenses to increase savings by $100."
        ),
        "Others" to listOf(
            "Track miscellaneous expenses to identify areas for saving.",
            "Cut down on impulse purchases by sticking to a budget.",
            "Set a limit on unplanned expenses to save more."
        )
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
        lendAmount = findViewById(R.id.lendAmount)
        addIncomeButton = findViewById(R.id.addPocketMoneyBtn)
        lastSavings = findViewById(R.id.lastSavings)
        totalSavings = findViewById(R.id.totalSavings)
        avgExpenses=findViewById(R.id.avgExpenses)
        totalExpenses = findViewById(R.id.totalExpenses)

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

        updateAccountPocketDetails { balances ->
            if (checkSession() && balances != null) {
                currentPocketMoney.text =  currentPocketMoney.text.toString() + balances[0]
                lendAmount.text =  lendAmount.text.toString() + "+ " + balances[3]
                oweAmount.text =  oweAmount.text.toString() + "- " + balances[4]
                totalSavings.text = totalSavings.text.toString()+ balances[2]
                lastSavings.text =  lastSavings.text.toString()+ balances[5]

                val monthlyPocket = balances[1]?.toFloatOrNull() ?: 0f
                val currentBalance = balances[0]?.toFloatOrNull() ?: 0f
                val calculatedTotalExpenses = (monthlyPocket - currentBalance).toInt()
                totalExpenses.text = totalExpenses.text.toString() + calculatedTotalExpenses.toString()

                val daysInMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                val avgExpense = if (daysInMonth > 0) (calculatedTotalExpenses / daysInMonth).toInt() else 0
                avgExpenses.text = avgExpenses.text.toString() + avgExpense.toString()
            } else {
                Toast.makeText(this, "Failed to load Balances data", Toast.LENGTH_SHORT).show()
            }
        }
        calculateExpense()
        fetchExpensesForLat7Days()
        fetchExpensesByCategories()

        addIncomeButton.setOnClickListener {
            updatePocketMoney()
        }

        val userTopCategory = "Food & Drink"
        showCategorySuggestions(userTopCategory)
    }

    private fun showCategorySuggestions(topCategory: String) {
        val suggestions = categorySuggestions[topCategory]
        if (suggestions != null) {
            findViewById<TextView>(R.id.suggestion1).text = suggestions[0]
            findViewById<TextView>(R.id.suggestion2).text = suggestions[1]
            findViewById<TextView>(R.id.suggestion3).text = suggestions[2]
        } else {
            findViewById<TextView>(R.id.suggestion1).text = "No suggestions available"
        }
    }
    private fun updatePocketMoney() {
        if (checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if(userId != null){

                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setTitle("Got extra money this month? Add it to your current pocket")

                val input = EditText(this)
                input.inputType = InputType.TYPE_CLASS_NUMBER
                input.hint = "Enter new extra income amount"
                dialogBuilder.setView(input)

                dialogBuilder.setPositiveButton("Yes") { dialog, _ ->
                    val pocketMoneyString = input.text.toString()

                    if (pocketMoneyString.isNotEmpty()) {
                        val extraPocketMoney = pocketMoneyString.toLong()

                        val db = Firebase.firestore
                        val studentRef = db.collection("students").document(userId)

                        db.collection("balance")
                            .whereEqualTo("student_ref", studentRef)
                            .get()
                            .addOnSuccessListener { document ->
                                if (!document.isEmpty) {
                                    val d = document.documents[0]
                                    val balanceRef = d.reference
                                    val currentBalance = d.getLong("currentBalance")
                                    val newBalance = currentBalance?.plus(extraPocketMoney)

                                    if (currentBalance != null) {
                                        balanceRef.update("currentBalance", newBalance)
                                            .addOnSuccessListener {
                                                Toast.makeText(this, "Balance updated successfully", Toast.LENGTH_SHORT).show()
                                                currentPocketMoney.text="₹$newBalance"
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(this, "Error updating balance", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                } else {
                                    Toast.makeText(this, "No balance document found for the student", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Error fetching balance", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }

                dialogBuilder.create().show()
            } else {
                Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Error in User Session", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAccountPocketDetails(callback: (Array<String?>?) -> Unit){
        if(checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)
            if (userId != null){
                val db = Firebase.firestore
                val studentRef = db.collection("students").document(userId)

                db.collection("balance")
                    .whereEqualTo("student_ref",studentRef)
                    .get()
                    .addOnSuccessListener(){document ->
                        if (document.isEmpty){
                            Toast.makeText(this, "Error in Fetching Data", Toast.LENGTH_SHORT).show()
                            callback(null)
                        } else {
                            for (d in document.documents){
                                balanceArray[0] = d.getLong("currentBalance").toString()
                                balanceArray[1] = d.getLong("monthlyPocket").toString()
                                balanceArray[2] = d.getLong("savings").toString()
                                balanceArray[3] = d.getLong("lent").toString()
                                balanceArray[4] = d.getLong("borrowed").toString()
                                balanceArray[5] = d.getLong("last_savings").toString()
                            }
                            callback(balanceArray)
                        }
                    }
                    .addOnFailureListener(){
                        Toast.makeText(this, "Error in Fetching Data", Toast.LENGTH_SHORT).show()
                        callback(null)
                    }
            }else{
                Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this, "Error in User Session", Toast.LENGTH_SHORT).show()
        }
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