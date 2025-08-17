package com.devkaran.qrhub

import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.devkaran.qrhub.model.Tester
import kotlinx.coroutines.MainScope

class TestersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testers)

        window.statusBarColor = ContextCompat.getColor(this, R.color.teal_700)

        val container: LinearLayout = findViewById(R.id.testersContainer)

        // Static list of testers (only LinkedIn & GitHub)
        val testers = listOf(
            Tester("Tejaswini Kokate ", "https://www.linkedin.com/in/tejaswini-kokate-044bba322", "https://github.com/"),
            Tester("Atul Bhagwat ", "https://www.linkedin.com/in/atul-bhagwat-9134b6259/?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app", "https://github.com/"),
            Tester("Meghana Sukre", "https://www.linkedin.com/in/meghana-sukre-023255366", "https://github.com/"),
            Tester("Abhijeet Ghanwat ", "https://www.linkedin.com/in/abhijeet-ghanwat-565004304?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app", "https://github.com/Abhi11-698"),
            Tester("Sultane Ajinkya", "https://www.linkedin.com/in/ajinkya-sultane?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app", "https://github.com/"),
            Tester("Developer Mahesh", "http://linkedin.com/in/maheshsangule/", "http://github.com/maheshsangule"),
            Tester("Abhay Bhaware", "https://www.linkedin.com/in/abhay-bhaware-1172b4289?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app", "https://github.com/AbhayBhaware"),
            Tester("Kedare Tanmay", "https://www.linkedin.com/in/tanmay-kedare-956705280?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app", "https://github.com/"),
            Tester("Targhale Seema", "https://www.linkedin.com/in/seema-targhale-191230329?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app", "https://github.com/SeemaTarghale"),
            Tester("Sagar Kharat ", "https://www.linkedin.com/in/sagar-kharat-b39b15292?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app", "https://github.com/SagarKharat1122?tab=repositories"),
            Tester("Kaveri Shelke ", "https://www.linkedin.com/in/kaveri-shelke-823788301?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app", "http://github.com/"),
            Tester("Mahesh Thombare ", "https://www.linkedin.com/in/mahesh-thombare-b05a52332/", "https://github.com/"),
            Tester("Sakshi Randhave", "https://www.linkedin.com/in/sakshi-randhave-685a44328/", "https://github.com/")

        ).shuffled() // 👈 shuffle order randomly each time

        // Dynamically add tester views
        for (tester in testers) {
            val view = layoutInflater.inflate(R.layout.item_tester, container, false)

            val name = view.findViewById<TextView>(R.id.txtName)
            val btnLinkedIn = view.findViewById<ImageView>(R.id.btnLinkedIn)
            val btnGithub = view.findViewById<ImageView>(R.id.btnGithub)

            name.text = tester.name

            btnLinkedIn.setOnClickListener { openUrl(tester.linkedin) }
            btnGithub.setOnClickListener { openUrl(tester.github) }

            container.addView(view)
        }

        val back=findViewById<ImageView>(R.id.back).setOnClickListener {
            val intent=Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent=Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
