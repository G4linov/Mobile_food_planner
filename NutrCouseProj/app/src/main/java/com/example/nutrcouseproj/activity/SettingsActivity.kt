package com.example.nutrcouseproj.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.nutrcouseproj.R
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var themeSwitch: SwitchMaterial
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backButton = findViewById<ImageButton>(R.id.back_button)

        backButton.setOnClickListener {
            finish() // Возврат к предыдущей активности без пересоздания
        }

        themeSwitch = findViewById(R.id.themeSwitch)
        sharedPreferences = getSharedPreferences("theme_prefs", MODE_PRIVATE)

        val isDarkMode = sharedPreferences.getBoolean("isDarkMode", false)
        themeSwitch.isChecked = isDarkMode

        // Устанавливаем тему без повторного вызова setDefaultNightMode, если не изменяется
        if (AppCompatDelegate.getDefaultNightMode() != getNightMode(isDarkMode)) {
            AppCompatDelegate.setDefaultNightMode(getNightMode(isDarkMode))
        }

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveThemePreference(isChecked)
            if (AppCompatDelegate.getDefaultNightMode() != getNightMode(isChecked)) {
                AppCompatDelegate.setDefaultNightMode(getNightMode(isChecked))
            }
        }
    }

    private fun getNightMode(isDarkMode: Boolean): Int {
        return if (isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
    }

    private fun saveThemePreference(isDarkMode: Boolean) {
        sharedPreferences.edit().putBoolean("isDarkMode", isDarkMode).apply()
    }
}
