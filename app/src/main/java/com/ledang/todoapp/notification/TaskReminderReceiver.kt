package com.ledang.todoapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ledang.todoapp.data.database.TaskDatabase
import com.ledang.todoapp.data.enums.TaskStatus
import kotlin.concurrent.thread

class TaskReminderReceiver : BroadcastReceiver() {
    
    companion object {
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_NOTIFICATION_TYPE = "notification_type"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
        val typeOrdinal = intent.getIntExtra(EXTRA_NOTIFICATION_TYPE, -1)
        
        if (taskId == -1L || typeOrdinal == -1) return
        
        val notificationType = NotificationHelper.NotificationType.values().getOrNull(typeOrdinal) ?: return
        
        // Create notification channel if needed
        NotificationHelper.createNotificationChannel(context)
        
        // Get task info from database
        thread {
            val db = TaskDatabase.getDatabase(context)
            val task = db.taskDao().getTaskById(taskId) ?: return@thread
            
            // For overdue notification, check if task is still not completed
            if (notificationType == NotificationHelper.NotificationType.OVERDUE) {
                if (task.status == TaskStatus.COMPLETED) {
                    return@thread // Task already completed, no need to notify
                }
            }
            
            // For start reminder, only notify if task is TODO
            if (notificationType == NotificationHelper.NotificationType.START_REMINDER) {
                if (task.status != TaskStatus.TODO) {
                    return@thread
                }
            }
            
            // For deadline reminder, notify if not completed
            if (notificationType == NotificationHelper.NotificationType.DEADLINE_REMINDER) {
                if (task.status == TaskStatus.COMPLETED) {
                    return@thread
                }
            }
            
            // Show notification
            NotificationHelper.showNotification(
                context = context,
                taskId = taskId,
                taskName = task.name,
                type = notificationType
            )
        }
    }
}
