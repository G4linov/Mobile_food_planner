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
import com.example.nutrcouseproj.model.RegisterRequest
import com.example.nutrcouseproj.model.TokenResponse
import com.example.nutrcouseproj.service.AuthService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity: AppCompatActivity() {

    private val authService: AuthService = ApiClient.getClient().create(AuthService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val loginField: EditText = findViewById(R.id.login)
        val passwordField: EditText = findViewById(R.id.password_first_time)
        val confirmPasswordField: EditText = findViewById(R.id.password_second_time)
        val registerButton: Button = findViewById(R.id.register_button)
        val loginRedirect: TextView = findViewById(R.id.login_redirect)

        registerButton.setOnClickListener {
            val login = loginField.text.toString()
            val password = passwordField.text.toString()
            val passwordConfirm = confirmPasswordField.text.toString()

            // Validate input
            if (login.isBlank() || password.isBlank() || passwordConfirm.isBlank()) {
                Toast.makeText(this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != passwordConfirm) {
                Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val registerRequest = RegisterRequest(username = login, password = password)

            authService.register(registerRequest).enqueue(object : Callback<TokenResponse> {
                override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                    if (response.isSuccessful) {
                        val token = response.body()?.access_token
                        if (token != null) {
                            TokenManager.saveToken(applicationContext, token)
                            val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@RegisterActivity, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@RegisterActivity, "Регистрация не удалась: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                    Toast.makeText(this@RegisterActivity, "Ошибка подключения: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        loginRedirect.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}