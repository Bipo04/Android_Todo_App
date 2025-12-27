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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
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

        // FAB click - mở AddTask và bỏ chọn nav items
        fabAdd.setOnClickListener {
            // Bỏ chọn tất cả navigation items
            bottomNav.menu.setGroupCheckable(0, true, false)
            for (i in 0 until bottomNav.menu.size()) {
                bottomNav.menu.getItem(i).isChecked = false
            }
            bottomNav.menu.setGroupCheckable(0, true, true)
            
            replaceFragment(AddTaskFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }
}
