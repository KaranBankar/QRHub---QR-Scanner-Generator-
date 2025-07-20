package com.example.qrhub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.zxing.integration.android.IntentIntegrator
import com.example.qrhub.databinding.ActivityMainBinding
import com.example.qrhub.fragments.GenerateFragment
import com.example.qrhub.fragments.HistoryFragment
import com.example.qrhub.fragments.ScanFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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