package com.devkaran.qrhub

import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
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
import com.devkaran.qrhub.databinding.ActivityMainBinding
import com.devkaran.qrhub.fragments.GenerateFragment
import com.devkaran.qrhub.fragments.HistoryFragment
import com.devkaran.qrhub.fragments.ScanFragment
import com.google.android.material.navigation.NavigationView

import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.install.model.InstallStatus

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    private val UPDATE_REQUEST_CODE = 123
    private lateinit var appUpdateManager: com.google.android.play.core.appupdate.AppUpdateManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.teal_700)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        appUpdateManager = AppUpdateManagerFactory.create(this)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

// Find your custom menu icon (inside the LinearLayout toolbar)
        val menuIcon: ImageView = findViewById(R.id.iv_menu)

// Open drawer when menu icon is clicked
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

// Set default fragment
        loadFragment(ScanFragment())


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
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        menuView.findViewById<LinearLayout>(R.id.nav_testers).setOnClickListener {
            val i=Intent(this, TestersActivity::class.java)
            startActivity(i)
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        menuView.findViewById<LinearLayout>(R.id.nav_policies).setOnClickListener {
            showPoliciesDialog()
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        menuView.findViewById<LinearLayout>(R.id.nav_about_us).setOnClickListener {
            showAboutDevelopersDialog()
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        menuView.findViewById<LinearLayout>(R.id.nav_share).setOnClickListener {
            shareApp()
        }
        menuView.findViewById<LinearLayout>(R.id.nav_update).setOnClickListener {
            checkForAppUpdate()
        }


    }

    private fun showPoliciesDialog() {
        // Inflate the custom dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_policies, null)

        // Create the AlertDialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Set up the dismiss button
        dialogView.findViewById<View>(R.id.dialog_dismiss_button).setOnClickListener {
            dialog.dismiss()
        }

        // Show the dialog
        dialog.show()
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

        if (requestCode == UPDATE_REQUEST_CODE && resultCode != RESULT_OK) {
            Toast.makeText(this, "Update failed!", Toast.LENGTH_SHORT).show()
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

    private fun showAboutDevelopersDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_about_developers, null)

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true) // dialog dismiss when touching outside
            .create()

        val k_link = dialogView.findViewById<ImageView>(R.id.karan_linkdin)
        val k_git = dialogView.findViewById<ImageView>(R.id.karan_github)

        val c_link = dialogView.findViewById<ImageView>(R.id.ch_linkdin)
        val c_git = dialogView.findViewById<ImageView>(R.id.ch_github)

        k_link.setOnClickListener {
            openLink("https://www.linkedin.com/in/karan-bankar-453b57252/")
        }

        c_link.setOnClickListener {
            openLink("https://www.linkedin.com/in/chaitany-kakde-2a3ba62a8/")
        }

        k_git.setOnClickListener {
            openLink("https://github.com/KaranBankar")
        }

        c_git.setOnClickListener {
            openLink("https://github.com/chaitanykakde")
        }

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()
    }

    private fun shareApp() {
        val appPackageName = packageName // gets your app's package name
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "Download QR Hub App Free QR Scanner and Generator : https://play.google.com/store/apps/details?id=$appPackageName"
            )
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun checkForAppUpdate() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) // or FLEXIBLE
            ) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE, // IMMEDIATE = user must update, FLEXIBLE = user can continue
                        this,
                        UPDATE_REQUEST_CODE
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


    private fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }


    override fun onResume() {
        super.onResume()
        // If update was in progress (like Flexible update), resume it
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    this,
                    UPDATE_REQUEST_CODE
                )
            }
        }
    }

}

