package com.ledang.todoapp.fragments

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ledang.todoapp.MainActivity
import com.ledang.todoapp.R
import com.ledang.todoapp.data.database.TaskDatabase
import com.ledang.todoapp.data.enums.TaskCategory
import kotlin.concurrent.thread

class DocumentFragment : Fragment() {

    private lateinit var rvCategories: RecyclerView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_document, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        rvCategories = view.findViewById(R.id.rv_categories)
        rvCategories.layoutManager = LinearLayoutManager(context)
        
        loadCategories()
    }

    override fun onResume() {
        super.onResume()
        loadCategories()
    }

    private fun loadCategories() {
        thread {
            val db = TaskDatabase.getDatabase(requireContext())
            val dao = db.taskDao()
            
            val categoryData = TaskCategory.values().map { category ->
                val count = dao.countTasksByCategory(category)
                CategoryWithCount(category, count)
            }
            
            activity?.runOnUiThread {
                rvCategories.adapter = CategoryAdapter(categoryData) { category ->
                    // Navigate to CategoryTasksFragment
                    (activity as? MainActivity)?.navigateToCategoryTasks(category)
                }
            }
        }
    }

    data class CategoryWithCount(
        val category: TaskCategory,
        val taskCount: Int
    )

    inner class CategoryAdapter(
        private val categories: List<CategoryWithCount>,
        private val onClick: (TaskCategory) -> Unit
    ) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val frameIcon: FrameLayout = view.findViewById(R.id.frame_icon)
            val imgIcon: ImageView = view.findViewById(R.id.img_category_icon)
            val tvName: TextView = view.findViewById(R.id.tv_category_name)
            val tvCount: TextView = view.findViewById(R.id.tv_task_count)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_category_document, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = categories[position]
            val category = item.category
            val context = holder.itemView.context

            // Set icon
            holder.imgIcon.setImageResource(category.iconRes)
            holder.imgIcon.imageTintList = ContextCompat.getColorStateList(context, category.colorRes)

            // Set icon background
            val background = holder.frameIcon.background
            if (background is GradientDrawable) {
                background.setColor(ContextCompat.getColor(context, category.lightColorRes))
            } else {
                val newBackground = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(ContextCompat.getColor(context, category.lightColorRes))
                }
                holder.frameIcon.background = newBackground
            }

            // Set texts
            holder.tvName.text = category.displayName + " Project"
            holder.tvCount.text = "${item.taskCount} Tasks"

            // Click listener
            holder.itemView.setOnClickListener {
                onClick(category)
            }
        }

        override fun getItemCount() = categories.size
    }
}
