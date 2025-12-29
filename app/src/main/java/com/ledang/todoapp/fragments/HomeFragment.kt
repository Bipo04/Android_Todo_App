package com.ledang.todoapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ledang.todoapp.MainActivity
import com.ledang.todoapp.R

class HomeFragment : Fragment() {
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Button "View Task" -> mở CalendarFragment
        view.findViewById<TextView>(R.id.btn_view_task).setOnClickListener {
            (activity as? MainActivity)?.navigateToCalendar()
        }
    }
}
