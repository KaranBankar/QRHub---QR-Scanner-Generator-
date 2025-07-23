package com.example.qrhub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.zxing.integration.android.IntentIntegrator
import com.example.qrhub.databinding.ActivityMainBinding
import com.example.qrhub.fragments.GenerateFragment
import com.example.qrhub.fragments.HistoryFragment
import com.example.qrhub.fragments.ScanFragment
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.teal_700)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        // Set default fragment
        loadFragment(ScanFragment())

        // Bottom navigation setup
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_scan -> loadFragment(ScanFragment())
                R.id.nav_generate -> loadFragment(GenerateFragment())
                R.id.nav_history -> loadFragment(HistoryFragment())
            }
            true
        }

        val navView = findViewById<NavigationView>(R.id.nav_view)
        val menuView = navView.inflateHeaderView(R.layout.custom_drawer)

        menuView.findViewById<LinearLayout>(R.id.nav_home).setOnClickListener {
            Toast.makeText(this,"Home Clicked", Toast.LENGTH_SHORT).show()
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        menuView.findViewById<LinearLayout>(R.id.nav_policies).setOnClickListener {

        }


    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            handleScanResult(result.contents)
        }
    }

    fun handleScanResult(result: String) {
        // Check if result is a URL
        if (result.startsWith("http://") || result.startsWith("https://")) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result))
            startActivity(intent)
        } else {
            // Show textual data in AlertDialog
            AlertDialog.Builder(this)
                .setTitle("Scan Result")
                .setMessage(result)
                .setPositiveButton("OK") { _, _ -> }
                .show()

            // Save to history
            (supportFragmentManager.findFragmentById(R.id.fragment_container) as? ScanFragment)
                ?.saveToHistory(result, "Scanned")
        }
    }
}

