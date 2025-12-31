package com.ledang.todoapp.fragments

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ledang.todoapp.MainActivity
import com.ledang.todoapp.R
import com.ledang.todoapp.data.database.TaskDatabase
import com.ledang.todoapp.data.entity.Task
import com.ledang.todoapp.data.enums.TaskCategory
import com.ledang.todoapp.data.enums.TaskStatus
import java.util.*
import kotlin.concurrent.thread

class HomeFragment : Fragment() {
    
    private lateinit var layoutTaskGroups: LinearLayout
    private lateinit var layoutInProgressTasks: LinearLayout
    private lateinit var scrollInProgress: HorizontalScrollView
    private lateinit var cardEmptyInProgress: CardView
    private lateinit var tvTaskGroupsCount: TextView
    private lateinit var tvInProgressCount: TextView
    private lateinit var progressToday: ProgressBar
    private lateinit var tvTodayProgressPercent: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutTaskGroups = view.findViewById(R.id.layout_task_groups)
        layoutInProgressTasks = view.findViewById(R.id.layout_in_progress_tasks)
        scrollInProgress = view.findViewById(R.id.scroll_in_progress)
        cardEmptyInProgress = view.findViewById(R.id.card_empty_in_progress)
        tvTaskGroupsCount = view.findViewById(R.id.tv_task_groups_count)
        tvInProgressCount = view.findViewById(R.id.tv_in_progress_count)
        progressToday = view.findViewById(R.id.progress_today)
        tvTodayProgressPercent = view.findViewById(R.id.tv_today_progress_percent)

        // Button "View Task" -> open CalendarFragment
        view.findViewById<TextView>(R.id.btn_view_task).setOnClickListener {
            (activity as? MainActivity)?.navigateToCalendar()
        }
        
        // Button "Add Task" in empty state -> open AddTaskFragment
        view.findViewById<TextView>(R.id.btn_add_task_empty).setOnClickListener {
            (activity as? MainActivity)?.navigateToAddTask()
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        thread {
            val db = TaskDatabase.getDatabase(requireContext())
            val dao = db.taskDao()
            
            // Get today's date range
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val endOfDay = calendar.timeInMillis
            
            // Calculate today's progress
            val todayTotal = dao.countTasksForToday(startOfDay, endOfDay)
            val todayCompleted = dao.countCompletedTasksForToday(startOfDay, endOfDay)
            val todayProgress = if (todayTotal > 0) (todayCompleted * 100 / todayTotal) else 0
            
            // Get in progress tasks
            val inProgressTasks = dao.getTasksByStatus(TaskStatus.IN_PROGRESS)
            
            // Get task groups data for each category
            val categories = TaskCategory.values()
            val taskGroupsData = categories.map { category ->
                val total = dao.countTasksByCategory(category)
                val completed = dao.countCompletedTasksByCategory(category)
                val progress = if (total > 0) (completed * 100 / total) else 0
                TaskGroupData(category, total, progress)
            }.filter { it.totalTasks > 0 } // Only show categories with tasks
            
            activity?.runOnUiThread {
                // Update today's progress
                progressToday.progress = todayProgress
                tvTodayProgressPercent.text = "$todayProgress%"
                
                // Update in progress count
                tvInProgressCount.text = inProgressTasks.size.toString()
                
                // Populate in progress tasks
                populateInProgressTasks(inProgressTasks)
                
                // Update task groups count
                tvTaskGroupsCount.text = taskGroupsData.size.toString()
                
                // Populate task groups
                populateTaskGroups(taskGroupsData)
            }
        }
    }

    private fun populateInProgressTasks(tasks: List<Task>) {
        layoutInProgressTasks.removeAllViews()
        
        if (tasks.isEmpty()) {
            // Show empty state
            scrollInProgress.visibility = View.GONE
            cardEmptyInProgress.visibility = View.VISIBLE
        } else {
            // Show tasks
            scrollInProgress.visibility = View.VISIBLE
            cardEmptyInProgress.visibility = View.GONE
            
            val inflater = LayoutInflater.from(context)
            val context = requireContext()
            
            for (task in tasks) {
                val itemView = inflater.inflate(R.layout.item_task_progress_card, layoutInProgressTasks, false)
                
                val category = task.category
                
                val tvCategory = itemView.findViewById<TextView>(R.id.tv_category)
                val imgIcon = itemView.findViewById<ImageView>(R.id.img_task_icon)
                val tvTitle = itemView.findViewById<TextView>(R.id.tv_task_title)
                val progressBar = itemView.findViewById<ProgressBar>(R.id.progress_task)
                
                // Set category
                tvCategory.text = category.displayName
                tvCategory.setTextColor(ContextCompat.getColor(context, category.colorRes))
                
                // Set icon
                imgIcon.setImageResource(category.iconRes)
                imgIcon.imageTintList = ContextCompat.getColorStateList(context, category.colorRes)
                
                // Set title
                tvTitle.text = task.name
                
                // Calculate time progress (how much time has passed)
                val currentTime = System.currentTimeMillis()
                val totalDuration = task.endTime - task.startTime
                val elapsedTime = currentTime - task.startTime
                val timeProgress = if (totalDuration > 0) {
                    ((elapsedTime * 100) / totalDuration).coerceIn(0, 100).toInt()
                } else 0
                
                progressBar.progress = timeProgress
                progressBar.progressTintList = ContextCompat.getColorStateList(context, category.colorRes)
                
                // Set layout params with margin
                val params = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.card_width_180dp),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.marginEnd = resources.getDimensionPixelSize(R.dimen.spacing_12dp)
                itemView.layoutParams = params
                
                layoutInProgressTasks.addView(itemView)
            }
        }
    }

    private fun populateTaskGroups(taskGroups: List<TaskGroupData>) {
        layoutTaskGroups.removeAllViews()
        
        val inflater = LayoutInflater.from(context)
        
        for (group in taskGroups) {
            val itemView = inflater.inflate(R.layout.item_task_group, layoutTaskGroups, false)
            
            val category = group.category
            val context = requireContext()
            
            // Set icon and colors
            val frameIcon = itemView.findViewById<FrameLayout>(R.id.frame_icon)
            val imgIcon = itemView.findViewById<ImageView>(R.id.img_group_icon)
            val tvName = itemView.findViewById<TextView>(R.id.tv_group_name)
            val tvCount = itemView.findViewById<TextView>(R.id.tv_task_count)
            val progressBar = itemView.findViewById<ProgressBar>(R.id.progress_group)
            val tvPercent = itemView.findViewById<TextView>(R.id.tv_progress_percent)
            
            // Set icon
            imgIcon.setImageResource(category.iconRes)
            imgIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.white)
            
            // Set frame background color
            val backgroundDrawable = frameIcon.background
            if (backgroundDrawable is GradientDrawable) {
                backgroundDrawable.setColor(ContextCompat.getColor(context, category.colorRes))
            } else {
                val newBackground = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(ContextCompat.getColor(context, category.colorRes))
                }
                frameIcon.background = newBackground
            }
            
            // Set text
            tvName.text = category.displayName
            tvCount.text = "${group.totalTasks} Tasks"
            
            // Set progress
            progressBar.progress = group.progressPercent
            progressBar.progressTintList = ContextCompat.getColorStateList(context, category.colorRes)
            progressBar.progressBackgroundTintList = ContextCompat.getColorStateList(context, category.lightColorRes)
            tvPercent.text = "${group.progressPercent}%"
            tvPercent.setTextColor(ContextCompat.getColor(context, category.colorRes))
            
            layoutTaskGroups.addView(itemView)
        }
    }

    data class TaskGroupData(
        val category: TaskCategory,
        val totalTasks: Int,
        val progressPercent: Int
    )
}
