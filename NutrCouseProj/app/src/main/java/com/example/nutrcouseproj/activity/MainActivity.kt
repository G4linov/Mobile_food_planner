package com.example.nutrcouseproj.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nutrcouseproj.R
import com.example.nutrcouseproj.clients.ApiClient
import com.example.nutrcouseproj.clients.TokenManager
import com.example.nutrcouseproj.service.AuthService
import com.example.nutrcouseproj.model.TokenResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private val authService: AuthService = ApiClient.getClient().create(AuthService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginField: EditText = findViewById(R.id.login)
        val passwordField: EditText = findViewById(R.id.password)
        val loginButton: Button = findViewById(R.id.login_button)
        val registerRedirect: TextView = findViewById(R.id.register_redirect)

        loginButton.setOnClickListener {
            val login = loginField.text.toString()
            val password = passwordField.text.toString()

            authService.login(login, password).enqueue(object : Callback<TokenResponse> {
                override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                    if (response.isSuccessful) {
                        val token = response.body()?.access_token
                        if (token  != null) {
                            TokenManager.saveToken(applicationContext, token)
                        }
                        val intent = Intent(this@MainActivity, HomeActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@MainActivity, "Неверный логин или пароль", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Ошибка подключения", Toast.LENGTH_SHORT).show()
                }
            })
        }

        registerRedirect.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}