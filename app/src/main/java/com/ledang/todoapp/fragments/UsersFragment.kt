package com.ledang.todoapp.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.imageview.ShapeableImageView
import com.ledang.todoapp.R
import com.ledang.todoapp.data.UserPreferences
import android.widget.TextView
import java.io.File
import java.io.FileOutputStream

class UsersFragment : Fragment() {

    private lateinit var tvUserName: TextView
    private lateinit var switchNotifications: SwitchCompat
    private lateinit var imgAvatar: ShapeableImageView
    private lateinit var layoutAvatar: FrameLayout
    private lateinit var layoutEditName: View
    
    // Activity result launcher for picking image from gallery
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    
    // Permission launcher
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize pick image launcher
        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    saveImageToInternalStorage(uri)
                }
            }
        }
        
        // Initialize permission launcher
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                openGallery()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
        imgAvatar = view.findViewById(R.id.img_avatar)
        layoutAvatar = view.findViewById(R.id.layout_avatar)
        layoutEditName = view.findViewById(R.id.layout_edit_name)

        // Load user name
        tvUserName.text = UserPreferences.getUserName(requireContext())
        
        // Load profile image
        loadProfileImage()

        // Load notifications preference
        switchNotifications.isChecked = UserPreferences.isNotificationsEnabled(requireContext())

        // Avatar click - change profile image
        layoutAvatar.setOnClickListener {
            checkPermissionAndOpenGallery()
        }
        
        // Name section click - edit name
        layoutEditName.setOnClickListener {
            showEditNameDialog()
        }

        // Notifications toggle
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            UserPreferences.setNotificationsEnabled(requireContext(), isChecked)
        }
    }
    
    private fun loadProfileImage() {
        val imagePath = UserPreferences.getProfileImagePath(requireContext())
        if (imagePath != null) {
            val file = File(imagePath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(imagePath)
                imgAvatar.setImageBitmap(bitmap)
                imgAvatar.setPadding(0, 0, 0, 0) // Remove padding when showing user image
            }
        }
    }
    
    private fun checkPermissionAndOpenGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                // Show explanation then request
                Toast.makeText(requireContext(), "Permission needed to access gallery", Toast.LENGTH_SHORT).show()
                permissionLauncher.launch(permission)
            }
            else -> {
                permissionLauncher.launch(permission)
            }
        }
    }
    
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }
    
    private fun saveImageToInternalStorage(uri: Uri) {
        try {
            val context = requireContext()
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            // Save to internal storage
            val filename = "profile_image.jpg"
            val file = File(context.filesDir, filename)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()
            
            // Save path to preferences
            UserPreferences.saveProfileImagePath(context, file.absolutePath)
            
            // Update UI
            imgAvatar.setImageBitmap(bitmap)
            imgAvatar.setPadding(0, 0, 0, 0)
            
            Toast.makeText(context, "Profile image updated!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
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
