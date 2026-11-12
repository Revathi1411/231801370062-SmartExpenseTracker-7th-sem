package com.example.expensetracker

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// This screen is used for both adding and editing an expense.
// If an "expense_id" is passed in the Intent, it works in edit mode.
class AddExpenseActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var tvTitle: TextView
    private lateinit var etAmount: EditText
    private lateinit var spCurrency: Spinner
    private lateinit var spCategory: Spinner
    private lateinit var btnDate: Button
    private lateinit var btnScan: Button
    private lateinit var btnConvert: Button
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var tvConverted: TextView

    private var userId = ""
    private var expenseId = -1
    private var selectedDate = ""

    // Opens the gallery and gives us the chosen image
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scanReceipt(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            finish()
            return
        }
        userId = user.uid
        db = DatabaseHelper(this)

        tvTitle = findViewById(R.id.tvTitle)
        etAmount = findViewById(R.id.etAmount)
        spCurrency = findViewById(R.id.spCurrency)
        spCategory = findViewById(R.id.spCategory)
        btnDate = findViewById(R.id.btnDate)
        btnScan = findViewById(R.id.btnScan)
        btnConvert = findViewById(R.id.btnConvert)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        tvConverted = findViewById(R.id.tvConverted)

        spCurrency.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, CURRENCIES)
        spCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, CATEGORIES)

        // Default date is today
        selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        btnDate.text = selectedDate

        // Edit mode: fill the fields with the saved expense
        expenseId = intent.getIntExtra("expense_id", -1)
        if (expenseId != -1) {
            loadExpense()
        }

        btnDate.setOnClickListener { showDatePicker() }
        btnScan.setOnClickListener { pickImage.launch("image/*") }
        btnConvert.setOnClickListener { previewConversion() }
        btnSave.setOnClickListener { saveExpense() }
        btnCancel.setOnClickListener { finish() }
    }

    private fun loadExpense() {
        val expense = db.getExpense(expenseId)
        if (expense == null) {
            finish()
            return
        }
        tvTitle.text = "Edit Expense"
        etAmount.setText(String.format(Locale.US, "%.2f", expense.amount))
        spCurrency.setSelection(CURRENCIES.indexOf(expense.currency))
        spCategory.setSelection(CATEGORIES.indexOf(expense.category))
        selectedDate = expense.date
        btnDate.text = selectedDate
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select expense date")
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            // MaterialDatePicker gives the date in UTC milliseconds
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            selectedDate = format.format(Date(selection))
            btnDate.text = selectedDate
        }
        picker.show(supportFragmentManager, "date_picker")
    }

    // ---------- Receipt scanning ----------

    private fun scanReceipt(uri: Uri) {
        Toast.makeText(this, "Scanning receipt...", Toast.LENGTH_SHORT).show()
        ReceiptScanner.scan(
            this, uri,
            onResult = { amount ->
                if (amount == null) {
                    Toast.makeText(this, "No amount found. Please type it.", Toast.LENGTH_LONG).show()
                } else {
                    etAmount.setText(String.format(Locale.US, "%.2f", amount))
                    Toast.makeText(this, "Amount detected. You can edit it.", Toast.LENGTH_LONG).show()
                }
            },
            onError = { message ->
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        )
    }

    // ---------- Currency conversion ----------

    private fun isInternetAvailable(): Boolean {
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Gets the live rate and gives back the amount in INR
    private fun convertToInr(amount: Double, currency: String, onDone: (Double) -> Unit) {
        if (!isInternetAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show()
            return
        }

        btnSave.isEnabled = false
        btnConvert.isEnabled = false
        CurrencyConverter.getRate(currency) { rate, error ->
            btnSave.isEnabled = true
            btnConvert.isEnabled = true
            if (rate == null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            } else {
                val inr = Math.round(amount * rate * 100) / 100.0
                onDone(inr)
            }
        }
    }

    private fun getValidAmount(): Double? {
        val amount = etAmount.text.toString().toDoubleOrNull()
        if (amount == null || amount <= 0) {
            etAmount.error = "Enter a valid amount"
            return null
        }
        return amount
    }

    // "Convert to INR" button: only shows the converted amount
    private fun previewConversion() {
        val amount = getValidAmount() ?: return
        val currency = spCurrency.selectedItem.toString()

        if (currency == "INR") {
            tvConverted.text = "Currency is already INR"
            return
        }
        convertToInr(amount, currency) { inr ->
            tvConverted.text = String.format(Locale.US, "%.2f %s = ₹ %.2f", amount, currency, inr)
        }
    }

    // ---------- Saving ----------

    private fun saveExpense() {
        val amount = getValidAmount() ?: return
        val currency = spCurrency.selectedItem.toString()

        if (currency == "INR") {
            saveToDatabase(amount, currency, amount)
        } else {
            convertToInr(amount, currency) { inr ->
                saveToDatabase(amount, currency, inr)
            }
        }
    }

    private fun saveToDatabase(amount: Double, currency: String, inrAmount: Double) {
        val category = spCategory.selectedItem.toString()

        if (expenseId == -1) {
            val expense = Expense(0, amount, category, selectedDate, currency, inrAmount)
            db.addExpense(userId, expense)
            Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show()
        } else {
            val expense = Expense(expenseId, amount, category, selectedDate, currency, inrAmount)
            db.updateExpense(expense)
            Toast.makeText(this, "Expense updated", Toast.LENGTH_SHORT).show()
        }
        checkBudgetAndFinish()
    }

    // After saving, warn the user if the monthly budget is nearly used or exceeded
    private fun checkBudgetAndFinish() {
        val budget = db.getBudget(userId)
        val total = db.getMonthTotal(userId, DatabaseHelper.getCurrentMonth())

        if (budget > 0) {
            val percent = total / budget * 100
            if (percent >= 100) {
                AlertDialog.Builder(this)
                    .setTitle("Budget Exceeded")
                    .setMessage("You have used " + percent.toInt() + "% of your monthly budget.")
                    .setCancelable(false)
                    .setPositiveButton("OK") { _, _ -> finish() }
                    .show()
                return
            } else if (percent >= 80) {
                Toast.makeText(
                    this,
                    "Warning: You have used " + percent.toInt() + "% of your budget",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        finish()
    }
}
