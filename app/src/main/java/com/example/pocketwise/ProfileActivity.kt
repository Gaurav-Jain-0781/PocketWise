package com.example.pocketwise

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.net.Uri
import android.Manifest
import android.app.Activity
import android.content.SharedPreferences
import android.provider.ContactsContract
import android.content.Context
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class ProfileActivity :AppCompatActivity(){
    private lateinit var toolbar: Toolbar
    private lateinit var navbar: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var profileImageView:ImageView
    private lateinit var nameTextView: TextView
    private lateinit var universityTextView: TextView
    private lateinit var contactListView: ListView
    private lateinit var addContactButton: Button
    private lateinit var contactAdapter: ArrayAdapter<String>
    private val contactList= mutableListOf<String>()
    private lateinit var sharedPreferences: SharedPreferences

    private val CONTACT_PICK_REQUEST=1
    private val PREF_NAME = "ProfilePrefs"
    private val PROFILE_IMAGE_KEY = "profile_image"
    private val CONTACTS_KEY = "contacts"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

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

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        profileImageView=findViewById(R.id.profileImageView)
        nameTextView = findViewById(R.id.nameTextView)
        universityTextView = findViewById(R.id.universityTextView)
        contactListView = findViewById(R.id.contactListView)
        addContactButton = findViewById(R.id.addContactButton)

        nameTextView.text = "Yash Mehta"
        universityTextView.text = "Christ University,Banglore Central Campus"

        contactAdapter= ArrayAdapter(this,android.R.layout.simple_list_item_1,contactList)
        contactListView.adapter=contactAdapter

        loadProfileImage()
        loadContacts()

        profileImageView.setOnClickListener{
            val galleryIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(galleryIntent)
        }

        addContactButton.setOnClickListener{
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS)
                !=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS),CONTACT_PICK_REQUEST)
            }else{
                pickContact()
            }
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

    private fun pickContact(){
        val contactPickerIntent=Intent(Intent.ACTION_PICK,ContactsContract.Contacts.CONTENT_URI)
        contactLauncher.launch(contactPickerIntent)
    }

    private val contactLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contactData: Uri? = result.data?.data
            if (contactData != null) {
                val cursor = contentResolver.query(contactData, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val contactName = cursor.getString(nameIndex)
                    contactList.add(contactName)
                    contactAdapter.notifyDataSetChanged()
                    saveContacts()
                    cursor.close()
                }
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = result.data?.data
            if (selectedImageUri != null) {
                profileImageView.setImageURI(selectedImageUri)
                saveProfileImage(selectedImageUri.toString())
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
        val imageUriString = sharedPreferences.getString(PROFILE_IMAGE_KEY, null)
        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            profileImageView.setImageURI(imageUri)
        } else {
            profileImageView.setImageResource(R.drawable.default_profile_image)  // Default image
        }
    }
     private fun saveContacts() {
        with(sharedPreferences.edit()) {
            putStringSet(CONTACTS_KEY, contactList.toSet())
            apply()
        }
    }
    private fun loadContacts() {
        val savedContacts = sharedPreferences.getStringSet(CONTACTS_KEY, emptySet())
        if (savedContacts != null) {
            contactList.clear()
            contactList.addAll(savedContacts)
            contactAdapter.notifyDataSetChanged()
        }
    }
}