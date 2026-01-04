package com.ledang.todoapp.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.ledang.todoapp.MainActivity
import com.ledang.todoapp.R
import com.ledang.todoapp.data.UserPreferences

class UsersFragment : Fragment() {

    private lateinit var tvUserName: TextView
    private lateinit var switchNotifications: SwitchCompat
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_users, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvUserName = view.findViewById(R.id.tv_user_name)
        switchNotifications = view.findViewById(R.id.switch_notifications)

        // Load user name
        tvUserName.text = UserPreferences.getUserName(requireContext())

        // Load notifications preference
        switchNotifications.isChecked = UserPreferences.isNotificationsEnabled(requireContext())

        // Edit name - both profile card and edit button
        view.findViewById<View>(R.id.layout_profile).setOnClickListener {
            showEditNameDialog()
        }

        // Notifications toggle
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            UserPreferences.setNotificationsEnabled(requireContext(), isChecked)
        }
    }

    private fun showEditNameDialog() {
        val context = requireContext()
        val editText = EditText(context).apply {
            setText(UserPreferences.getUserName(context))
            hint = "Enter your name"
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(context)
            .setTitle("Edit Name")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    UserPreferences.saveUserName(context, newName)
                    tvUserName.text = newName
                    Toast.makeText(context, "Name updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

