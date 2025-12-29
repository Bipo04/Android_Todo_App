package com.ledang.todoapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ledang.todoapp.R
import com.ledang.todoapp.data.entity.Task
import com.ledang.todoapp.data.enums.TaskCategory
import com.ledang.todoapp.data.enums.TaskStatus
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private var tasks: List<Task> = emptyList()
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvProjectName: TextView = view.findViewById(R.id.tv_project_name)
        val tvTaskTitle: TextView = view.findViewById(R.id.tv_task_title)
        val tvTime: TextView = view.findViewById(R.id.tv_time)
        val tvStatus: TextView = view.findViewById(R.id.tv_status)
        val imgCategory: ImageView = view.findViewById(R.id.img_category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task_list, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        
        holder.tvProjectName.text = getCategoryName(task.category)
        holder.tvTaskTitle.text = task.name
        holder.tvTime.text = timeFormat.format(Date(task.startTime))
        
        // Set status text and color
        when (task.status) {
            TaskStatus.TODO -> {
                holder.tvStatus.text = "To-do"
                holder.tvStatus.setTextColor(holder.itemView.context.getColor(R.color.primary_purple))
            }
            TaskStatus.IN_PROGRESS -> {
                holder.tvStatus.text = "In Progress"
                holder.tvStatus.setTextColor(holder.itemView.context.getColor(R.color.status_in_progress))
            }
            TaskStatus.COMPLETED -> {
                holder.tvStatus.text = "Done"
                holder.tvStatus.setTextColor(holder.itemView.context.getColor(R.color.status_completed))
            }
            TaskStatus.OVERDUE -> {
                holder.tvStatus.text = "Overdue"
                holder.tvStatus.setTextColor(holder.itemView.context.getColor(R.color.status_overdue))
            }
        }
    }

    override fun getItemCount() = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    private fun getCategoryName(category: TaskCategory): String {
        return when (category) {
            TaskCategory.WORK -> "Work"
            TaskCategory.PERSONAL -> "Personal"
            TaskCategory.STUDY -> "Study"
            TaskCategory.SPORTS -> "Sports"
        }
    }
}
