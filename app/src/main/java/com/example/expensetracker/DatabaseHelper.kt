package com.example.expensetracker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, "expense.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE expenses (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id TEXT, " +
                    "amount REAL, " +
                    "category TEXT, " +
                    "date TEXT, " +
                    "currency TEXT, " +
                    "inr_amount REAL)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS expenses")
        onCreate(db)
    }

    // ---------- Expense operations ----------

    fun addExpense(userId: String, expense: Expense): Long {
        val values = ContentValues()
        values.put("user_id", userId)
        values.put("amount", expense.amount)
        values.put("category", expense.category)
        values.put("date", expense.date)
        values.put("currency", expense.currency)
        values.put("inr_amount", expense.inrAmount)
        return writableDatabase.insert("expenses", null, values)
    }

    fun updateExpense(expense: Expense): Int {
        val values = ContentValues()
        values.put("amount", expense.amount)
        values.put("category", expense.category)
        values.put("date", expense.date)
        values.put("currency", expense.currency)
        values.put("inr_amount", expense.inrAmount)
        return writableDatabase.update("expenses", values, "id = ?", arrayOf(expense.id.toString()))
    }

    fun deleteExpense(id: Int): Int {
        return writableDatabase.delete("expenses", "id = ?", arrayOf(id.toString()))
    }

    fun getExpense(id: Int): Expense? {
        val cursor = readableDatabase.rawQuery(
            "SELECT id, amount, category, date, currency, inr_amount FROM expenses WHERE id = ?",
            arrayOf(id.toString())
        )
        var expense: Expense? = null
        if (cursor.moveToFirst()) {
            expense = Expense(
                cursor.getInt(0), cursor.getDouble(1), cursor.getString(2),
                cursor.getString(3), cursor.getString(4), cursor.getDouble(5)
            )
        }
        cursor.close()
        return expense
    }

    // All expenses of the logged in user, newest date first
    fun getAllExpenses(userId: String): ArrayList<Expense> {
        val list = ArrayList<Expense>()
        val cursor = readableDatabase.rawQuery(
            "SELECT id, amount, category, date, currency, inr_amount FROM expenses " +
                    "WHERE user_id = ? ORDER BY date DESC, id DESC",
            arrayOf(userId)
        )
        while (cursor.moveToNext()) {
            list.add(
                Expense(
                    cursor.getInt(0), cursor.getDouble(1), cursor.getString(2),
                    cursor.getString(3), cursor.getString(4), cursor.getDouble(5)
                )
            )
        }
        cursor.close()
        return list
    }

    // ---------- Monthly summary (month looks like "2026-11") ----------

    fun getMonthTotal(userId: String, month: String): Double {
        val cursor = readableDatabase.rawQuery(
            "SELECT SUM(inr_amount) FROM expenses WHERE user_id = ? AND date LIKE ?",
            arrayOf(userId, "$month%")
        )
        var total = 0.0
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0)   // gives 0.0 when there are no rows
        }
        cursor.close()
        return total
    }

    fun getCategoryTotals(userId: String, month: String): HashMap<String, Double> {
        val totals = HashMap<String, Double>()
        val cursor = readableDatabase.rawQuery(
            "SELECT category, SUM(inr_amount) FROM expenses " +
                    "WHERE user_id = ? AND date LIKE ? GROUP BY category",
            arrayOf(userId, "$month%")
        )
        while (cursor.moveToNext()) {
            totals[cursor.getString(0)] = cursor.getDouble(1)
        }
        cursor.close()
        return totals
    }

    // ---------- Budget (saved in SharedPreferences) ----------

    fun saveBudget(userId: String, budget: Double) {
        val prefs = context.getSharedPreferences("budget_prefs", Context.MODE_PRIVATE)
        prefs.edit().putFloat("budget_$userId", budget.toFloat()).apply()
    }

    fun getBudget(userId: String): Double {
        val prefs = context.getSharedPreferences("budget_prefs", Context.MODE_PRIVATE)
        return prefs.getFloat("budget_$userId", 0f).toDouble()
    }

    companion object {
        // Current month in the same format as the saved dates, e.g. "2026-11"
        fun getCurrentMonth(): String {
            return SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
        }
    }
}
