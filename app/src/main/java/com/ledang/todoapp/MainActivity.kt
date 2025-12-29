package com.ledang.todoapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ledang.todoapp.fragments.AddTaskFragment
import com.ledang.todoapp.fragments.CalendarFragment
import com.ledang.todoapp.fragments.DocumentFragment
import com.ledang.todoapp.fragments.HomeFragment
import com.ledang.todoapp.fragments.UsersFragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

    // Hàm để chuyển sang CalendarFragment từ HomeFragment
    fun navigateToCalendar() {
        bottomNav.selectedItemId = R.id.calendarFragment
    }
}
