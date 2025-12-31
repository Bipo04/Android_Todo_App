package com.ledang.todoapp.adapter

import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ledang.todoapp.R
import com.ledang.todoapp.data.entity.Task
import com.ledang.todoapp.data.enums.TaskStatus
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private var tasks: List<Task> = emptyList(),
    private val onCompleteClick: ((Task) -> Unit)? = null,
    private val onItemClick: ((Task) -> Unit)? = null
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvProjectName: TextView = view.findViewById(R.id.tv_project_name)
        val tvTaskTitle: TextView = view.findViewById(R.id.tv_task_title)
        val tvTime: TextView = view.findViewById(R.id.tv_time)
        val tvStatus: TextView = view.findViewById(R.id.tv_status)
        val imgCategory: ImageView = view.findViewById(R.id.img_category)
        val btnComplete: ImageView = view.findViewById(R.id.btn_complete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task_list, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        val context = holder.itemView.context
        val category = task.category
        
        // Set category name using enum's displayName
        holder.tvProjectName.text = category.displayName
        holder.tvTaskTitle.text = task.name
        holder.tvTime.text = timeFormat.format(Date(task.startTime))
        
        // Set category icon and colors
        holder.imgCategory.setImageResource(category.iconRes)
        holder.imgCategory.imageTintList = ContextCompat.getColorStateList(context, category.colorRes)
        
        // Set background color (light version)
        val backgroundDrawable = holder.imgCategory.background
        if (backgroundDrawable is GradientDrawable) {
            backgroundDrawable.setColor(ContextCompat.getColor(context, category.lightColorRes))
        } else {
            val newBackground = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(ContextCompat.getColor(context, category.lightColorRes))
            }
            holder.imgCategory.background = newBackground
        }
        
        // Set status text, color and complete button state
        when (task.status) {
            TaskStatus.TODO -> {
                holder.tvStatus.text = "To-do"
                holder.tvStatus.setTextColor(context.getColor(R.color.primary_purple))
                holder.btnComplete.setImageResource(R.drawable.ic_check_circle)
                holder.btnComplete.imageTintList = ContextCompat.getColorStateList(context, R.color.text_description)
                holder.tvTaskTitle.paintFlags = holder.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                holder.btnComplete.isEnabled = false // Cannot complete TODO - not started yet
                holder.btnComplete.alpha = 0.3f
            }
            TaskStatus.IN_PROGRESS -> {
                holder.tvStatus.text = "In Progress"
                holder.tvStatus.setTextColor(context.getColor(R.color.status_in_progress))
                holder.btnComplete.setImageResource(R.drawable.ic_check_circle)
                holder.btnComplete.imageTintList = ContextCompat.getColorStateList(context, R.color.status_in_progress)
                holder.tvTaskTitle.paintFlags = holder.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                holder.btnComplete.isEnabled = true
                holder.btnComplete.alpha = 1f
            }
            TaskStatus.COMPLETED -> {
                holder.tvStatus.text = "Done"
                holder.tvStatus.setTextColor(context.getColor(R.color.status_completed))
                holder.btnComplete.setImageResource(R.drawable.ic_check_circle_filled)
                holder.btnComplete.imageTintList = ContextCompat.getColorStateList(context, R.color.status_completed)
                holder.tvTaskTitle.paintFlags = holder.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                holder.btnComplete.isEnabled = false
                holder.btnComplete.alpha = 1f
            }
            TaskStatus.OVERDUE -> {
                holder.tvStatus.text = "Overdue"
                holder.tvStatus.setTextColor(context.getColor(R.color.status_overdue))
                holder.btnComplete.setImageResource(R.drawable.ic_check_circle)
                holder.btnComplete.imageTintList = ContextCompat.getColorStateList(context, R.color.status_overdue)
                holder.tvTaskTitle.paintFlags = holder.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                holder.btnComplete.isEnabled = true // Allow completing overdue tasks
                holder.btnComplete.alpha = 1f
            }
        }
        
        // Complete button click listener - only for IN_PROGRESS or OVERDUE
        holder.btnComplete.setOnClickListener {
            if (task.status == TaskStatus.IN_PROGRESS || task.status == TaskStatus.OVERDUE) {
                onCompleteClick?.invoke(task)
            }
        }
        
        // Item click listener - navigate to task detail
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(task)
        }
    }

    override fun getItemCount() = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}
