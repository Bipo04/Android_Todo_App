package com.ledang.todoapp.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ledang.todoapp.R
import com.ledang.todoapp.data.entity.Task
import com.ledang.todoapp.data.enums.TaskCategory
import com.ledang.todoapp.data.enums.TaskStatus
import com.ledang.todoapp.data.database.TaskDatabase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class AddTaskFragment : Fragment() {

    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var etTaskName: EditText
    private lateinit var etDescription: EditText
    private lateinit var tvCategoryName: TextView
    
    private var startTimeMillis: Long = 0
    private var endTimeMillis: Long = 0
    private var selectedCategory: TaskCategory = TaskCategory.WORK
    
    private val dateTimeFormat = SimpleDateFormat("dd MMM, yyyy - hh:mm a", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvStartDate = view.findViewById(R.id.tv_start_date)
        tvEndDate = view.findViewById(R.id.tv_end_date)
        etTaskName = view.findViewById(R.id.et_project_name)
        etDescription = view.findViewById(R.id.et_description)
        tvCategoryName = view.findViewById(R.id.tv_category_name)

        // Category selector
        view.findViewById<View>(R.id.layout_category).setOnClickListener {
            showCategoryDialog()
        }

        // Start Date picker
        view.findViewById<View>(R.id.layout_start_date).setOnClickListener {
            showDateTimePicker { selectedDateTime ->
                startTimeMillis = selectedDateTime.time
                tvStartDate.text = dateTimeFormat.format(selectedDateTime)
            }
        }

        // End Date picker
        view.findViewById<View>(R.id.layout_end_date).setOnClickListener {
            showDateTimePicker { selectedDateTime ->
                endTimeMillis = selectedDateTime.time
                tvEndDate.text = dateTimeFormat.format(selectedDateTime)
            }
        }

        // Add Task button
        view.findViewById<View>(R.id.btn_add_project).setOnClickListener {
            saveTask()
        }
    }

    private fun showCategoryDialog() {
        val categories = arrayOf("Work", "Personal", "Study", "Sports")
        val categoryEnums = arrayOf(TaskCategory.WORK, TaskCategory.PERSONAL, TaskCategory.STUDY, TaskCategory.SPORTS)
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Category")
            .setItems(categories) { _, which ->
                selectedCategory = categoryEnums[which]
                tvCategoryName.text = categories[which]
            }
            .show()
    }

    private fun showDateTimePicker(onDateTimeSelected: (Date) -> Unit) {
        val currentCalendar = Calendar.getInstance()

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                currentCalendar.set(Calendar.YEAR, year)
                currentCalendar.set(Calendar.MONTH, month)
                currentCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        currentCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        currentCalendar.set(Calendar.MINUTE, minute)
                        onDateTimeSelected(currentCalendar.time)
                    },
                    currentCalendar.get(Calendar.HOUR_OF_DAY),
                    currentCalendar.get(Calendar.MINUTE),
                    false
                ).show()
            },
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveTask() {
        val taskName = etTaskName.text.toString().trim()
        val description = etDescription.text.toString().trim()

        // Validation
        if (taskName.isEmpty()) {
            Toast.makeText(context, "Please enter task name", Toast.LENGTH_SHORT).show()
            return
        }
        if (startTimeMillis == 0L) {
            Toast.makeText(context, "Please select start date", Toast.LENGTH_SHORT).show()
            return
        }
        if (endTimeMillis == 0L) {
            Toast.makeText(context, "Please select end date", Toast.LENGTH_SHORT).show()
            return
        }

        val task = Task(
            name = taskName,
            description = description,
            startTime = startTimeMillis,
            endTime = endTimeMillis,
            category = selectedCategory,
            status = TaskStatus.TODO
        )

        // Save to database in background thread
        thread {
            val db = TaskDatabase.getDatabase(requireContext())
            db.taskDao().insert(task)
            
            activity?.runOnUiThread {
                Toast.makeText(context, "Task added successfully!", Toast.LENGTH_SHORT).show()
                // Clear form
                etTaskName.text.clear()
                etDescription.text.clear()
                tvStartDate.text = "Select date"
                tvEndDate.text = "Select date"
                startTimeMillis = 0
                endTimeMillis = 0
            }
        }
    }
}
