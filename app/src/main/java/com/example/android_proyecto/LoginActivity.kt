package com.example.android_proyecto
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Observer

class LoginActivity : ComponentActivity() {

    private val vm: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUser = findViewById<EditText>(R.id.etUsername)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoRegister = findViewById<Button>(R.id.btnGoRegister)

        btnLogin.setOnClickListener {
            vm.doLogin(etUser.text.toString().trim(), etPass.text.toString())
        }

        btnGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        vm.loginState.observe(this, Observer { result ->
            result.onSuccess { user ->
                Toast.makeText(this, "Bienvenido ${user.username}", Toast.LENGTH_SHORT).show()
                // TODO: navega a tu pantalla principal
            }.onFailure { e ->
                Toast.makeText(this, e.message ?: "Error de login", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
