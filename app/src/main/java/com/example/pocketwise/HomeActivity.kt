package com.example.pocketwise

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class HomeActivity : AppCompatActivity() {
    lateinit var drawerLayout: DrawerLayout
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        enableEdgeToEdge()

        drawerLayout = findViewById(R.id.myDrawerLayout)
        actionBarDrawerToggle=ActionBarDrawerToggle(this,drawerLayout,R.string.nav_open,R.string.nav_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            handleNavigation(menuItem)
            true
        }
    }

    private fun handleNavigation(menuItem: MenuItem) {
        Log.d("HomeActivity", "Navigation item selected: ${menuItem.itemId}")
        when (menuItem.itemId) {
            R.id.home_nav_menu -> {
                Log.d("HomeActivity", "Home menu selected")
                startActivity(Intent(this, HomeActivity::class.java))
            }
            R.id.summary_nav_menu -> {
                Log.d("HomeActivity", "Summary menu selected")
                startActivity(Intent(this, SummaryActivity::class.java))
            }
            R.id.history_nav_menu -> {
                Log.d("HomeActivity", "History menu selected")
                startActivity(Intent(this, HistoryActivity::class.java))
            }
            R.id.profile_nav_menu -> {
                Log.d("HomeActivity", "Profile menu selected")
                startActivity(Intent(this, ProfileActivity::class.java))
            }
            R.id.logout_nav_menu -> {
                Log.d("HomeActivity", "Logout menu selected")
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
        drawerLayout.closeDrawers()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            true
        }else {
            super.onOptionsItemSelected(item)
        }
    }
}