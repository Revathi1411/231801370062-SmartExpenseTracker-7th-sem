package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)

        btnRegister.setOnClickListener { registerUser() }
        tvLogin.setOnClickListener { finish() }   // go back to login screen
    }

    private fun registerUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val confirm = etConfirmPassword.text.toString()

        if (email.isEmpty()) {
            etEmail.error = "Enter your email"
            return
        }
        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            return
        }
        if (password != confirm) {
            etConfirmPassword.error = "Passwords do not match"
            return
        }

        btnRegister.isEnabled = false
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            btnRegister.isEnabled = true
            if (task.isSuccessful) {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                // Firebase logs the new user in automatically, so open the dashboard
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                Toast.makeText(this, "Registration failed: " + task.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
