package com.ledang.todoapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ledang.todoapp.R
import com.ledang.todoapp.adapter.DateAdapter
import com.ledang.todoapp.adapter.DateItem
import com.ledang.todoapp.adapter.TaskAdapter
import com.ledang.todoapp.data.database.TaskDatabase
import com.ledang.todoapp.data.entity.Task
import com.ledang.todoapp.data.enums.TaskStatus
import java.util.*
import kotlin.concurrent.thread

class CalendarFragment : Fragment() {

    private lateinit var rvDates: RecyclerView
    private lateinit var rvTasks: RecyclerView
    private lateinit var dateAdapter: DateAdapter
    private lateinit var taskAdapter: TaskAdapter
    
    private var allTasks: List<Task> = emptyList()
    private var currentFilter: TaskStatus? = null
    private var selectedDate: Date = Date()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Date RecyclerView
        rvDates = view.findViewById(R.id.rv_dates)
        val dates = generateDates()
        
        dateAdapter = DateAdapter(dates) { dateItem, position ->
            selectedDate = dateItem.date
            dateAdapter.selectDate(position)
            loadTasksForDate()
        }
        rvDates.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvDates.adapter = dateAdapter

        // Setup Tasks RecyclerView
        rvTasks = view.findViewById(R.id.rv_tasks)
        taskAdapter = TaskAdapter()
        rvTasks.layoutManager = LinearLayoutManager(context)
        rvTasks.adapter = taskAdapter

        // Setup filter clicks
        setupFilters(view)
    }

    override fun onResume() {
        super.onResume()
        refreshDates()
        loadTasksForDate()
    }

    private fun generateDates(): List<DateItem> {
        val dates = mutableListOf<DateItem>()
        val calendar = Calendar.getInstance()
        
        // Start from 5 days ago so today is positioned nicely
        calendar.add(Calendar.DAY_OF_YEAR, -5)
        
        // Generate 16 days (5 before + today + 10 after)
        for (i in 0 until 16) {
            val isToday = isSameDay(calendar.time, Date())
            dates.add(DateItem(calendar.time, isToday))
            if (isToday) {
                selectedDate = calendar.time
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return dates
    }

    private fun refreshDates() {
        // Regenerate dates to ensure calendar is always up-to-date
        val newDates = generateDates()
        val todayIndex = newDates.indexOfFirst { it.isSelected }
        
        dateAdapter.updateDates(newDates)
        
        // Scroll to center on today
        if (todayIndex >= 0) {
            rvDates.post {
                val layoutManager = rvDates.layoutManager as LinearLayoutManager
                // Calculate item width: 48dp (circle) + 16dp (padding) + 8dp (margin) = 72dp
                val density = resources.displayMetrics.density
                val itemWidth = (72 * density).toInt()
                val offset = (rvDates.width / 2) - (itemWidth / 2)
                layoutManager.scrollToPositionWithOffset(todayIndex, offset)
            }
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun setupFilters(view: View) {
        val filterAll = view.findViewById<TextView>(R.id.filter_all)
        val filterTodo = view.findViewById<TextView>(R.id.filter_todo)
        val filterInProgress = view.findViewById<TextView>(R.id.filter_in_progress)
        val filterCompleted = view.findViewById<TextView>(R.id.filter_completed)
        val filterOverdue = view.findViewById<TextView>(R.id.filter_overdue)

        filterAll.setOnClickListener {
            currentFilter = null
            updateFilterUI(view, filterAll)
            applyFilter()
        }

        filterTodo.setOnClickListener {
            currentFilter = TaskStatus.TODO
            updateFilterUI(view, filterTodo)
            applyFilter()
        }

        filterInProgress.setOnClickListener {
            currentFilter = TaskStatus.IN_PROGRESS
            updateFilterUI(view, filterInProgress)
            applyFilter()
        }

        filterCompleted.setOnClickListener {
            currentFilter = TaskStatus.COMPLETED
            updateFilterUI(view, filterCompleted)
            applyFilter()
        }

        filterOverdue.setOnClickListener {
            currentFilter = TaskStatus.OVERDUE
            updateFilterUI(view, filterOverdue)
            applyFilter()
        }
    }

    private fun updateFilterUI(rootView: View, selectedFilter: TextView) {
        val filters = listOf(
            rootView.findViewById<TextView>(R.id.filter_all),
            rootView.findViewById<TextView>(R.id.filter_todo),
            rootView.findViewById<TextView>(R.id.filter_in_progress),
            rootView.findViewById<TextView>(R.id.filter_completed),
            rootView.findViewById<TextView>(R.id.filter_overdue)
        )

        filters.forEach { filter ->
            if (filter == selectedFilter) {
                filter.setBackgroundResource(R.drawable.chip_selected)
                filter.setTextColor(resources.getColor(R.color.white, null))
            } else {
                filter.setBackgroundResource(R.drawable.chip_unselected)
                filter.setTextColor(resources.getColor(R.color.primary_purple, null))
            }
        }
    }

    private fun loadTasksForDate() {
        thread {
            val db = TaskDatabase.getDatabase(requireContext())
            
            // Get start and end of selected day
            val calendar = Calendar.getInstance()
            calendar.time = selectedDate
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val endOfDay = calendar.timeInMillis
            
            allTasks = db.taskDao().getTasksByDate(startOfDay, endOfDay)
            
            activity?.runOnUiThread {
                applyFilter()
            }
        }
    }

    private fun applyFilter() {
        val filteredTasks = if (currentFilter == null) {
            allTasks
        } else {
            allTasks.filter { it.status == currentFilter }
        }
        taskAdapter.updateTasks(filteredTasks)
    }
}
