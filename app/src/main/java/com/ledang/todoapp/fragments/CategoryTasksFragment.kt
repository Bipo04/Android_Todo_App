package com.ledang.todoapp.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ledang.todoapp.MainActivity
import com.ledang.todoapp.R
import com.ledang.todoapp.data.database.TaskDatabase
import com.ledang.todoapp.data.entity.Task
import com.ledang.todoapp.data.enums.TaskCategory
import com.ledang.todoapp.data.enums.TaskStatus

import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class CategoryTasksFragment : Fragment() {

    private lateinit var rvTasks: RecyclerView
    private lateinit var tvTitle: TextView
    private lateinit var btnBack: ImageView
    private lateinit var btnDelete: TextView
    private lateinit var layoutEmpty: View
    
    private var category: TaskCategory = TaskCategory.WORK
    private val selectedTaskIds = mutableSetOf<Long>()
    private var taskAdapter: TaskAdapter? = null
    
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())

    companion object {
        private const val ARG_CATEGORY = "category"

        fun newInstance(category: TaskCategory): CategoryTasksFragment {
            return CategoryTasksFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY, category.name)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getString(ARG_CATEGORY)?.let {
            category = TaskCategory.valueOf(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_category_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvTasks = view.findViewById(R.id.rv_tasks)
        tvTitle = view.findViewById(R.id.tv_title)
        btnBack = view.findViewById(R.id.btn_back)
        btnDelete = view.findViewById(R.id.btn_delete)
        layoutEmpty = view.findViewById(R.id.layout_empty)

        tvTitle.text = "${category.displayName}'s Tasks"
        rvTasks.layoutManager = LinearLayoutManager(context)

        btnBack.setOnClickListener {
            (activity as? MainActivity)?.goBack()
        }

        btnDelete.setOnClickListener {
            confirmDelete()
        }

        loadTasks()
    }

    private fun loadTasks() {
        thread {
            val db = TaskDatabase.getDatabase(requireContext())
            val tasks = db.taskDao().getTasksByCategory(category)

            activity?.runOnUiThread {
                if (tasks.isEmpty()) {
                    layoutEmpty.visibility = View.VISIBLE
                    rvTasks.visibility = View.GONE
                } else {
                    layoutEmpty.visibility = View.GONE
                    rvTasks.visibility = View.VISIBLE
                    taskAdapter = TaskAdapter(tasks)
                    rvTasks.adapter = taskAdapter
                }
            }
        }
    }

    private fun updateDeleteButton() {
        if (selectedTaskIds.isEmpty()) {
            btnDelete.visibility = View.INVISIBLE
        } else {
            btnDelete.visibility = View.VISIBLE
            btnDelete.text = "Delete (${selectedTaskIds.size})"
        }
    }

    private fun confirmDelete() {
        if (selectedTaskIds.isEmpty()) return

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Tasks")
            .setMessage("Are you sure you want to delete ${selectedTaskIds.size} task(s)?")
            .setPositiveButton("Delete") { _, _ ->
                deleteTasks()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteTasks() {
        thread {
            val db = TaskDatabase.getDatabase(requireContext())
            val dao = db.taskDao()
            
            selectedTaskIds.forEach { taskId ->
                dao.deleteById(taskId)
            }

            activity?.runOnUiThread {
                Toast.makeText(context, "${selectedTaskIds.size} task(s) deleted", Toast.LENGTH_SHORT).show()
                selectedTaskIds.clear()
                updateDeleteButton()
                loadTasks()
            }
        }
    }

    inner class TaskAdapter(
        private val tasks: List<Task>
    ) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvDescription: TextView = view.findViewById(R.id.tv_description)
            val tvTaskName: TextView = view.findViewById(R.id.tv_task_name)
            val tvDate: TextView = view.findViewById(R.id.tv_date)
            val tvStatus: TextView = view.findViewById(R.id.tv_status)
            val checkbox: CheckBox = view.findViewById(R.id.checkbox_select)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_task_selectable, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val task = tasks[position]
            val context = holder.itemView.context

            holder.tvDescription.text = task.description.ifEmpty { task.category.displayName }
            holder.tvTaskName.text = task.name
            holder.tvDate.text = dateFormat.format(Date(task.endTime))

            // Status
            when (task.status) {
                TaskStatus.TODO -> {
                    holder.tvStatus.text = "To-do"
                    holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.primary_purple))
                }
                TaskStatus.IN_PROGRESS -> {
                    holder.tvStatus.text = "In Progress"
                    holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_in_progress))
                }
                TaskStatus.COMPLETED -> {
                    holder.tvStatus.text = "Completed"
                    holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_completed))
                }
                TaskStatus.OVERDUE -> {
                    holder.tvStatus.text = "Overdue"
                    holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_overdue))
                }
            }

            // Checkbox state
            holder.checkbox.isChecked = selectedTaskIds.contains(task.id)
            
            holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedTaskIds.add(task.id)
                } else {
                    selectedTaskIds.remove(task.id)
                }
                updateDeleteButton()
            }

            // Click on item to view task detail
            holder.itemView.setOnClickListener {
                (activity as? MainActivity)?.navigateToTaskDetail(task.id)
            }
        }

        override fun getItemCount() = tasks.size
    }
}
