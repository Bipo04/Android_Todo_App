package com.ledang.todoapp.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ledang.todoapp.R
import com.ledang.todoapp.data.entity.Task
import com.ledang.todoapp.data.enums.TaskCategory
import com.ledang.todoapp.data.enums.TaskStatus
import com.ledang.todoapp.data.database.TaskDatabase
import com.ledang.todoapp.notification.TaskAlarmScheduler
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class AddTaskFragment : Fragment() {

    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var etTaskName: EditText
    private lateinit var etDescription: EditText
    private lateinit var tvCategoryName: TextView
    private lateinit var imgCategoryIcon: ImageView
    
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
        imgCategoryIcon = view.findViewById(R.id.img_category_icon)

        // Set initial category display
        updateCategoryUI()

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
        val categories = TaskCategory.values()
        val categoryNames = categories.map { it.displayName }.toTypedArray()
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Category")
            .setItems(categoryNames) { _, which ->
                selectedCategory = categories[which]
                updateCategoryUI()
            }
            .show()
    }

    private fun updateCategoryUI() {
        val context = requireContext()
        
        // Update category name
        tvCategoryName.text = selectedCategory.displayName
        
        // Update icon
        imgCategoryIcon.setImageResource(selectedCategory.iconRes)
        imgCategoryIcon.imageTintList = ContextCompat.getColorStateList(context, selectedCategory.colorRes)
        
        // Update background color (light version)
        val backgroundDrawable = imgCategoryIcon.background
        if (backgroundDrawable is GradientDrawable) {
            backgroundDrawable.setColor(ContextCompat.getColor(context, selectedCategory.lightColorRes))
        } else {
            val newBackground = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(ContextCompat.getColor(context, selectedCategory.lightColorRes))
            }
            imgCategoryIcon.background = newBackground
        }
    }

    private fun showDateTimePicker(onDateTimeSelected: (Date) -> Unit) {
        val currentCalendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
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
                        
                        // Validate that selected time is not in the past
                        if (currentCalendar.timeInMillis < System.currentTimeMillis()) {
                            Toast.makeText(context, "Cannot select a time in the past", Toast.LENGTH_SHORT).show()
                            return@TimePickerDialog
                        }
                        
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
        )
        
        // Set minimum date to today - prevent selecting past dates
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
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
        if (startTimeMillis < System.currentTimeMillis()) {
            Toast.makeText(context, "Start time cannot be in the past", Toast.LENGTH_SHORT).show()
            return
        }
        if (endTimeMillis < System.currentTimeMillis()) {
            Toast.makeText(context, "End time cannot be in the past", Toast.LENGTH_SHORT).show()
            return
        }
        if (startTimeMillis > endTimeMillis) {
            Toast.makeText(context, "Start time cannot be after end time", Toast.LENGTH_SHORT).show()
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
            val taskId = db.taskDao().insert(task)
            
            // Schedule task reminders
            val savedTask = task.copy(id = taskId)
            TaskAlarmScheduler.scheduleTaskReminders(requireContext(), savedTask)
            
            activity?.runOnUiThread {
                Toast.makeText(context, "Task added successfully!", Toast.LENGTH_SHORT).show()
                // Clear form
                etTaskName.text.clear()
                etDescription.text.clear()
                tvStartDate.text = "Select date"
                tvEndDate.text = "Select date"
                startTimeMillis = 0
                endTimeMillis = 0
                // Reset to default category
                selectedCategory = TaskCategory.WORK
                updateCategoryUI()
            }
        }
    }
}
