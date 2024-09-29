package com.example.pocketwise

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var topAnimation : Animation
    private lateinit var bottomAnimation : Animation
    private lateinit var logo : ImageView
    private lateinit var app : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.splash)

        topAnimation = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)

        logo = findViewById(R.id.logo_image)
        app = findViewById(R.id.app_name)

        logo.startAnimation(topAnimation)
        app.startAnimation(bottomAnimation)

        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginStatus()
        }, 2000)
    }

    private fun checkLoginStatus() {
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
        val loggedIn = sharedPreference.getBoolean("loggedIn", false)
        val loginTimestamp = sharedPreference.getLong("loginTimestamp", 0)
        val currentTime = System.currentTimeMillis()
        val sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000

        if (loggedIn && (currentTime - loginTimestamp) < sevenDaysInMillis) {
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            sharedPreference.edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}