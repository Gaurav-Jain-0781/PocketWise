package com.example.pocketwise

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class OwingsEntry(
    val category: String= "",
    val note: String= "",
    val amount: Long? = null,
    val date: Timestamp?= null,
    val friends: String= "",
)

class IOweUActivity : AppCompatActivity() {
    private lateinit var owingLayout: LinearLayout
    private lateinit var toolbar: Toolbar
    private lateinit var toolbar_title: TextView
    private lateinit var navbar: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var amount: EditText
    private lateinit var friend:EditText
    private lateinit var category: Spinner
    private lateinit var dateText: TextView
    private lateinit var datearrow: ImageView
    private lateinit var notes: EditText
    private lateinit var owingsButton: Button
    private var selectedCategory = ""

    private val categories = listOf(
        "Borrowed", "Lent"
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
        setContentView(R.layout.activity_ioweu_activity)
        enableEdgeToEdge()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toolbar_title=findViewById(R.id.toolbar_title)
        toolbar_title.text="IOweU"

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

        amount = findViewById(R.id.owe_amount)
        friend=findViewById(R.id.friend_name)
        category = findViewById(R.id.category_spinner)
        dateText = findViewById(R.id.owe_date)
        datearrow = findViewById(R.id.change_date_arrow)
        notes = findViewById(R.id.owe_note)
        owingsButton = findViewById(R.id.add_owings)

        setupCategoryDropdown()

        datearrow.setOnClickListener {
            showDatePickerDialog()
        }

        owingsButton.setOnClickListener(){
            addOwings()
        }

        owingLayout=findViewById(R.id.owingsParent)
        getOwings()
    }

    private fun getOwings(){
        if(checkSession()){
            val sharedPreferences=getSharedPreferences("user_session", MODE_PRIVATE)
            val userId=sharedPreferences.getString("userId",null)

            if(userId!==null){
                val db=Firebase.firestore
                val userRef=db.collection("students").document(userId)

                db.collection("owings")
                    .whereEqualTo("user_ref",userRef)
                    .get()
                    .addOnSuccessListener { documents->
                        val owingsByDate= mutableMapOf<String,MutableList<OwingsEntry>>()
                        val dateFormatter= SimpleDateFormat("dd MMMM yyyy",Locale.getDefault())

                        for(document in documents){
                            val ownings=document.toObject(OwingsEntry::class.java)
                            val owningsDateFormatted= ownings.date?.let { dateFormatter.format(it.toDate()) }?:"Unknown Date"

                            val formattedOwings = OwingsEntry(
                                category=ownings.category,
                                note=ownings.note,
                                amount=ownings.amount,
                                date=ownings.date,
                                friends=ownings.friends,
                            )

                            owingsByDate.getOrPut(owningsDateFormatted){ mutableListOf()}.add(formattedOwings)
                        }
                        displayOwingsByDate(owingsByDate)
                    }
                    .addOnFailureListener{exception->
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

    private fun displayOwingsByDate(owingsByDate: Map<String, List<OwingsEntry>>) {
        owingLayout.removeAllViews()

        val datePadding = resources.getDimensionPixelSize(R.dimen.date_padding)
        val owingsMargin = resources.getDimensionPixelSize(R.dimen.expense_margin)
        val owingsPadding = resources.getDimensionPixelSize(R.dimen.expense_padding)
        val imageSize = resources.getDimensionPixelSize(R.dimen.image_size)
        val imageMarginEnd = resources.getDimensionPixelSize(R.dimen.image_margin_end)

        for ((date, owings) in owingsByDate) {
            val dateTextView = TextView(this).apply {
                text = date
                setPadding(datePadding, datePadding, datePadding, datePadding)
                gravity = Gravity.START
                setTextColor(ContextCompat.getColor(context, R.color.background))
                typeface = ResourcesCompat.getFont(context, R.font.dosis_medium)
                setTypeface(typeface, Typeface.BOLD)
            }
            owingLayout.addView(dateTextView)

            for (owing in owings) {
                val horizontalLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
                    setPadding(owingsPadding, owingsPadding, owingsPadding, owingsPadding)
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
                    setImageResource(R.drawable.borrow_icon)
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
                    text = owing.friends
                    textSize = 22f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(context, R.color.black))
                    gravity = Gravity.START
                    typeface = ResourcesCompat.getFont(context, R.font.righteous)
                }

                val messageTextView = TextView(this).apply {
                    text = owing.note
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(context, R.color.notes))
                    gravity = Gravity.START
                    typeface = ResourcesCompat.getFont(context, R.font.dosis_semibold)
                }

                val verticalAmountLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }

                val categoryTextView = TextView(this).apply {
                    text = owing.category
                    textSize = 16f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(context, R.color.black))
                    gravity = Gravity.END
                    typeface = ResourcesCompat.getFont(context, R.font.lato_light_italic)
                }

                val amountTextView = TextView(this).apply {
                    if(owing.category=="Lent"){
                        text="₹${owing.amount}"
                        setTextColor(ContextCompat.getColor(context, R.color.income))
                    }else {
                        text = "₹${owing.amount}"
                        setTextColor(ContextCompat.getColor(context, R.color.expense))
                    }
                    textSize = 25f
                    gravity = Gravity.END
                    typeface = ResourcesCompat.getFont(context, R.font.righteous)
                }

                verticalLayout.addView(titleTextView)
                verticalLayout.addView(messageTextView)
                verticalAmountLayout.addView(categoryTextView)
                verticalAmountLayout.addView(amountTextView)

                horizontalLayout.addView(imageView)
                horizontalLayout.addView(verticalLayout)
                horizontalLayout.addView(verticalAmountLayout)

                owingLayout.addView(horizontalLayout)
            }
        }
    }

    private fun setupCategoryDropdown() {
        val adapter = ArrayAdapter(this, R.layout.expense_category_item, categories)
        adapter.setDropDownViewResource(R.layout.expense_category_dropdown)
        category.adapter = adapter
        val defaultCategoryPosition = categories.indexOf("Borrowed")
        category.setSelection(defaultCategoryPosition)

        category.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategory = parent.getItemAtPosition(position) as String
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

    private fun addOwings(){
        if(checkSession()){
            val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreferences.getString("userId",null)

            if(userId!=null){
                val db = Firebase.firestore
                val studentRef=db.collection("students").document(userId)
                val note=notes.text.toString()
                val amount=amount.text.toString()
                val currentDate=Timestamp.now()
                val friend=friend.text.toString()

                val oweHashMap= hashMapOf(
                    "category" to selectedCategory,
                    "note" to note,
                    "amount" to amount.toLong(),
                    "date" to currentDate,
                    "friends" to friend,
                    "user_ref" to studentRef
                )

                val owingsDocRef=db.collection("owings").document()

                owingsDocRef.set(oweHashMap)
                    .addOnSuccessListener {
                        Toast.makeText(this,"Owings added Successfully",Toast.LENGTH_LONG).show()
                        updateBalance(studentRef,amount.toLong(),selectedCategory)
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {e ->
                        Toast.makeText(this, "Error in recording Owings: ${e.message}", Toast.LENGTH_LONG).show()
                        return@addOnFailureListener
                    }
            } else {
                Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Error in User Session", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateBalance(studentRef:DocumentReference,amount:Long,selectedCategory:String){
        val db = Firebase.firestore

        db.collection("balance")
            .whereEqualTo("student_ref",studentRef)
            .get()
            .addOnSuccessListener { document ->
                if(!document.isEmpty){
                    val d=document.documents[0]
                    val balanceRef=d.reference
                    val lent=d.getLong("lent")
                    val borrowed=d.getLong("borrowed")
                    val currentBalance = d.getLong("currentBalance")

                    if (selectedCategory == "Lent") {
                        if (lent != null) {
                            balanceRef.update("lent", lent+amount)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Balance updated successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(this, "Failed to update balance: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }

                            if (currentBalance != null) {
                                balanceRef.update("currentBalance", currentBalance-amount)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Balance updated successfully", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Error updating balance", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    } else if (selectedCategory == "Borrowed") {
                        if (borrowed != null) {
                            balanceRef.update("borrowed", borrowed+amount)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Balance updated successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(this, "Failed to update balance: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }

                            if (currentBalance != null) {
                                balanceRef.update("currentBalance", currentBalance+amount)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Balance updated successfully", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Error updating balance", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "No balance document found for the student", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching balance", Toast.LENGTH_SHORT).show()
            }
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