package com.example.expensetracker

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SummaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            finish()
            return
        }

        val tvMonth: TextView = findViewById(R.id.tvMonth)
        val tvTotal: TextView = findViewById(R.id.tvTotal)
        val container: LinearLayout = findViewById(R.id.categoryContainer)
        val btnBack: Button = findViewById(R.id.btnBack)

        val db = DatabaseHelper(this)
        val month = DatabaseHelper.getCurrentMonth()
        val total = db.getMonthTotal(user.uid, month)
        val categoryTotals = db.getCategoryTotals(user.uid, month)

        tvMonth.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        tvTotal.text = "Total: ₹ " + String.format(Locale.US, "%.2f", total)

        // One row (name, amount, progress bar) for every category
        for (category in CATEGORIES) {
            val amount = categoryTotals[category] ?: 0.0
            val row = LayoutInflater.from(this).inflate(R.layout.item_category, container, false)

            row.findViewById<TextView>(R.id.tvCategoryName).text = category
            row.findViewById<TextView>(R.id.tvCategoryAmount).text =
                "₹ " + String.format(Locale.US, "%.2f", amount)

            // progress bar shows this category's share of the month total
            val percent = if (total > 0) (amount / total * 100).toInt() else 0
            row.findViewById<ProgressBar>(R.id.progressCategory).progress = percent

            container.addView(row)
        }

        btnBack.setOnClickListener { finish() }
    }
}
