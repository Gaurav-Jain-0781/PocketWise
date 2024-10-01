package com.example.pocketwise

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.app.Activity
import android.content.SharedPreferences
import android.content.Context
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage

class ProfileActivity :AppCompatActivity(){
    private lateinit var toolbar: Toolbar
    private lateinit var navbar: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var profileImageView:ImageView
    private lateinit var name: TextView
    private lateinit var regNo: TextView
    private lateinit var university: TextView
    private lateinit var email: TextView
    private lateinit var currentBalance: TextView
    private lateinit var monthlyPocket: TextView
    private lateinit var savings: TextView
    private lateinit var pocketButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private val studentArray : Array<String?> = arrayOf(null, null, null, null, null, null, null)
    private val PREF_NAME = "ProfilePrefs"
    private val PROFILE_IMAGE_KEY = "profile_image"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

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

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        profileImageView=findViewById(R.id.profileImageView)
        name = findViewById(R.id.name)
        regNo = findViewById(R.id.regNo)
        university = findViewById(R.id.university)
        email = findViewById(R.id.email)
        currentBalance = findViewById(R.id.currentBalance)
        monthlyPocket = findViewById(R.id.monthlyPocket)
        savings = findViewById(R.id.savings)
        pocketButton = findViewById(R.id.updatePocket)

        loadProfileImage()
        updateProfileData { student ->
            if(checkSession() && student != null){
                name.text = student[0]
                regNo.text = student[1]
                university.text = student[2]
                email.text = student[3]
            } else {
                Toast.makeText(this, "Failed to load Student data", Toast.LENGTH_SHORT).show()
            }
        }

        updateBalances { balances ->
            if(checkSession() && balances != null){
                currentBalance.text = currentBalance.text.toString() + balances[4]
                monthlyPocket.text = monthlyPocket.text.toString() + balances[5]
                savings.text = savings.text.toString() + balances[6]
            } else {
                Toast.makeText(this, "Failed to load Balances data", Toast.LENGTH_SHORT).show()
            }
        }

        profileImageView.setOnClickListener{
            val galleryIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(galleryIntent)
        }

        pocketButton.setOnClickListener {
            updatePocketMoney()
        }
    }

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

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = result.data?.data
            if (selectedImageUri != null) {
                profileImageView.setImageURI(selectedImageUri)
                saveProfileImage(selectedImageUri.toString())
                uploadImageToFirebase(selectedImageUri)
            }
        }
    }

    private fun saveProfileImage(imageUri: String) {
        with(sharedPreferences.edit()) {
            putString(PROFILE_IMAGE_KEY, imageUri)
            apply()
        }
    }

    private fun loadProfileImage() {
        if(checkSession()) {
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if (userId != null) {
                val storageReference = Firebase.storage.reference
                val imageReference = storageReference.child("images/${userId}.jpg")

                imageReference.downloadUrl
                    .addOnSuccessListener { uri ->
                        Glide.with(this)
                            .load(uri)
                            .into(profileImageView)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to load Image", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Error Loading Photo", Toast.LENGTH_SHORT).show()
            }
        } else {
            profileImageView.setImageResource(R.drawable.default_profile_image)
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri?) {
        if (imageUri != null) {
            val storageReference = FirebaseStorage.getInstance().reference

            if(checkSession()){
                val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
                val userID = sharedPreference.getString("userId", null)

                if(userID != null){
                    val imageReference = storageReference.child("images/${userID}.jpg")
                    val uploadTask = imageReference.putFile(imageUri)

                    uploadTask.addOnSuccessListener {
                        Toast.makeText(this, "Image Upload Successfully", Toast.LENGTH_SHORT).show()
                        loadProfileImage()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Image Upload Failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Error Loading User Id", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error in User Session", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkSession(): Boolean {
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
        return sharedPreference.getBoolean("loggedIn", false)
    }

    private fun updateProfileData(callback: (Array<String?>?) -> Unit) {
        if (checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if(userId != null){
                val db = Firebase.firestore

                db.collection("students")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()){
                            studentArray[0] = document.getString("name")
                            studentArray[1] = document.getString("regNo")
                            studentArray[2] = document.getString("email")
                            studentArray[3] =  document.getString("university")
                            callback(studentArray)
                        } else {
                            Toast.makeText(this, "No data found for user", Toast.LENGTH_SHORT).show()
                            callback(null)
                        }
                    }
                    .addOnFailureListener(){
                        Toast.makeText(this, "Error in Fetching Data", Toast.LENGTH_SHORT).show()
                        callback(null)
                    }
            } else {
                Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Error in User Session", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateBalances(callback: (Array<String?>?) -> Unit) {
        if (checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if(userId != null){
                val db = Firebase.firestore
                val studentRef = db.collection("students").document(userId)

                db.collection("balance")
                    .whereEqualTo("student_ref", studentRef)
                    .get()
                    .addOnSuccessListener(){document ->
                        if (document.isEmpty){
                            Toast.makeText(this, "Error in Fetching Data", Toast.LENGTH_SHORT).show()
                            callback(null)
                        } else {
                            for (d in document.documents){
                                studentArray[4] = d.getLong("currentBalance").toString()
                                studentArray[5] = d.getLong("monthlyPocket").toString()
                                studentArray[6] = d.getLong("savings").toString()
                            }
                            callback(studentArray)
                        }
                    }
                    .addOnFailureListener(){
                        Toast.makeText(this, "Error in Fetching Data", Toast.LENGTH_SHORT).show()
                        callback(null)
                    }
            } else {
                Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Error in User Session", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePocketMoney() {
        if (checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if(userId != null){

                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setTitle("Update Pocket Money")

                val input = EditText(this)
                input.inputType = InputType.TYPE_CLASS_NUMBER
                input.hint = "Enter new pocket money"
                dialogBuilder.setView(input)

                dialogBuilder.setPositiveButton("Yes") { dialog, _ ->
                    val pocketMoneyString = input.text.toString()

                    if (pocketMoneyString.isNotEmpty()) {
                        val pocketMoney = pocketMoneyString.toLong()

                        val db = Firebase.firestore
                        val studentRef = db.collection("students").document(userId)

                        db.collection("balance")
                            .whereEqualTo("student_ref", studentRef)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                if (!querySnapshot.isEmpty) {
                                    for (document in querySnapshot.documents) {
                                        document.reference.update("monthlyPocket", pocketMoney)
                                            .addOnSuccessListener {
                                                Toast.makeText(this, "Monthly pocket money updated successfully", Toast.LENGTH_SHORT).show()
                                                monthlyPocket.text = monthlyPocket.text.toString() + pocketMoney.toString()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(this, "Error updating monthly pocket money", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                } else {
                                    Toast.makeText(this, "No Doc Found", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "No Doc Found", Toast.LENGTH_SHORT).show()
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
}