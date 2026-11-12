package com.example.expensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class ExpenseAdapter(
    private var list: ArrayList<Expense>,
    private val onEdit: (Expense) -> Unit,
    private val onDelete: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvInr: TextView = view.findViewById(R.id.tvInr)
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = list[position]

        holder.tvCategory.text = expense.category
        holder.tvDate.text = expense.date

        if (expense.currency == "INR") {
            holder.tvAmount.visibility = View.GONE
        } else {
            // show the original amount for foreign currency
            holder.tvAmount.visibility = View.VISIBLE
            holder.tvAmount.text =
                expense.currency + " " + String.format(Locale.US, "%.2f", expense.amount)
        }
        holder.tvInr.text = "₹ " + String.format(Locale.US, "%.2f", expense.inrAmount)

        holder.btnEdit.setOnClickListener { onEdit(expense) }
        holder.btnDelete.setOnClickListener { onDelete(expense) }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateList(newList: ArrayList<Expense>) {
        list = newList
        notifyDataSetChanged()
    }
}
