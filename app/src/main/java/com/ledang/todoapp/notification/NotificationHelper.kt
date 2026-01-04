package com.ledang.todoapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ledang.todoapp.MainActivity
import com.ledang.todoapp.R
import com.ledang.todoapp.TodoApplication
import com.ledang.todoapp.data.UserPreferences

object NotificationHelper {
    
    private const val CHANNEL_ID = "task_reminders"
    private const val CHANNEL_NAME = "Task Reminders"
    private const val CHANNEL_DESCRIPTION = "Notifications for task reminders"
    
    enum class NotificationType {
        START_REMINDER,      // 15 min before start
        DEADLINE_REMINDER,   // 15 min before end
        OVERDUE              // Task is overdue
    }
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showNotification(
        context: Context,
        taskId: Long,
        taskName: String,
        type: NotificationType
    ) {
        // Check if notifications are enabled
        if (!UserPreferences.isNotificationsEnabled(context)) {
            return
        }
        
        // Don't show notification if app is in foreground
        if (TodoApplication.isAppInForeground) {
            return
        }
        
        val (title, message) = when (type) {
            NotificationType.START_REMINDER -> {
                "Task is about to start" to "\"$taskName\" will start in 15 minutes"
            }
            NotificationType.DEADLINE_REMINDER -> {
                "Task is approaching its deadline" to "\"$taskName\" will be due in 15 minutes"
            }
            NotificationType.OVERDUE -> {
                "Task is overdue" to "\"$taskName\" is overdue and has not been completed"
            }

        }
        
        // Intent to open app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        // Use unique ID for each notification (taskId * 10 + type ordinal)
        val notificationId = (taskId * 10 + type.ordinal).toInt()
        
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted
        }
    }
}
