package com.ledang.todoapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ledang.todoapp.data.database.TaskDatabase
import com.ledang.todoapp.data.enums.TaskStatus
import kotlin.concurrent.thread

/**
 * Reschedules all task reminders after device reboot.
 * Alarms are cleared when the device is powered off, so we need to reschedule them.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Create notification channel
            NotificationHelper.createNotificationChannel(context)
            
            // Reschedule all pending task reminders
            thread {
                val db = TaskDatabase.getDatabase(context)
                val allTasks = db.taskDao().getAllTasks()
                
                allTasks.forEach { task ->
                    if (task.status != TaskStatus.COMPLETED) {
                        TaskAlarmScheduler.scheduleTaskReminders(context, task)
                    }
                }
            }
        }
    }
}
