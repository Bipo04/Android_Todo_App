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
import com.ledang.todoapp.data.database.TaskDatabase
import com.ledang.todoapp.data.entity.Task
import com.ledang.todoapp.data.enums.TaskStatus

import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class TaskDetailFragment : Fragment() {

    private lateinit var imgCategoryIcon: ImageView
    private lateinit var tvCategoryName: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvTaskName: TextView
    private lateinit var etTaskName: EditText
    private lateinit var tvDescription: TextView
    private lateinit var etDescription: EditText
    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var layoutStartDate: View
    private lateinit var layoutEndDate: View
    private lateinit var imgStartDateEdit: ImageView
    private lateinit var imgEndDateEdit: ImageView
    private lateinit var btnSave: TextView

    private val dateTimeFormat = SimpleDateFormat("dd MMM, yyyy - hh:mm a", Locale.getDefault())
    
    private var currentTask: Task? = null
    private var isEditMode = false
    private var startTimeMillis: Long = 0
    private var endTimeMillis: Long = 0

    companion object {
        private const val ARG_TASK_ID = "task_id"

        fun newInstance(taskId: Long): TaskDetailFragment {
            return TaskDetailFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_TASK_ID, taskId)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        imgCategoryIcon = view.findViewById(R.id.img_category_icon)
        tvCategoryName = view.findViewById(R.id.tv_category_name)
        tvStatus = view.findViewById(R.id.tv_status)
        tvTaskName = view.findViewById(R.id.tv_task_name)
        etTaskName = view.findViewById(R.id.et_task_name)
        tvDescription = view.findViewById(R.id.tv_description)
        etDescription = view.findViewById(R.id.et_description)
        tvStartDate = view.findViewById(R.id.tv_start_date)
        tvEndDate = view.findViewById(R.id.tv_end_date)
        layoutStartDate = view.findViewById(R.id.layout_start_date)
        layoutEndDate = view.findViewById(R.id.layout_end_date)
        imgStartDateEdit = view.findViewById(R.id.img_start_date_edit)
        imgEndDateEdit = view.findViewById(R.id.img_end_date_edit)
        btnSave = view.findViewById(R.id.btn_save)

        // Back button
        view.findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            (activity as? com.ledang.todoapp.MainActivity)?.goBack()
        }

        // Save button
        btnSave.setOnClickListener {
            saveTask()
        }

        // Load task data
        val taskId = arguments?.getLong(ARG_TASK_ID) ?: return
        loadTaskData(taskId)
    }

    private fun loadTaskData(taskId: Long) {
        thread {
            val db = TaskDatabase.getDatabase(requireContext())
            val task = db.taskDao().getTaskById(taskId)

            activity?.runOnUiThread {
                task?.let { 
                    currentTask = it
                    startTimeMillis = it.startTime
                    endTimeMillis = it.endTime
                    displayTask(it) 
                }
            }
        }
    }

    private fun displayTask(task: Task) {
        val context = requireContext()
        val category = task.category

        // Category
        tvCategoryName.text = category.displayName
        imgCategoryIcon.setImageResource(category.iconRes)
        imgCategoryIcon.imageTintList = ContextCompat.getColorStateList(context, category.colorRes)

        // Category icon background
        val categoryBackground = imgCategoryIcon.background
        if (categoryBackground is GradientDrawable) {
            categoryBackground.setColor(ContextCompat.getColor(context, category.lightColorRes))
        } else {
            val newBackground = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(ContextCompat.getColor(context, category.lightColorRes))
            }
            imgCategoryIcon.background = newBackground
        }

        // Status
        when (task.status) {
            TaskStatus.TODO -> {
                tvStatus.text = "To-do"
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.primary_purple))
                // Enable edit mode for TODO tasks
                enableEditMode()
            }
            TaskStatus.IN_PROGRESS -> {
                tvStatus.text = "In Progress"
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_in_progress))
            }
            TaskStatus.COMPLETED -> {
                tvStatus.text = "Completed"
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_completed))
            }
            TaskStatus.OVERDUE -> {
                tvStatus.text = "Overdue"
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_overdue))
            }
        }

        // Task name and description
        tvTaskName.text = task.name
        etTaskName.setText(task.name)
        tvDescription.text = if (task.description.isNotEmpty()) task.description else "No description"
        etDescription.setText(task.description)

        // Dates
        tvStartDate.text = dateTimeFormat.format(Date(task.startTime))
        tvEndDate.text = dateTimeFormat.format(Date(task.endTime))
    }

    private fun enableEditMode() {
        isEditMode = true
        
        // Show EditText, hide TextView for editable fields
        tvTaskName.visibility = View.GONE
        etTaskName.visibility = View.VISIBLE
        tvDescription.visibility = View.GONE
        etDescription.visibility = View.VISIBLE
        
        // Show edit indicators for dates
        imgStartDateEdit.visibility = View.VISIBLE
        imgEndDateEdit.visibility = View.VISIBLE
        
        // Show save button
        btnSave.visibility = View.VISIBLE
        
        // Enable date pickers
        layoutStartDate.setOnClickListener {
            showDateTimePicker(startTimeMillis) { selectedDateTime ->
                startTimeMillis = selectedDateTime.time
                tvStartDate.text = dateTimeFormat.format(selectedDateTime)
            }
        }
        
        layoutEndDate.setOnClickListener {
            showDateTimePicker(endTimeMillis) { selectedDateTime ->
                endTimeMillis = selectedDateTime.time
                tvEndDate.text = dateTimeFormat.format(selectedDateTime)
            }
        }
    }

    private fun showDateTimePicker(initialTime: Long, onDateTimeSelected: (Date) -> Unit) {
        val currentCalendar = Calendar.getInstance()
        if (initialTime > 0) {
            currentCalendar.timeInMillis = initialTime
        }

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
        val task = currentTask ?: return
        
        val taskName = etTaskName.text.toString().trim()
        val description = etDescription.text.toString().trim()
        
        // Validation
        if (taskName.isEmpty()) {
            Toast.makeText(context, "Please enter task name", Toast.LENGTH_SHORT).show()
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
        
        // Create updated task
        val updatedTask = task.copy(
            name = taskName,
            description = description,
            startTime = startTimeMillis,
            endTime = endTimeMillis
        )
        
        // Save to database
        thread {
            val db = TaskDatabase.getDatabase(requireContext())
            db.taskDao().update(updatedTask)
            
            activity?.runOnUiThread {
                Toast.makeText(context, "Task updated successfully!", Toast.LENGTH_SHORT).show()
                (activity as? com.ledang.todoapp.MainActivity)?.goBack()
            }
        }
    }
}

