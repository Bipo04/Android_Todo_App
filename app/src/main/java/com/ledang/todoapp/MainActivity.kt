package com.ledang.todoapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ledang.todoapp.data.UserPreferences
import com.ledang.todoapp.fragments.AddTaskFragment
import com.ledang.todoapp.fragments.CalendarFragment
import com.ledang.todoapp.fragments.DocumentFragment
import com.ledang.todoapp.fragments.HomeFragment
import com.ledang.todoapp.fragments.TaskDetailFragment
import com.ledang.todoapp.fragments.UsersFragment
import com.ledang.todoapp.notification.NotificationHelper
import com.ledang.todoapp.notification.TaskAlarmScheduler
import com.ledang.todoapp.data.database.TaskDatabase
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    
    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if first launch - redirect to WelcomeActivity
        if (UserPreferences.isFirstLaunch(this)) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
            return
        }
        
        setContentView(R.layout.activity_main)
        
        // Create notification channel
        NotificationHelper.createNotificationChannel(this)
        
        // Request notification permission for Android 13+
        requestNotificationPermission()
        
        // Schedule alarms for all pending tasks (ensures existing tasks get notifications)
        scheduleAllPendingTasks()

        bottomNav = findViewById(R.id.bottomNavigationView)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fab_add)

        // Load HomeFragment mặc định
        replaceFragment(HomeFragment())

        // Đặt background null để transparent
        bottomNav.background = null

        // Xử lý click navigation items
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> replaceFragment(HomeFragment())
                R.id.calendarFragment -> replaceFragment(CalendarFragment())
                R.id.documentFragment -> replaceFragment(DocumentFragment())
                R.id.usersFragment -> replaceFragment(UsersFragment())
            }
            true
        }

        // Xử lý khi click lại item đang được chọn (reselect)
        bottomNav.setOnItemReselectedListener { item ->
            // Nếu đang ở AddTask thì navigate về fragment tương ứng
            val currentFragment = supportFragmentManager.findFragmentById(R.id.frame_layout)
            if (currentFragment is AddTaskFragment) {
                item.isChecked = true // Đảm bảo icon sáng
                when (item.itemId) {
                    R.id.homeFragment -> replaceFragment(HomeFragment())
                    R.id.calendarFragment -> replaceFragment(CalendarFragment())
                    R.id.documentFragment -> replaceFragment(DocumentFragment())
                    R.id.usersFragment -> replaceFragment(UsersFragment())
                }
            }
        }

        // FAB click - mở AddTask
        fabAdd.setOnClickListener {
            // Kiểm tra nếu đang ở AddTaskFragment thì không làm gì
            val currentFragment = supportFragmentManager.findFragmentById(R.id.frame_layout)
            if (currentFragment is AddTaskFragment) {
                return@setOnClickListener
            }

            // Clear selection của bottom nav
            bottomNav.menu.findItem(bottomNav.selectedItemId)?.isChecked = false
            
            replaceFragment(AddTaskFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }
    
    // Replace fragment with back stack support
    private fun replaceFragmentWithBackStack(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }
    
    // Go back to previous fragment
    fun goBack() {
        supportFragmentManager.popBackStack()
    }

    // Hàm để chuyển sang CalendarFragment từ HomeFragment
    fun navigateToCalendar() {
        bottomNav.selectedItemId = R.id.calendarFragment
    }
    
    // Hàm để chuyển sang AddTaskFragment
    fun navigateToAddTask() {
        // Clear selection của bottom nav
        bottomNav.menu.findItem(bottomNav.selectedItemId)?.isChecked = false
        replaceFragment(AddTaskFragment())
    }
    
    // Hàm để chuyển sang TaskDetailFragment (giữ nav selection, có back stack)
    fun navigateToTaskDetail(taskId: Long) {
        replaceFragmentWithBackStack(TaskDetailFragment.newInstance(taskId))
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }
    
    private fun scheduleAllPendingTasks() {
        thread {
            val db = TaskDatabase.getDatabase(this)
            val allTasks = db.taskDao().getAllTasks()
            
            // Schedule alarms for all non-completed tasks
            allTasks.forEach { task ->
                if (task.status != com.ledang.todoapp.data.enums.TaskStatus.COMPLETED) {
                    TaskAlarmScheduler.scheduleTaskReminders(this, task)
                }
            }
        }
    }
    
    // Hàm để chuyển sang CategoryTasksFragment
    fun navigateToCategoryTasks(category: com.ledang.todoapp.data.enums.TaskCategory) {
        replaceFragmentWithBackStack(com.ledang.todoapp.fragments.CategoryTasksFragment.newInstance(category))
    }
}
