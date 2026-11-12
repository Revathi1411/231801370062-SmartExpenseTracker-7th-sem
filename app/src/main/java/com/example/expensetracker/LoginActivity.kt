package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        // If the user is already logged in, go straight to the dashboard
        if (auth.currentUser != null) {
            openMain()
            return
        }

        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)

        btnLogin.setOnClickListener { loginUser() }
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        if (email.isEmpty()) {
            etEmail.error = "Enter your email"
            return
        }
        if (password.isEmpty()) {
            etPassword.error = "Enter your password"
            return
        }

        btnLogin.isEnabled = false
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            btnLogin.isEnabled = true
            if (task.isSuccessful) {
                openMain()
            } else {
                Toast.makeText(this, "Login failed: " + task.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
