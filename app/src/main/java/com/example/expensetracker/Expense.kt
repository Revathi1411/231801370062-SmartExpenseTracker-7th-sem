package com.example.expensetracker

// One expense record
data class Expense(
    val id: Int = 0,
    val amount: Double,
    val category: String,
    val date: String,        // stored as yyyy-MM-dd
    val currency: String,
    val inrAmount: Double    // amount converted to INR (same as amount when currency is INR)
)

// Lists used by the spinners and the summary screen
val CATEGORIES = arrayOf("Food", "Travel", "Shopping", "Education", "Entertainment", "Others")
val CURRENCIES = arrayOf("INR", "USD", "EUR", "GBP", "AED", "AUD", "CAD", "JPY", "SGD")
