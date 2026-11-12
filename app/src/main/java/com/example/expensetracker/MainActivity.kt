package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseHelper
    private lateinit var adapter: ExpenseAdapter
    private lateinit var tvMonthTotal: TextView
    private lateinit var tvBudgetAlert: TextView
    private lateinit var tvEmpty: TextView
    private var userId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        // If nobody is logged in, go back to the login screen
        val user = auth.currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        userId = user.uid

        setContentView(R.layout.activity_main)
        db = DatabaseHelper(this)

        tvMonthTotal = findViewById(R.id.tvMonthTotal)
        tvBudgetAlert = findViewById(R.id.tvBudgetAlert)
        tvEmpty = findViewById(R.id.tvEmpty)
        val recyclerExpenses: RecyclerView = findViewById(R.id.recyclerExpenses)
        val btnAdd: Button = findViewById(R.id.btnAdd)
        val btnSummary: Button = findViewById(R.id.btnSummary)
        val btnBudget: Button = findViewById(R.id.btnBudget)
        val btnLogout: Button = findViewById(R.id.btnLogout)

        adapter = ExpenseAdapter(
            ArrayList(),
            { expense -> openEdit(expense) },
            { expense -> confirmDelete(expense) }
        )
        recyclerExpenses.layoutManager = LinearLayoutManager(this)
        recyclerExpenses.adapter = adapter

        btnAdd.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }
        btnSummary.setOnClickListener {
            startActivity(Intent(this, SummaryActivity::class.java))
        }
        btnBudget.setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
        }
        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // Refresh the list and totals every time we come back to this screen
    override fun onResume() {
        super.onResume()
        if (userId.isNotEmpty()) {
            loadData()
        }
    }

    private fun loadData() {
        val list = db.getAllExpenses(userId)
        adapter.updateList(list)
        if (list.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
        }

        val total = db.getMonthTotal(userId, DatabaseHelper.getCurrentMonth())
        tvMonthTotal.text = "This month: ₹ " + String.format(Locale.US, "%.2f", total)
        showBudgetAlert(total)
    }

    // Shows a warning at 80% and an alert at 100% of the budget
    private fun showBudgetAlert(total: Double) {
        val budget = db.getBudget(userId)
        if (budget <= 0) {
            tvBudgetAlert.visibility = View.GONE
            return
        }

        val percent = total / budget * 100
        if (percent >= 100) {
            tvBudgetAlert.text = "Budget exceeded! You have used " + percent.toInt() + "% of your budget."
            tvBudgetAlert.setTextColor(ContextCompat.getColor(this, R.color.red))
            tvBudgetAlert.setBackgroundColor(ContextCompat.getColor(this, R.color.red_light))
            tvBudgetAlert.visibility = View.VISIBLE
        } else if (percent >= 80) {
            tvBudgetAlert.text = "Warning: You have used " + percent.toInt() + "% of your budget."
            tvBudgetAlert.setTextColor(ContextCompat.getColor(this, R.color.orange))
            tvBudgetAlert.setBackgroundColor(ContextCompat.getColor(this, R.color.orange_light))
            tvBudgetAlert.visibility = View.VISIBLE
        } else {
            tvBudgetAlert.visibility = View.GONE
        }
    }

    private fun openEdit(expense: Expense) {
        val intent = Intent(this, AddExpenseActivity::class.java)
        intent.putExtra("expense_id", expense.id)
        startActivity(intent)
    }

    private fun confirmDelete(expense: Expense) {
        AlertDialog.Builder(this)
            .setTitle("Delete Expense")
            .setMessage("Are you sure you want to delete this expense?")
            .setPositiveButton("Delete") { _, _ ->
                db.deleteExpense(expense.id)
                loadData()   // list, month total and budget alert update here
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
