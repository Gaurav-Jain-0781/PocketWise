package com.example.pocketwise

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

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
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}