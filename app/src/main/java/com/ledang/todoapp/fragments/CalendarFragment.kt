package com.ledang.todoapp.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.ledang.todoapp.MainActivity
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
    private var isFirstLoad = true

    // Auto update status
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 60_000L

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateTaskStatuses()
            handler.postDelayed(this, updateInterval)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_calendar, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDateRecycler(view)
        setupTaskRecycler(view)
        setupFilters(view)
    }

    override fun onResume() {
        super.onResume()
        refreshDates()
        loadTasksForDate()
        updateTaskStatuses()
        handler.postDelayed(updateRunnable, updateInterval)
        isFirstLoad = false
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateRunnable)
    }

    // ================= DATE LIST =================

    private fun setupDateRecycler(view: View) {
        rvDates = view.findViewById(R.id.rv_dates)

        dateAdapter = DateAdapter(generateDates()) { item, position ->
            selectedDate = item.date
            dateAdapter.selectDate(position)
            loadTasksForDate()
        }

        rvDates.apply {
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = dateAdapter
        }

        // Snap center like Google Calendar
        LinearSnapHelper().attachToRecyclerView(rvDates)
    }

    private fun generateDates(): List<DateItem> {
        val list = mutableListOf<DateItem>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -5)

        repeat(16) {
            val date = cal.time
            val isToday = isSameDay(date, Date())

            if (isToday && isFirstLoad) {
                selectedDate = date
            }

            list.add(DateItem(date, isSameDay(date, selectedDate)))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    private fun refreshDates() {
        val dates = generateDates()
        val selectedIndex = dates.indexOfFirst { it.isSelected }

        dateAdapter.updateDates(dates)

        if (selectedIndex >= 0) {
            // Wait for layout then center the item
            rvDates.post {
                val layoutManager = rvDates.layoutManager as LinearLayoutManager
                // First scroll to make it visible
                layoutManager.scrollToPosition(selectedIndex)
                
                // Then center with offset
                rvDates.post {
                    val viewHolder = rvDates.findViewHolderForAdapterPosition(selectedIndex)
                    val itemWidth = viewHolder?.itemView?.width ?: return@post
                    val offset = (rvDates.width - itemWidth) / 2
                    layoutManager.scrollToPositionWithOffset(selectedIndex, offset)
                }
            }
        }
    }

    private fun isSameDay(d1: Date, d2: Date): Boolean {
        val c1 = Calendar.getInstance().apply { time = d1 }
        val c2 = Calendar.getInstance().apply { time = d2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    // ================= TASK LIST =================

    private fun setupTaskRecycler(view: View) {
        rvTasks = view.findViewById(R.id.rv_tasks)

        taskAdapter = TaskAdapter(
            onCompleteClick = { markTaskCompleted(it) },
            onItemClick = {
                (activity as? MainActivity)?.navigateToTaskDetail(it.id)
            }
        )

        rvTasks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = taskAdapter
        }
    }

    private fun loadTasksForDate() {
        thread {
            val db = TaskDatabase.getDatabase(requireContext())
            val cal = Calendar.getInstance().apply {
                time = selectedDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val start = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val end = cal.timeInMillis

            allTasks = db.taskDao().getTasksByDate(start, end)

            activity?.runOnUiThread {
                applyFilter()
            }
        }
    }

    private fun applyFilter() {
        val result = currentFilter?.let {
            allTasks.filter { t -> t.status == it }
        } ?: allTasks

        taskAdapter.updateTasks(result)
    }

    private fun markTaskCompleted(task: Task) {
        thread {
            val db = TaskDatabase.getDatabase(requireContext())
            db.taskDao().updateStatus(task.id, TaskStatus.COMPLETED)

            activity?.runOnUiThread {
                Toast.makeText(context, "Task completed!", Toast.LENGTH_SHORT).show()
                loadTasksForDate()
            }
        }
    }

    // ================= FILTER =================

    private fun setupFilters(view: View) {
        val map = mapOf(
            R.id.filter_all to null,
            R.id.filter_todo to TaskStatus.TODO,
            R.id.filter_in_progress to TaskStatus.IN_PROGRESS,
            R.id.filter_completed to TaskStatus.COMPLETED,
            R.id.filter_overdue to TaskStatus.OVERDUE
        )

        map.forEach { (id, status) ->
            view.findViewById<TextView>(id).setOnClickListener {
                currentFilter = status
                updateFilterUI(view, it as TextView)
                applyFilter()
            }
        }
    }

    private fun updateFilterUI(root: View, selected: TextView) {
        val ids = listOf(
            R.id.filter_all,
            R.id.filter_todo,
            R.id.filter_in_progress,
            R.id.filter_completed,
            R.id.filter_overdue
        )

        ids.forEach {
            val tv = root.findViewById<TextView>(it)
            if (tv == selected) {
                tv.setBackgroundResource(R.drawable.chip_selected)
                tv.setTextColor(resources.getColor(R.color.white, null))
            } else {
                tv.setBackgroundResource(R.drawable.chip_unselected)
                tv.setTextColor(resources.getColor(R.color.primary_purple, null))
            }
        }
    }

    // ================= AUTO STATUS =================

    private fun updateTaskStatuses() {
        thread {
            val db = TaskDatabase.getDatabase(requireContext())
            val now = System.currentTimeMillis()

            db.taskDao().getTasksToStartProgress(now)
                .forEach { db.taskDao().updateStatus(it.id, TaskStatus.IN_PROGRESS) }

            db.taskDao().getTasksToMarkOverdue(now)
                .forEach { db.taskDao().updateStatus(it.id, TaskStatus.OVERDUE) }
        }
    }
}
