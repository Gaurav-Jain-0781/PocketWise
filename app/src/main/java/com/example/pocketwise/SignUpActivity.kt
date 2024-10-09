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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.firestore

class SignUpActivity : AppCompatActivity() {
    private lateinit var loginLink : TextView
    private lateinit var signupButton : Button
    private lateinit var name : EditText
    private lateinit var reg_no : EditText
    private lateinit var university : EditText
    private lateinit var phoneNo : EditText
    private lateinit var password : EditText
    private lateinit var confirmPassword : EditText
    private lateinit var email : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.signup)

        loginLink = findViewById(R.id.loginLink)
        signupButton = findViewById(R.id.signup_button)
        name = findViewById(R.id.name)
        reg_no = findViewById(R.id.reg_no)
        university = findViewById(R.id.college)
        phoneNo = findViewById(R.id.phone_no)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        confirmPassword = findViewById(R.id.confirmPassword)

        loginLink.setOnClickListener(){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        signupButton.setOnClickListener(){
            val name = name.text.toString().trim()
            val regNo = reg_no.text.toString().trim()
            val university = university.text.toString().trim()
            val phoneStr = phoneNo.text.toString().trim()
            val email = email.text.toString().trim()
            val password = password.text.toString().trim()
            val confirmPassword = confirmPassword.text.toString().trim()

            if (name.isBlank() || regNo.isBlank() || university.isBlank() || phoneStr.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(checkPassword(password, confirmPassword)){
                insertData(name, regNo, university, phoneStr, email, password)
            } else {
                Toast.makeText(applicationContext, "Password Do not Match", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkPassword(p: String, cp: String) : Boolean{
        return p == cp
    }

    private fun insertData(name: String, regNo: String, university: String, phoneNo: String, email: String, password: String){
        val userMap = hashMapOf(
            "name" to name,
            "regNo" to regNo,
            "university" to university,
            "phoneNo" to phoneNo,
            "email" to email,
            "password" to password,
        )

        val db = Firebase.firestore
        val userDocRef = db.collection("students").document()

        userDocRef.set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Student Details Added Successful", Toast.LENGTH_LONG).show()
                insertBalance(userDocRef)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Registration Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun insertBalance(studentRef: DocumentReference){
        val balanceMap = hashMapOf(
            "currentBalance" to 0,
            "monthlyPocket" to 0,
            "savings" to 0,
            "lent" to 0,
            "borrowed" to 0,
            "student_ref" to studentRef,
        )

        val db = Firebase.firestore
        val userDocRef = db.collection("balance").document()

        userDocRef.set(balanceMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Registration Successful", Toast.LENGTH_LONG).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Registration Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}