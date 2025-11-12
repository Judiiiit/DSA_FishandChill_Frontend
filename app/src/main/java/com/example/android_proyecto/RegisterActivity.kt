package com.example.android_proyecto
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Observer

class RegisterActivity : ComponentActivity() {

    private val vm: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etUser = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            vm.doRegister(
                etUser.text.toString().trim(),
                etEmail.text.toString().trim(),
                etPass.text.toString()
            )
        }

        vm.registerState.observe(this, Observer { result ->
            result.onSuccess { user ->
                Toast.makeText(this, "Usuario creado: ${user.username}", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure { e ->
                Toast.makeText(this, e.message ?: "Error de registro", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
