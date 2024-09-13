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
import android.util.Log
import android.content.Context

class ProfileActivity :AppCompatActivity(){
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
        Log.d("ProfileActivity", "ProfileActivity launched")

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