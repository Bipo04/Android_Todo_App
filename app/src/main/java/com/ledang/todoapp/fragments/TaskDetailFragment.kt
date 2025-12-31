package com.ledang.todoapp.fragments

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
    private lateinit var tvDescription: TextView
    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView

    private val dateTimeFormat = SimpleDateFormat("dd MMM, yyyy - hh:mm a", Locale.getDefault())

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
        tvDescription = view.findViewById(R.id.tv_description)
        tvStartDate = view.findViewById(R.id.tv_start_date)
        tvEndDate = view.findViewById(R.id.tv_end_date)

        // Back button
        view.findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            (activity as? com.ledang.todoapp.MainActivity)?.goBack()
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
                task?.let { displayTask(it) }
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
        tvDescription.text = if (task.description.isNotEmpty()) task.description else "No description"

        // Dates
        tvStartDate.text = dateTimeFormat.format(Date(task.startTime))
        tvEndDate.text = dateTimeFormat.format(Date(task.endTime))
    }
}
