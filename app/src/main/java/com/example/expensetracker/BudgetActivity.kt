package com.example.expensetracker

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class BudgetActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var etBudget: EditText
    private lateinit var tvSpent: TextView
    private lateinit var tvBudgetStatus: TextView
    private lateinit var progressBudget: ProgressBar
    private var userId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            finish()
            return
        }
        userId = user.uid
        db = DatabaseHelper(this)

        etBudget = findViewById(R.id.etBudget)
        tvSpent = findViewById(R.id.tvSpent)
        tvBudgetStatus = findViewById(R.id.tvBudgetStatus)
        progressBudget = findViewById(R.id.progressBudget)
        val btnSaveBudget: Button = findViewById(R.id.btnSaveBudget)
        val btnBack: Button = findViewById(R.id.btnBack)

        // Show the saved budget if there is one
        val savedBudget = db.getBudget(userId)
        if (savedBudget > 0) {
            etBudget.setText(String.format(Locale.US, "%.2f", savedBudget))
        }
        updateStatus()

        btnSaveBudget.setOnClickListener { saveBudget() }
        btnBack.setOnClickListener { finish() }
    }

    private fun saveBudget() {
        val budget = etBudget.text.toString().toDoubleOrNull()
        if (budget == null || budget <= 0) {
            etBudget.error = "Enter a valid budget"
            return
        }
        db.saveBudget(userId, budget)
        Toast.makeText(this, "Budget saved", Toast.LENGTH_SHORT).show()
        updateStatus()
    }

    private fun updateStatus() {
        val budget = db.getBudget(userId)
        val spent = db.getMonthTotal(userId, DatabaseHelper.getCurrentMonth())

        tvSpent.text = "Spent this month: ₹ " + String.format(Locale.US, "%.2f", spent)

        if (budget <= 0) {
            progressBudget.progress = 0
            tvBudgetStatus.visibility = View.GONE
            return
        }

        val percent = spent / budget * 100
        progressBudget.progress = if (percent > 100) 100 else percent.toInt()

        if (percent >= 100) {
            tvBudgetStatus.text = "Budget exceeded! You have used " + percent.toInt() + "% of your budget."
            tvBudgetStatus.setTextColor(ContextCompat.getColor(this, R.color.red))
            tvBudgetStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.red_light))
            tvBudgetStatus.visibility = View.VISIBLE
        } else if (percent >= 80) {
            tvBudgetStatus.text = "Warning: You have used " + percent.toInt() + "% of your budget."
            tvBudgetStatus.setTextColor(ContextCompat.getColor(this, R.color.orange))
            tvBudgetStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.orange_light))
            tvBudgetStatus.visibility = View.VISIBLE
        } else {
            tvBudgetStatus.visibility = View.GONE
        }
    }
}
