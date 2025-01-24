package com.example.nutrcouseproj.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.nutrcouseproj.R

class ProfileJournalActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_journal)

        val buttonDiary = findViewById<ImageButton>(R.id.button_diary)
        val backButton = findViewById<ImageButton>(R.id.back_button)
        val settingsButton = findViewById<ImageButton>(R.id.settings_button)


        buttonDiary.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        backButton.setOnClickListener {
            finish()
        }
    }
}