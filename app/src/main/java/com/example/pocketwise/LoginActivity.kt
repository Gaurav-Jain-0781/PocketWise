package com.example.pocketwise

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class LoginActivity : AppCompatActivity() {
    private lateinit var username : EditText
    private lateinit var password : EditText
    private lateinit var loginButton : Button
    private lateinit var signUpTextView : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login)

        loginButton = findViewById(R.id.login_button)
        signUpTextView = findViewById(R.id.signupLink)
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)

        signUpTextView.setOnClickListener(){
            startActivity(Intent(this, SignUpActivity:: class.java))
        }

        loginButton.setOnClickListener(){
            login()
        }
    }

    private fun login(){
        val name = username.text.toString()
        val pass = password.text.toString()

        if(name.isEmpty() || pass.isEmpty()){
            Toast.makeText(this,"Fill all Fields!", Toast.LENGTH_LONG).show()
            return
        } else {
            fetchData(name, pass)
        }
    }

    private fun fetchData(name: String, password: String) {
        val db = Firebase.firestore

        db.collection("students")
            .whereEqualTo("name", name)
            .whereEqualTo("password", password)
            .get()
            .addOnSuccessListener {  document ->
                if(document.isEmpty){
                    Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                }
                else {
                    for (d in document.documents) {
                        val userId = d.id
                        val userName = d.getString("name")

                        saveSession(userName, userId)
                        Toast.makeText(this, "Welcome, $userName", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, ProfileActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Login Failed! Try Again..", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveSession(userName : String?, userId : String){
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("userId", userId)
        editor.putString("userName", userName)
        editor.putBoolean("loggedIn", true)
        editor.putLong("loginTimestamp", System.currentTimeMillis())
        editor.apply()
    }
}