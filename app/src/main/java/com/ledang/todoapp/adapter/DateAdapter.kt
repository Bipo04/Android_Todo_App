package com.ledang.todoapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ledang.todoapp.R
import java.text.SimpleDateFormat
import java.util.*

data class DateItem(
    val date: Date,
    var isSelected: Boolean = false
)

class DateAdapter(
    private var dates: List<DateItem> = emptyList(),
    private val onDateClick: (DateItem, Int) -> Unit
) : RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

    private val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
    private val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())

    class DateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMonth: TextView = view.findViewById(R.id.tv_month)
        val tvDate: TextView = view.findViewById(R.id.tv_date)
        val tvDay: TextView = view.findViewById(R.id.tv_day)
        val frameDate: FrameLayout = view.findViewById(R.id.frame_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_date_selector, parent, false)
        return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val dateItem = dates[position]
        
        holder.tvMonth.text = monthFormat.format(dateItem.date)
        holder.tvDate.text = dayFormat.format(dateItem.date)
        holder.tvDay.text = dayOfWeekFormat.format(dateItem.date)

        // Update UI based on selection
        if (dateItem.isSelected) {
            holder.frameDate.setBackgroundResource(R.drawable.circle_date_selected)
            holder.tvDate.setTextColor(holder.itemView.context.getColor(R.color.white))
        } else {
            holder.frameDate.setBackgroundResource(R.drawable.circle_date_unselected)
            holder.tvDate.setTextColor(holder.itemView.context.getColor(R.color.text_title))
        }

        holder.itemView.setOnClickListener {
            onDateClick(dateItem, position)
        }
    }

    override fun getItemCount() = dates.size

    fun updateDates(newDates: List<DateItem>) {
        dates = newDates
        notifyDataSetChanged()
    }

    fun selectDate(position: Int) {
        dates.forEachIndexed { index, dateItem ->
            dateItem.isSelected = index == position
        }
        notifyDataSetChanged()
    }
}
